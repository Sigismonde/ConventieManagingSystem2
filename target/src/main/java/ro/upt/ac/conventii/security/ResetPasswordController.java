package ro.upt.ac.conventii.security;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
public class ResetPasswordController {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private BCryptPasswordEncoder passwordEncoder;

    @GetMapping("/reset-password")
    public String showResetPasswordForm() {
        return "reset-password";
    }

    @PostMapping("/reset-password")
    public String resetPassword(@RequestParam("email") String email,
                                @RequestParam("newPassword") String newPassword,
                                @RequestParam("confirmPassword") String confirmPassword,
                                @RequestParam(value = "resetCode", required = false, defaultValue = "UPT2025") String resetCode,
                                RedirectAttributes redirectAttributes) {
        
        // Codul de resetare static, poate fi schimbat cu un cod mai complex sau generat dinamic
        final String ADMIN_RESET_CODE = "UPT2025";
        
        // Verificăm codul de resetare (opțional, pentru securitate suplimentară)
        if (!resetCode.equals(ADMIN_RESET_CODE)) {
            redirectAttributes.addFlashAttribute("error", "Codul de resetare este incorect!");
            return "redirect:/reset-password";
        }
        
        // Verificăm dacă utilizatorul există
        User user = userRepository.findByEmail(email);
        if (user == null) {
            redirectAttributes.addFlashAttribute("error", "Nu există niciun utilizator cu acest email!");
            return "redirect:/reset-password";
        }
        
        // Verificăm dacă parolele noi se potrivesc
        if (!newPassword.equals(confirmPassword)) {
            redirectAttributes.addFlashAttribute("error", "Parolele introduse nu coincid!");
            return "redirect:/reset-password";
        }
        
        // Actualizăm parola
        user.setPassword(passwordEncoder.encode(newPassword));
        user.setFirstLogin(false); // Marcăm că nu mai este prima logare
        userRepository.save(user);
        
        redirectAttributes.addFlashAttribute("success", "Parola a fost resetată cu succes! Acum vă puteți autentifica cu noua parolă.");
        
        return "redirect:/login";
    }
}