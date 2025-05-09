
package ro.upt.ac.conventii.tutore;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import ro.upt.ac.conventii.companie.Companie;
import ro.upt.ac.conventii.companie.CompanieRepository;
import ro.upt.ac.conventii.conventie.Conventie;
import ro.upt.ac.conventii.conventie.ConventieRepository;
import ro.upt.ac.conventii.security.User;
import ro.upt.ac.conventii.security.UserRepository;
import ro.upt.ac.conventii.service.PasswordGeneratorService;

@Controller
@RequestMapping("/prodecan/management/tutori") // Changed mapping
public class TutoreAdminController {

    @Autowired
    private TutoreRepository tutoreRepository;
    
    @Autowired
    private CompanieRepository companieRepository;
    
    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private BCryptPasswordEncoder passwordEncoder;
    
    @Autowired
    private PasswordGeneratorService passwordGeneratorService;
    
    @GetMapping("")  // This maps to "/prodecan/management/tutori"
    public String listTutori(Model model, Authentication authentication) {
        User user = (User) authentication.getPrincipal();
        model.addAttribute("user", user);
        
        List<Tutore> tutori = tutoreRepository.findAll();
        model.addAttribute("tutori", tutori != null ? tutori : new ArrayList<>());
        
        return "prodecan/management/tutori-list";  // Make sure this path is correct
    }
    
    // Show create tutor form
    @GetMapping("/create") // This maps to /prodecan/management/tutori/create
    public String showCreateForm(Model model, Authentication authentication) {
        User user = (User) authentication.getPrincipal();
        model.addAttribute("user", user);
        
        model.addAttribute("tutore", new Tutore());
        model.addAttribute("companii", companieRepository.findAll());
        
        return "prodecan/management/tutore-form"; // Update template path
    }
    
