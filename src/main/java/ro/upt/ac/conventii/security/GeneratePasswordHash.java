package ro.upt.ac.conventii.security;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

public class GeneratePasswordHash {
    public static void main(String[] args) {
        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
        String password = "123456";
        String encodedPassword = encoder.encode(password);
        System.out.println("Hash pentru parola " + password + ": " + encodedPassword);
        
        // Verifică dacă hash-ul funcționează
        boolean matches = encoder.matches(password, encodedPassword);
        System.out.println("Parola se potrivește: " + matches);
        
        // Verifică și hash-ul existent
        String existingHash = "$2a$10$WstMkAgkK4K17wQIOsZlPOYfBMylbMG8Ak6B0AKHPVqM4QBOVtfce";
        boolean matchesExisting = encoder.matches(password, existingHash);
        System.out.println("Parola se potrivește cu hash-ul existent: " + matchesExisting);
    }
}