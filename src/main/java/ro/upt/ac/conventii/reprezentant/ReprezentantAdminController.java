package ro.upt.ac.conventii.reprezentant;

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
import ro.upt.ac.conventii.security.User;
import ro.upt.ac.conventii.security.UserRepository;
import ro.upt.ac.conventii.service.PasswordGeneratorService;

@Controller
@RequestMapping("/prodecan/management") // Modificat pentru a se potrivi cu maparea din formular
public class ReprezentantAdminController {

    @Autowired
    private ReprezentantRepository reprezentantRepository;
    
    @Autowired
    private CompanieRepository companieRepository;
    
    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private BCryptPasswordEncoder passwordEncoder;
    
    @Autowired
    private PasswordGeneratorService passwordGeneratorService;
    
    // List all reprezentanti
    @GetMapping("/reprezentanti")
    public String listReprezentanti(Model model, Authentication authentication) {
        User user = (User) authentication.getPrincipal();
        model.addAttribute("user", user);
        
        List<Reprezentant> reprezentanti = reprezentantRepository.findAll();
        model.addAttribute("reprezentanti", reprezentanti);
        
        return "prodecan/management/reprezentanti";
    }
    
    // Show create reprezentant form
    @GetMapping("/create-reprezentant")
    public String showCreateForm(Model model, Authentication authentication) {
        User user = (User) authentication.getPrincipal();
        model.addAttribute("user", user);
        
        model.addAttribute("reprezentant", new Reprezentant());
        model.addAttribute("companii", companieRepository.findAll());
        
        return "prodecan/management/create-reprezentant";
    }
    
    // Create a new reprezentant
    @PostMapping("/reprezentanti/create")
    public String createReprezentant(@ModelAttribute Reprezentant reprezentant, RedirectAttributes redirectAttributes) {
        try {
            System.out.println("Se încearcă crearea reprezentantului: " + reprezentant.getEmail());
            
            // Verificăm dacă email-ul este valid
            if (reprezentant.getEmail() == null || reprezentant.getEmail().trim().isEmpty()) {
                redirectAttributes.addFlashAttribute("errorMessage", 
                    "Email-ul este obligatoriu!");
                return "redirect:/prodecan/management/create-reprezentant";
            }
            
            // Verificăm dacă există deja un reprezentant cu acest email
            if (reprezentantRepository.findByEmail(reprezentant.getEmail()).isPresent()) {
                redirectAttributes.addFlashAttribute("errorMessage", 
                    "Există deja un reprezentant cu această adresă de email!");
                return "redirect:/prodecan/management/create-reprezentant";
            }
            
            // Verificăm dacă compania există
            Companie companie = companieRepository.findById(reprezentant.getCompanie().getId())
                    .orElseThrow(() -> new RuntimeException("Compania nu a fost găsită"));
            if (companie == null) {
                redirectAttributes.addFlashAttribute("errorMessage", 
                    "Compania selectată nu există!");
                return "redirect:/prodecan/management/create-reprezentant";
            }
            
            // Setăm compania corect
            reprezentant.setCompanie(companie);
            
            // Salvăm reprezentantul
            Reprezentant savedReprezentant = reprezentantRepository.save(reprezentant);
            System.out.println("Reprezentant salvat cu ID: " + savedReprezentant.getId());
            
            // Verificăm dacă există deja un cont de utilizator
            User existingUser = userRepository.findByEmail(reprezentant.getEmail());
            if (existingUser != null) {
                redirectAttributes.addFlashAttribute("errorMessage", 
                    "Există deja un utilizator cu această adresă de email!");
                return "redirect:/prodecan/management/reprezentanti";
            }
            
            // Creăm contul de utilizator
            String temporaryPassword = passwordGeneratorService.generateRandomPassword();
            User userReprezentant = new User();
            userReprezentant.setEmail(reprezentant.getEmail());
            userReprezentant.setNume(reprezentant.getNume());
            userReprezentant.setPrenume(reprezentant.getPrenume());
            userReprezentant.setPassword(passwordEncoder.encode(temporaryPassword));
            userReprezentant.setRole("ROLE_REPREZENTANT");
            userReprezentant.setEnabled(true);
            userReprezentant.setFirstLogin(true);
            
            userRepository.save(userReprezentant);
            
            redirectAttributes.addFlashAttribute("successMessage", 
                "Reprezentant creat cu succes!\n" +
                "----------------------------------------\n" +
                "Email: " + reprezentant.getEmail() + "\n" +
                "PAROLA TEMPORARĂ: " + temporaryPassword + "\n" +
                "----------------------------------------\n" +
                "IMPORTANT: Salvați această parolă!");
                
            return "redirect:/prodecan/management/reprezentanti";
            
        } catch (Exception e) {
            e.printStackTrace();
            redirectAttributes.addFlashAttribute("errorMessage", 
                "Eroare la crearea reprezentantului: " + e.getMessage());
            return "redirect:/prodecan/management/create-reprezentant";
        }
    }
    
    @GetMapping("/reprezentanti/edit/{id}")
    public String showEditReprezentantForm(@PathVariable int id, Model model, Authentication authentication) {
        User user = (User) authentication.getPrincipal();
        model.addAttribute("user", user);
        
        Reprezentant reprezentant = reprezentantRepository.findById(id);
        if (reprezentant == null) {
            return "redirect:/prodecan/management/reprezentanti";
        }
        
        model.addAttribute("reprezentant", reprezentant);
        model.addAttribute("companii", companieRepository.findAll());
        
        return "prodecan/management/reprezentant-form";
    }
    