    // Create a new tutor
    @PostMapping("/create")
    public String createTutore(@ModelAttribute Tutore tutore, RedirectAttributes redirectAttributes) {
        try {
            // Save the tutor
            Companie companie = companieRepository.findById(tutore.getCompanie().getId());
            tutore.setCompanie(companie);
            tutoreRepository.save(tutore);
            
            // Create user account
            String temporaryPassword = passwordGeneratorService.generateRandomPassword();
            User userTutore = new User();
            userTutore.setEmail(tutore.getEmail());
            userTutore.setNume(tutore.getNume());
            userTutore.setPrenume(tutore.getPrenume());
            userTutore.setPassword(passwordEncoder.encode(temporaryPassword));
            userTutore.setRole("ROLE_TUTORE");
            userTutore.setEnabled(true);
            userTutore.setFirstLogin(true);
            
            userRepository.save(userTutore);
            
            redirectAttributes.addFlashAttribute("successMessage", 
                "Tutore creat cu succes!\n" +
                "----------------------------------------\n" +
                "Email: " + tutore.getEmail() + "\n" +
                "PAROLA TEMPORARĂ: " + temporaryPassword + "\n" +
                "----------------------------------------\n" +
                "IMPORTANT: Salvați această parolă!");
                
            return "redirect:/prodecan/management/tutori"; // Update redirect URL
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", 
                "Eroare la crearea tutorelui: " + e.getMessage());
            return "redirect:/prodecan/management/tutori/create"; // Update redirect URL
        }
    }
    
    // Show edit tutor form
    @GetMapping("/edit/{id}")
    public String showEditForm(@PathVariable int id, Model model, Authentication authentication) {
        User user = (User) authentication.getPrincipal();
        model.addAttribute("user", user);
        
        Tutore tutore = tutoreRepository.findById(id);
        if (tutore == null) {
            return "redirect:/prodecan/management/tutori"; // Update redirect URL
        }
        
        model.addAttribute("tutore", tutore);
        model.addAttribute("companii", companieRepository.findAll());
        
        return "prodecan/management/tutore-form"; // Update template path
    }
    
    // Update tutor
    @PostMapping("/edit/{id}")
    public String updateTutore(@PathVariable int id, @ModelAttribute Tutore tutore, RedirectAttributes redirectAttributes) {
        try {
            Tutore existingTutore = tutoreRepository.findById(id);
            if (existingTutore == null) {
                redirectAttributes.addFlashAttribute("errorMessage", "Tutore negăsit!");
                return "redirect:/prodecan/management/tutori"; // Update redirect URL
            }
            
            // Save original email
            String originalEmail = existingTutore.getEmail();
            
            // Update tutor fields
            existingTutore.setNume(tutore.getNume());
            existingTutore.setPrenume(tutore.getPrenume());
            existingTutore.setFunctie(tutore.getFunctie());
            existingTutore.setTelefon(tutore.getTelefon());
            
            // Update company if changed
            if (existingTutore.getCompanie().getId() != tutore.getCompanie().getId()) {
                Companie companie = companieRepository.findById(tutore.getCompanie().getId());
                existingTutore.setCompanie(companie);
            }
            tutoreRepository.save(existingTutore);
            
            // Update user account if exists
            User userTutore = userRepository.findByEmail(originalEmail);
            if (userTutore != null) {
                userTutore.setNume(tutore.getNume());
                userTutore.setPrenume(tutore.getPrenume());
                userRepository.save(userTutore);
            }
            
            redirectAttributes.addFlashAttribute("successMessage", "Tutore actualizat cu succes!");
            return "redirect:/prodecan/management/tutori"; // Update redirect URL
            
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", 
                "Eroare la actualizarea tutorelui: " + e.getMessage());
            return "redirect:/prodecan/management/tutori/edit/" + id; // Update redirect URL
        }
    }
    
    // Delete tutor
    @PostMapping("/delete/{id}")
    public String deleteTutore(@PathVariable int id, RedirectAttributes redirectAttributes) {
        try {
            Tutore tutore = tutoreRepository.findById(id);
            if (tutore == null) {
                redirectAttributes.addFlashAttribute("errorMessage", "Tutore negăsit!");
                return "redirect:/prodecan/management/tutori"; // Update redirect URL
            }
            
            // Delete user account if exists
            User userTutore = userRepository.findByEmail(tutore.getEmail());
            if (userTutore != null) {
                userRepository.delete(userTutore);
            }
            
            // Delete tutor
            tutoreRepository.delete(tutore);
            
            redirectAttributes.addFlashAttribute("successMessage", "Tutore șters cu succes!");
            
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", 
                "Eroare la ștergerea tutorelui: " + e.getMessage());
        }
        
        return "redirect:/prodecan/management/tutori"; // Update redirect URL
    }
    
    // Reset tutor password
    @PostMapping("/reset-password/{id}")
    public String resetPassword(@PathVariable int id, RedirectAttributes redirectAttributes) {
        try {
            Tutore tutore = tutoreRepository.findById(id);
            if (tutore == null) {
                redirectAttributes.addFlashAttribute("errorMessage", "Tutore negăsit!");
                return "redirect:/prodecan/management/tutori"; // Update redirect URL
            }
            
            User userTutore = userRepository.findByEmail(tutore.getEmail());
            if (userTutore != null) {
                String newPassword = passwordGeneratorService.generateRandomPassword();
                userTutore.setPassword(passwordEncoder.encode(newPassword));
                userTutore.setFirstLogin(true);
                userRepository.save(userTutore);
                
                redirectAttributes.addFlashAttribute("successMessage", 
                    "Parolă resetată cu succes!\n" +
                    "----------------------------------------\n" +
                    "Tutore: " + tutore.getNume() + " " + tutore.getPrenume() + "\n" +
                    "Email: " + tutore.getEmail() + "\n" +
                    "NOUA PAROLĂ: " + newPassword + "\n" +
                    "----------------------------------------\n" +
                    "IMPORTANT: Salvați această parolă!");
            } else {
                redirectAttributes.addFlashAttribute("errorMessage", 
                    "Nu există un cont de utilizator pentru acest tutore!");
            }
            
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", 
                "Eroare la resetarea parolei: " + e.getMessage());
        }
        
        return "redirect:/prodecan/management/tutori"; // Update redirect URL
    }
}