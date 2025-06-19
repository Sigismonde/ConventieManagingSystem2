package ro.upt.ac.conventii.config;



import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.ui.Model;
import org.springframework.security.core.Authentication;
import java.time.LocalDate;
import ro.upt.ac.conventii.security.User;

@ControllerAdvice
public class GlobalControllerAdvice {

    @ModelAttribute
    public void addGlobalAttributes(Model model, Authentication authentication) {
        if (authentication != null && authentication.getPrincipal() instanceof User) {
            User user = (User) authentication.getPrincipal();
            model.addAttribute("user", user);
            
            // Putem adăuga și alte atribute globale aici dacă e nevoie
            model.addAttribute("currentYear", LocalDate.now().getYear());
        }
    }
}