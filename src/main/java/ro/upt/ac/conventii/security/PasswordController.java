package ro.upt.ac.conventii.security;

import java.security.Principal;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
public class PasswordController {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private BCryptPasswordEncoder passwordEncoder;

    @GetMapping("/change-password")
    public String showChangePasswordForm(Model model) {
        return "change-password";
    }

    @PostMapping("/change-password")
    public String changePassword(@RequestParam("currentPassword") String currentPassword,
                               @RequestParam("newPassword") String newPassword,
                               @RequestParam("confirmPassword") String confirmPassword,
                               Principal principal,
                               RedirectAttributes redirectAttributes) {
        
        User user = userRepository.findByEmail(principal.getName());
        
        // Verificăm parola curentă
        if (!passwordEncoder.matches(currentPassword, user.getPassword())) {
            redirectAttributes.addFlashAttribute("error", "Parola curentă este incorectă!");
            return "redirect:/change-password";
        }
        
        // Verificăm dacă parolele noi se potrivesc
        if (!newPassword.equals(confirmPassword)) {
            redirectAttributes.addFlashAttribute("error", "Parolele nu se potrivesc!");
            return "redirect:/change-password";
        }
        
        // Actualizăm parola și marcăm că nu mai e prima logare
        user.setPassword(passwordEncoder.encode(newPassword));
        user.setFirstLogin(false);
        userRepository.save(user);
        
        redirectAttributes.addFlashAttribute("success", "Parola a fost schimbată cu succes!");
        
        return "redirect:/";
    }
}