package ro.upt.ac.conventii.security;

import java.util.Collection;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class HomeController {
    
    @GetMapping("/")
    public String home(Authentication authentication) {
        if (authentication != null) {
            Collection<? extends GrantedAuthority> authorities = authentication.getAuthorities();
            String role = authorities.iterator().next().getAuthority();
            
            switch (role) {
                case "ROLE_PRODECAN":
                    return "redirect:/prodecan/dashboard";
                case "ROLE_STUDENT":
                    return "redirect:/student/dashboard";
                case "ROLE_PRORECTOR":
                    return "redirect:/prorector/dashboard";
                case "ROLE_PARTNER":
                    return "redirect:/partner/dashboard";
                default:
                    return "redirect:/login";
            }
        }
        return "redirect:/login";
    }
}