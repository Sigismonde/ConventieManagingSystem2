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
import ro.upt.ac.conventii.security.User;
import ro.upt.ac.conventii.security.UserRepository;
import ro.upt.ac.conventii.service.PasswordGeneratorService;

@Controller
@RequestMapping("/prodecan/management/tutori")
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
    
    @GetMapping("")
    public String listTutori(Model model, Authentication authentication) {
        User user = (User) authentication.getPrincipal();
        model.addAttribute("user", user);
        
        try {
            List<Tutore> tutori = tutoreRepository.findAll();
            model.addAttribute("tutori", tutori != null ? tutori : new ArrayList<>());
        } catch (Exception e) {
            e.printStackTrace();
            model.addAttribute("errorMessage", "Eroare la încărcarea listei de tutori: " + e.getMessage());
        }
        
        return "prodecan/management/tutori";
    }
    
    @GetMapping("/create")
    public String showCreateForm(Model model, Authentication authentication) {
        User user = (User) authentication.getPrincipal();
        model.addAttribute("user", user);
        
        try {
            Tutore tutore = new Tutore();
            model.addAttribute("tutore", tutore);
            
            List<Companie> companii = companieRepository.findAll();
            model.addAttribute("companii", companii != null ? companii : new ArrayList<>());
        } catch (Exception e) {
            e.printStackTrace();
            model.addAttribute("errorMessage", "Eroare la încărcarea formularului: " + e.getMessage());
        }
        
        return "prodecan/management/tutore-form";
    }
    
    @PostMapping("/create")
    public String createTutore(@ModelAttribute Tutore tutore, RedirectAttributes redirectAttributes) {
        try {
            // Verificăm dacă compania selectată există
            if (tutore.getCompanie() != null && tutore.getCompanie().getId() > 0) {
                Companie companie = companieRepository.findById(tutore.getCompanie().getId());
                if (companie != null) {
                    tutore.setCompanie(companie);
                } else {
                    throw new RuntimeException("Compania selectată nu a fost găsită!");
                }
            } else {
                throw new RuntimeException("Este necesară selectarea unei companii!");
            }
            
            // Verificăm dacă există deja un utilizator cu acest email
            User existingUser = userRepository.findByEmail(tutore.getEmail());
            if (existingUser != null) {
                throw new RuntimeException("Există deja un utilizator cu adresa de email " + tutore.getEmail());
            }
            
            // Salvăm tutorele
            Tutore savedTutore = tutoreRepository.save(tutore);
            
            if (savedTutore == null || savedTutore.getId() == 0) {
                throw new RuntimeException("Eroare la salvarea tutorelui în baza de date!");
            }
            
            // Creăm contul de utilizator
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
                
            return "redirect:/prodecan/management/tutori";
        } catch (Exception e) {
            e.printStackTrace();
            redirectAttributes.addFlashAttribute("errorMessage", 
                "Eroare la crearea tutorelui: " + e.getMessage());
            return "redirect:/prodecan/management/tutori/create";
        }
    }
}