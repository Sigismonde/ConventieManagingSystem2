package ro.upt.ac.conventii.security;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
public class CustomAuthenticationSuccessHandler implements AuthenticationSuccessHandler {
    
    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, 
                                      HttpServletResponse response, 
                                      Authentication authentication) throws IOException {
        User user = (User) authentication.getPrincipal();
        
        if (user.isFirstLogin()) {
            response.sendRedirect("/change-password");
        } else {
            // Redirecționare bazată pe rol
            switch (user.getRole()) {
                case "ROLE_STUDENT":
                    response.sendRedirect("/student/dashboard");
                    break;
                case "ROLE_PRODECAN":
                    response.sendRedirect("/prodecan/dashboard");
                    break;
                case "ROLE_PRORECTOR":
                    response.sendRedirect("/prorector/dashboard");
                    break;
                case "ROLE_REPREZENTANT":
                    response.sendRedirect("/reprezentant/dashboard");
                    break;
                case "ROLE_TUTORE":
                    response.sendRedirect("/tutore/dashboard");
                    break;
                default:
                    response.sendRedirect("/");
                    break;
            }
        }
    }
}