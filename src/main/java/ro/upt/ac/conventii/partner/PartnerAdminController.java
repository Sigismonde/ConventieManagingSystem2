package ro.upt.ac.conventii.partner;

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
public class PartnerAdminController {

    @Autowired
    private PartnerRepository partnerRepository;
    
    @Autowired
    private CompanieRepository companieRepository;
    
    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private BCryptPasswordEncoder passwordEncoder;
    
    @Autowired
    private PasswordGeneratorService passwordGeneratorService;
    
    // List all partners
    @GetMapping("/partners")
    public String listPartners(Model model, Authentication authentication) {
        User user = (User) authentication.getPrincipal();
        model.addAttribute("user", user);
        
        List<Partner> partners = partnerRepository.findAll();
        model.addAttribute("partners", partners);
        
        return "prodecan/management/partners";
    }
    
    // Show create partner form
    @GetMapping("/create")
    public String showCreateForm(Model model, Authentication authentication) {
        User user = (User) authentication.getPrincipal();
        model.addAttribute("user", user);
        
        model.addAttribute("partner", new Partner());
        model.addAttribute("companii", companieRepository.findAll());
        
        return "prodecan/management/create";
    }
    
    // Create a new partner
    @PostMapping("/partners/create")
    public String createPartner(@ModelAttribute Partner partner, RedirectAttributes redirectAttributes) {
        try {
            System.out.println("Se încearcă crearea partenerului: " + partner.getEmail());
            
            // Verificăm dacă email-ul este valid
            if (partner.getEmail() == null || partner.getEmail().trim().isEmpty()) {
                redirectAttributes.addFlashAttribute("errorMessage", 
                    "Email-ul este obligatoriu!");
                return "redirect:/prodecan/management/create";
            }
            
            // Verificăm dacă există deja un partener cu acest email
            if (partnerRepository.findByEmail(partner.getEmail()).isPresent()) {
                redirectAttributes.addFlashAttribute("errorMessage", 
                    "Există deja un partener cu această adresă de email!");
                return "redirect:/prodecan/management/create";
            }
            
            // Verificăm dacă compania există
            Companie companie = companieRepository.findById(partner.getCompanie().getId());
            if (companie == null) {
                redirectAttributes.addFlashAttribute("errorMessage", 
                    "Compania selectată nu există!");
                return "redirect:/prodecan/management/create";
            }
            
            // Setăm compania corect
            partner.setCompanie(companie);
            
            // Salvăm partenerul
            Partner savedPartner = partnerRepository.save(partner);
            System.out.println("Partener salvat cu ID: " + savedPartner.getId());
            
            // Verificăm dacă există deja un cont de utilizator
            User existingUser = userRepository.findByEmail(partner.getEmail());
            if (existingUser != null) {
                redirectAttributes.addFlashAttribute("errorMessage", 
                    "Există deja un utilizator cu această adresă de email!");
                return "redirect:/prodecan/management/partners";
            }
            
            // Creăm contul de utilizator
            String temporaryPassword = passwordGeneratorService.generateRandomPassword();
            User userPartner = new User();
            userPartner.setEmail(partner.getEmail());
            userPartner.setNume(partner.getNume());
            userPartner.setPrenume(partner.getPrenume());
            userPartner.setPassword(passwordEncoder.encode(temporaryPassword));
            userPartner.setRole("ROLE_PARTNER");
            userPartner.setEnabled(true);
            userPartner.setFirstLogin(true);
            
            userRepository.save(userPartner);
            
            redirectAttributes.addFlashAttribute("successMessage", 
                "Partener creat cu succes!\n" +
                "----------------------------------------\n" +
                "Email: " + partner.getEmail() + "\n" +
                "PAROLA TEMPORARĂ: " + temporaryPassword + "\n" +
                "----------------------------------------\n" +
                "IMPORTANT: Salvați această parolă!");
                
            return "redirect:/prodecan/management/partners";
        } catch (Exception e) {
            System.err.println("Eroare la crearea partenerului: " + e.getMessage());
            e.printStackTrace(); // Afișăm stack trace-ul complet pentru debugging
            redirectAttributes.addFlashAttribute("errorMessage", 
                "Eroare la crearea partenerului: " + e.getMessage());
            return "redirect:/prodecan/management/create";
        }
    }
    
