package ro.upt.ac.conventii.tutore;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

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
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import ro.upt.ac.conventii.companie.Companie;
import ro.upt.ac.conventii.companie.CompanieRepository;
import ro.upt.ac.conventii.conventie.Conventie;
import ro.upt.ac.conventii.conventie.ConventieRepository;
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
    private ConventieRepository conventieRepository;
    
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
    
 // În TutoreAdminController.java - actualizează metoda createTutore

    @PostMapping("/create")
    public String createTutore(@ModelAttribute("tutore") Tutore tutore, 
                               @RequestParam("companie.id") Integer companieId,
                               RedirectAttributes redirectAttributes) {
        try {
            // Verificăm dacă compania selectată există
            if (companieId != null && companieId > 0) {
                Companie companie = companieRepository.findCompanieById(companieId);
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
            
            // Creăm parola temporară în același stil ca la parteneri: prenume + nume
            String temporaryPassword = tutore.getPrenume() + tutore.getNume();
            
            // Creăm contul de utilizator
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
                "A fost creat automat și un cont de utilizator pentru tutore.\n" +
                "----------------------------------------\n" +
                "Tutore: " + tutore.getPrenume() + " " + tutore.getNume() + "\n" +
                "Email: " + tutore.getEmail() + "\n" +
                "PAROLA: " + temporaryPassword + "\n" +
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
    
    @GetMapping("/edit/{id}")
    public String showEditForm(@PathVariable int id, Model model, Authentication authentication) {
        User user = (User) authentication.getPrincipal();
        model.addAttribute("user", user);
        
        try {
            // IMPORTANT: Folosim metoda redenumită care returnează Tutore direct
            Tutore tutore = tutoreRepository.findTutoreById(id);
            if (tutore == null) {
                throw new RuntimeException("Tutorele cu ID-ul " + id + " nu a fost găsit!");
            }
            
            model.addAttribute("tutore", tutore);
            
            List<Companie> companii = companieRepository.findAll();
            model.addAttribute("companii", companii != null ? companii : new ArrayList<>());
        } catch (Exception e) {
            e.printStackTrace();
            model.addAttribute("errorMessage", "Eroare la încărcarea formularului: " + e.getMessage());
            return "redirect:/prodecan/management/tutori";
        }
        
        return "prodecan/management/tutore-form";
    }
    
    @PostMapping("/edit/{id}")
    public String updateTutore(@PathVariable int id, 
                              @ModelAttribute("tutore") Tutore tutore,
                              @RequestParam("companie.id") Integer companieId,
                              RedirectAttributes redirectAttributes) {
        try {
            // IMPORTANT: Folosim metoda redenumită care returnează Tutore direct
            Tutore existingTutore = tutoreRepository.findTutoreById(id);
            if (existingTutore == null) {
                throw new RuntimeException("Tutorele cu ID-ul " + id + " nu a fost găsit!");
            }
            
            // Verificăm dacă compania selectată există
            if (companieId != null && companieId > 0) {
                // IMPORTANT: Folosim metoda redenumită care returnează Companie direct
                Companie companie = companieRepository.findCompanieById(companieId);
                if (companie != null) {
                    tutore.setCompanie(companie);
                } else {
                    throw new RuntimeException("Compania selectată nu a fost găsită!");
                }
            } else {
                throw new RuntimeException("Este necesară selectarea unei companii!");
            }
            
            // Păstrăm ID-ul și email-ul existent
            tutore.setId(id);
            tutore.setEmail(existingTutore.getEmail());
            
            // Păstrăm și semnătura existentă dacă există
            if (existingTutore.getSemnatura() != null) {
                tutore.setSemnatura(existingTutore.getSemnatura());
            }
            
            // Salvăm tutorele actualizat
            tutoreRepository.save(tutore);
            
            // Actualizăm și contul de utilizator asociat dacă există
            User userTutore = userRepository.findByEmail(tutore.getEmail());
            if (userTutore != null) {
                userTutore.setNume(tutore.getNume());
                userTutore.setPrenume(tutore.getPrenume());
                userRepository.save(userTutore);
            }
            
            redirectAttributes.addFlashAttribute("successMessage", "Tutore actualizat cu succes!");
            return "redirect:/prodecan/management/tutori";
            
        } catch (Exception e) {
            e.printStackTrace();
            redirectAttributes.addFlashAttribute("errorMessage", 
                "Eroare la actualizarea tutorelui: " + e.getMessage());
            return "redirect:/prodecan/management/tutori/edit/" + id;
        }
    }
    
 // Adaugă aceste metode în TutoreAdminController.java

    @PostMapping("/reset-password/{id}")
    public String resetTutorePassword(@PathVariable int id, RedirectAttributes redirectAttributes) {
        try {
            // Găsim tutorele
            Tutore tutore = tutoreRepository.findTutoreById(id);
            if (tutore == null) {
                throw new RuntimeException("Tutorele cu ID-ul " + id + " nu a fost găsit!");
            }
            
            // Găsim utilizatorul asociat
            User userTutore = userRepository.findByEmail(tutore.getEmail());
            if (userTutore == null) {
                throw new RuntimeException("Nu există un cont de utilizator pentru acest tutore!");
            }
            
            // Generăm noua parolă temporară în același stil ca la parteneri
            String newTemporaryPassword = tutore.getPrenume() + tutore.getNume();
            
            // Actualizăm parola
            userTutore.setPassword(passwordEncoder.encode(newTemporaryPassword));
            userTutore.setFirstLogin(true);
            userRepository.save(userTutore);
            
            redirectAttributes.addFlashAttribute("successMessage", 
                "Parola a fost resetată cu succes!\n" +
                "----------------------------------------\n" +
                "Tutore: " + tutore.getPrenume() + " " + tutore.getNume() + "\n" +
                "Email: " + tutore.getEmail() + "\n" +
                "NOUA PAROLĂ: " + newTemporaryPassword + "\n" +
                "----------------------------------------\n" +
                "IMPORTANT: Salvați această parolă!");
                
        } catch (Exception e) {
            e.printStackTrace();
            redirectAttributes.addFlashAttribute("errorMessage", 
                "Eroare la resetarea parolei: " + e.getMessage());
        }
        
        return "redirect:/prodecan/management/tutori";
    }

    @PostMapping("/delete/{id}")
    public String deleteTutore(@PathVariable int id, RedirectAttributes redirectAttributes) {
        try {
            // Găsim tutorele
            Tutore tutore = tutoreRepository.findTutoreById(id);
            if (tutore == null) {
                throw new RuntimeException("Tutorele cu ID-ul " + id + " nu a fost găsit!");
            }
            
            // Verificăm dacă există convenții asociate cu acest tutore
            List<Conventie> conventiiAsociate = conventieRepository.findByTutoreId(id);
            if (!conventiiAsociate.isEmpty()) {
                throw new RuntimeException("Nu se poate șterge tutorele deoarece există convenții asociate!");
            }
            
            // Ștergem contul de utilizator asociat
            User userTutore = userRepository.findByEmail(tutore.getEmail());
            if (userTutore != null) {
                userRepository.delete(userTutore);
            }
            
            // Ștergem tutorele
            tutoreRepository.delete(tutore);
            
            redirectAttributes.addFlashAttribute("successMessage", 
                "Tutorele " + tutore.getPrenume() + " " + tutore.getNume() + " a fost șters cu succes!");
                
        } catch (Exception e) {
            e.printStackTrace();
            redirectAttributes.addFlashAttribute("errorMessage", 
                "Eroare la ștergerea tutorelui: " + e.getMessage());
        }
        
        return "redirect:/prodecan/management/tutori";
    }
}