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
@RequestMapping("/prodecan/partners")
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
    @GetMapping("")
    public String listPartners(Model model, Authentication authentication) {
        User user = (User) authentication.getPrincipal();
        model.addAttribute("user", user);
        
        List<Partner> partners = partnerRepository.findAll();
        model.addAttribute("partners", partners);
        
        return "prodecan/partners-list";
    }
    
    // Show create partner form
    @GetMapping("/create")
    public String showCreateForm(Model model, Authentication authentication) {
        User user = (User) authentication.getPrincipal();
        model.addAttribute("user", user);
        
        model.addAttribute("partner", new Partner());
        model.addAttribute("companii", companieRepository.findAll());
        
        return "prodecan/partner-form";
    }
    
    // Create a new partner
    @PostMapping("/create")
    public String createPartner(@ModelAttribute Partner partner, RedirectAttributes redirectAttributes) {
        try {
            // Save the partner
            Companie companie = companieRepository.findById(partner.getCompanie().getId());
            partner.setCompanie(companie);
            partnerRepository.save(partner);
            
            // Create user account
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
                
            return "redirect:/prodecan/partners";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", 
                "Eroare la crearea partenerului: " + e.getMessage());
            return "redirect:/prodecan/partners/create";
        }
    }
    
    // Show edit partner form
    @GetMapping("/edit/{id}")
    public String showEditForm(@PathVariable int id, Model model, Authentication authentication) {
        User user = (User) authentication.getPrincipal();
        model.addAttribute("user", user);
        
        Partner partner = partnerRepository.findById(id);
        if (partner == null) {
            return "redirect:/prodecan/partners";
        }
        
        model.addAttribute("partner", partner);
        model.addAttribute("companii", companieRepository.findAll());
        
        return "prodecan/partner-form";
    }
    
    // Update partner
    @PostMapping("/edit/{id}")
    public String updatePartner(@PathVariable int id, @ModelAttribute Partner partner, RedirectAttributes redirectAttributes) {
        try {
            Partner existingPartner = partnerRepository.findById(id);
            if (existingPartner == null) {
                redirectAttributes.addFlashAttribute("errorMessage", "Partener negăsit!");
                return "redirect:/prodecan/partners";
            }
            
            // Save original email
            String originalEmail = existingPartner.getEmail();
            
            // Update partner fields
            existingPartner.setNume(partner.getNume());
            existingPartner.setPrenume(partner.getPrenume());
            existingPartner.setFunctie(partner.getFunctie());
            existingPartner.setTelefon(partner.getTelefon());
            
         // Update company if changed
            if (existingPartner.getCompanie().getId() != partner.getCompanie().getId()) {
                Companie companie = companieRepository.findById(partner.getCompanie().getId());
                existingPartner.setCompanie(companie);
            }
            partnerRepository.save(existingPartner);
            
            // Update user account if exists
            User userPartner = userRepository.findByEmail(originalEmail);
            if (userPartner != null) {
                userPartner.setNume(partner.getNume());
                userPartner.setPrenume(partner.getPrenume());
                userRepository.save(userPartner);
            }
            
            redirectAttributes.addFlashAttribute("successMessage", "Partener actualizat cu succes!");
            return "redirect:/prodecan/partners";
            
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", 
                "Eroare la actualizarea partenerului: " + e.getMessage());
            return "redirect:/prodecan/partners/edit/" + id;
        }
    }
    
    // Delete partner
    @PostMapping("/delete/{id}")
    public String deletePartner(@PathVariable int id, RedirectAttributes redirectAttributes) {
        try {
            Partner partner = partnerRepository.findById(id);
            if (partner == null) {
                redirectAttributes.addFlashAttribute("errorMessage", "Partener negăsit!");
                return "redirect:/prodecan/partners";
            }
            
            // Delete user account if exists
            User userPartner = userRepository.findByEmail(partner.getEmail());
            if (userPartner != null) {
                userRepository.delete(userPartner);
            }
            
            // Delete partner
            partnerRepository.delete(partner);
            
            redirectAttributes.addFlashAttribute("successMessage", "Partener șters cu succes!");
            
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", 
                "Eroare la ștergerea partenerului: " + e.getMessage());
        }
        
        return "redirect:/prodecan/partners";
    }
    
    // Reset partner password
    @PostMapping("/reset-password/{id}")
    public String resetPassword(@PathVariable int id, RedirectAttributes redirectAttributes) {
        try {
            Partner partner = partnerRepository.findById(id);
            if (partner == null) {
                redirectAttributes.addFlashAttribute("errorMessage", "Partener negăsit!");
                return "redirect:/prodecan/partners";
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
        
        return "redirect:/prodecan/partners";
    }
}