    // Restul metodelor rămâne neschimbat, dar trebuie actualizate URL-urile
    @GetMapping("/partners/edit/{id}")
    public String showEditForm(@PathVariable int id, Model model, Authentication authentication) {
        User user = (User) authentication.getPrincipal();
        model.addAttribute("user", user);
        
        Partner partner = partnerRepository.findById(id);
        if (partner == null) {
            return "redirect:/prodecan/management/partners";
        }
        
        model.addAttribute("partner", partner);
        model.addAttribute("companii", companieRepository.findAll());
        
        return "prodecan/management/partner-form";
    }
    
    @PostMapping("/partners/edit/{id}")
    public String updatePartner(@PathVariable int id, @ModelAttribute Partner partner, RedirectAttributes redirectAttributes) {
        try {
            Partner existingPartner = partnerRepository.findById(id);
            if (existingPartner == null) {
                redirectAttributes.addFlashAttribute("errorMessage", "Partener negăsit!");
                return "redirect:/prodecan/management/partners";
            }
            
            // Salvăm email-ul original
            String originalEmail = existingPartner.getEmail();
            
            // Actualizăm câmpurile partenerului
            existingPartner.setNume(partner.getNume());
            existingPartner.setPrenume(partner.getPrenume());
            existingPartner.setFunctie(partner.getFunctie());
            existingPartner.setTelefon(partner.getTelefon());
            
         // Actualizăm compania dacă s-a schimbat
            if (existingPartner.getCompanie().getId() != partner.getCompanie().getId()) {
                Companie companie = companieRepository.findById(partner.getCompanie().getId());
                if (companie == null) {
                    redirectAttributes.addFlashAttribute("errorMessage", "Compania selectată nu există!");
                    return "redirect:/prodecan/management/partners/edit/" + id;
                }
                existingPartner.setCompanie(companie);
            }
            
            partnerRepository.save(existingPartner);
            
            // Actualizăm contul de utilizator dacă există
            User userPartner = userRepository.findByEmail(originalEmail);
            if (userPartner != null) {
                userPartner.setNume(partner.getNume());
                userPartner.setPrenume(partner.getPrenume());
                userRepository.save(userPartner);
            }
            
            redirectAttributes.addFlashAttribute("successMessage", "Partener actualizat cu succes!");
            return "redirect:/prodecan/management/partners";
            
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", 
                "Eroare la actualizarea partenerului: " + e.getMessage());
            return "redirect:/prodecan/management/partners/edit/" + id;
        }
    }
    
    @PostMapping("/partners/delete/{id}")
    public String deletePartner(@PathVariable int id, RedirectAttributes redirectAttributes) {
        try {
            Partner partner = partnerRepository.findById(id);
            if (partner == null) {
                redirectAttributes.addFlashAttribute("errorMessage", "Partener negăsit!");
                return "redirect:/prodecan/management/partners";
            }
            
            // Ștergem contul de utilizator dacă există
            User userPartner = userRepository.findByEmail(partner.getEmail());
            if (userPartner != null) {
                userRepository.delete(userPartner);
            }
            
            // Ștergem partenerul
            partnerRepository.delete(partner);
            
            redirectAttributes.addFlashAttribute("successMessage", "Partener șters cu succes!");
            
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", 
                "Eroare la ștergerea partenerului: " + e.getMessage());
        }
        
        return "redirect:/prodecan/management/partners";
    }
    
    @PostMapping("/partners/reset-password/{id}")
    public String resetPassword(@PathVariable int id, RedirectAttributes redirectAttributes) {
        try {
            Partner partner = partnerRepository.findById(id);
            if (partner == null) {
                redirectAttributes.addFlashAttribute("errorMessage", "Partener negăsit!");
                return "redirect:/prodecan/management/partners";
            }
            
            User userPartner = userRepository.findByEmail(partner.getEmail());
            if (userPartner != null) {
                String newPassword = passwordGeneratorService.generateRandomPassword();
                userPartner.setPassword(passwordEncoder.encode(newPassword));
                userPartner.setFirstLogin(true);
                userRepository.save(userPartner);
                
                redirectAttributes.addFlashAttribute("successMessage", 
                    "Parolă resetată cu succes!\n" +
                    "----------------------------------------\n" +
                    "Partener: " + partner.getNume() + " " + partner.getPrenume() + "\n" +
                    "Email: " + partner.getEmail() + "\n" +
                    "NOUA PAROLĂ: " + newPassword + "\n" +
                    "----------------------------------------\n" +
                    "IMPORTANT: Salvați această parolă!");
            } else {
                redirectAttributes.addFlashAttribute("errorMessage", 
                    "Nu există un cont de utilizator pentru acest partener!");
            }
            
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", 
                "Eroare la resetarea parolei: " + e.getMessage());
        }
        
        return "redirect:/prodecan/management/partners";
    }
}