    @PostMapping("/reprezentanti/edit/{id}")
    public String updateReprezentant(@PathVariable int id, @ModelAttribute Reprezentant reprezentant, RedirectAttributes redirectAttributes) {
        try {
            System.out.println("=== UPDATE REPREZENTANT DEBUG ===");
            System.out.println("ID: " + id);
            System.out.println("Reprezentant data: " + reprezentant);
            
            Reprezentant existingReprezentant = reprezentantRepository.findById(id);
            if (existingReprezentant == null) {
                redirectAttributes.addFlashAttribute("errorMessage", "Reprezentantul nu a fost găsit!");
                return "redirect:/prodecan/management/reprezentanti";
            }
            
            // Salvăm email-ul original
            String originalEmail = existingReprezentant.getEmail();
            
            // Actualizăm câmpurile reprezentantului
            existingReprezentant.setNume(reprezentant.getNume());
            existingReprezentant.setPrenume(reprezentant.getPrenume());
            existingReprezentant.setFunctie(reprezentant.getFunctie());
            existingReprezentant.setTelefon(reprezentant.getTelefon());
            
            // Actualizăm compania dacă s-a schimbat
            if (reprezentant.getCompanie() != null && reprezentant.getCompanie().getId() != 0) {
                Companie companie = companieRepository.findById(reprezentant.getCompanie().getId())
                        .orElseThrow(() -> new RuntimeException("Compania nu a fost găsită"));
                if (companie != null) {
                    existingReprezentant.setCompanie(companie);
                }
            }
            
            // Salvăm reprezentantul
            reprezentantRepository.save(existingReprezentant);
            
            // Actualizăm contul de utilizator
            User userReprezentant = userRepository.findByEmail(originalEmail);
            if (userReprezentant != null) {
                userReprezentant.setNume(reprezentant.getNume());
                userReprezentant.setPrenume(reprezentant.getPrenume());
                userRepository.save(userReprezentant);
            }
            
            redirectAttributes.addFlashAttribute("successMessage", "Reprezentant actualizat cu succes!");
            
            return "redirect:/prodecan/management/reprezentanti";
            
        } catch (Exception e) {
            e.printStackTrace();
            redirectAttributes.addFlashAttribute("errorMessage", 
                "Eroare la actualizarea reprezentantului: " + e.getMessage());
            return "redirect:/prodecan/management/reprezentanti/edit/" + id;
        }
    }
    
    @PostMapping("/reprezentanti/delete/{id}")
    public String deleteReprezentant(@PathVariable int id, RedirectAttributes redirectAttributes) {
        try {
            Reprezentant reprezentant = reprezentantRepository.findById(id);
            if (reprezentant == null) {
                redirectAttributes.addFlashAttribute("errorMessage", "Reprezentant negăsit!");
                return "redirect:/prodecan/management/reprezentanti";
            }
            
            // Ștergem contul de utilizator dacă există
            User userReprezentant = userRepository.findByEmail(reprezentant.getEmail());
            if (userReprezentant != null) {
                userRepository.delete(userReprezentant);
            }
            
            // Ștergem reprezentantul
            reprezentantRepository.delete(reprezentant);
            
            redirectAttributes.addFlashAttribute("successMessage", "Reprezentant șters cu succes!");
            
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", 
                "Eroare la ștergerea reprezentantului: " + e.getMessage());
        }
        
        return "redirect:/prodecan/management/reprezentanti";
    }
    
    @PostMapping("/reprezentanti/reset-password/{id}")
    public String resetPassword(@PathVariable int id, RedirectAttributes redirectAttributes) {
        try {
            Reprezentant reprezentant = reprezentantRepository.findById(id);
            if (reprezentant == null) {
                redirectAttributes.addFlashAttribute("errorMessage", "Reprezentant negăsit!");
                return "redirect:/prodecan/management/reprezentanti";
            }
            
            User userReprezentant = userRepository.findByEmail(reprezentant.getEmail());
            if (userReprezentant != null) {
                String newPassword = passwordGeneratorService.generateRandomPassword();
                userReprezentant.setPassword(passwordEncoder.encode(newPassword));
                userReprezentant.setFirstLogin(true);
                userRepository.save(userReprezentant);
                
                redirectAttributes.addFlashAttribute("successMessage", 
                    "Parolă resetată cu succes!\n" +
                    "----------------------------------------\n" +
                    "Reprezentant: " + reprezentant.getNume() + " " + reprezentant.getPrenume() + "\n" +
                    "Email: " + reprezentant.getEmail() + "\n" +
                    "NOUA PAROLĂ: " + newPassword + "\n" +
                    "----------------------------------------\n" +
                    "IMPORTANT: Salvați această parolă!");
            } else {
                redirectAttributes.addFlashAttribute("errorMessage", 
                    "Nu există un cont de utilizator pentru acest reprezentant!");
            }
            
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", 
                "Eroare la resetarea parolei: " + e.getMessage());
        }
        
        return "redirect:/prodecan/management/reprezentanti";
    }
}