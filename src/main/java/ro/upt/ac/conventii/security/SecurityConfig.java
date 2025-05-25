package ro.upt.ac.conventii.security;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .csrf(csrf -> csrf.disable())
            .authorizeHttpRequests(auth -> auth
                // Adăugăm pagina de resetare a parolei la lista de URL-uri permise fără autentificare
                .requestMatchers("/login", "/change-password", "/reset-password", "/css/**", "/js/**", "/images/**").permitAll()
                .requestMatchers("/prodecan/**").hasRole("PRODECAN")
                .requestMatchers("/prorector/**").hasRole("PRORECTOR")
                .requestMatchers("/student/**").hasRole("STUDENT")
                .requestMatchers("/reprezentant/**").hasRole("REPREZENTANT")
                .requestMatchers("/tutore/**").hasRole("TUTORE")
                .anyRequest().authenticated()
            )
            .formLogin(form -> form
                .loginPage("/login")
                .successHandler(authenticationSuccessHandler())
                .permitAll()
            )
            .logout(logout -> logout
                .logoutRequestMatcher(new AntPathRequestMatcher("/logout"))
                .logoutSuccessUrl("/login?logout")
                .permitAll()
            );
        
        return http.build();
    }

    @Bean
    public AuthenticationSuccessHandler authenticationSuccessHandler() {
        return new CustomAuthenticationSuccessHandler();
    }

    @Bean
    public BCryptPasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}