package ro.upt.ac.conventii.reprezentant;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ro.upt.ac.conventii.companie.Companie;
import ro.upt.ac.conventii.companie.CompanieRepository;
import ro.upt.ac.conventii.security.User;
import ro.upt.ac.conventii.security.UserRepository;
import ro.upt.ac.conventii.service.PasswordGeneratorService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import jakarta.persistence.EntityNotFoundException;

@Service
public class ReprezentantService {
    
    @Autowired
    private ReprezentantRepository reprezentantRepository;
    
    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private CompanieRepository companieRepository;
    
    @Autowired
    private PasswordGeneratorService passwordGeneratorService;
    
    @Autowired
    private BCryptPasswordEncoder passwordEncoder;
    
    @Transactional
    public ReprezentantCreationResult createReprezentantWithUser(Reprezentant reprezentant) {
        try {
            // 1. Verificăm și încărcăm compania
            Companie companie = companieRepository.findById(reprezentant.getCompanie().getId())
                .orElseThrow(() -> new EntityNotFoundException("Compania cu ID " + 
                    reprezentant.getCompanie().getId() + " nu a fost găsită"));
            reprezentant.setCompanie(companie);
            
            // 2. Salvăm reprezentantul
            Reprezentant savedReprezentant = reprezentantRepository.save(reprezentant);
            
            // Forțăm flush pentru a ne asigura că datele sunt scrise în baza de date
            reprezentantRepository.flush();
            
            // 3. Creăm contul de utilizator
            String temporaryPassword = passwordGeneratorService.generateRandomPassword();
            User userReprezentant = new User();
            userReprezentant.setEmail(reprezentant.getEmail());
            userReprezentant.setNume(reprezentant.getNume());
            userReprezentant.setPrenume(reprezentant.getPrenume());
            userReprezentant.setPassword(passwordEncoder.encode(temporaryPassword));
            userReprezentant.setRole("ROLE_REPREZENTANT");
            userReprezentant.setEnabled(true);
            userReprezentant.setFirstLogin(true);
            
            userRepository.save(userReprezentant);
            
            // 4. Verificăm că reprezentantul a fost salvat
            Reprezentant verificationReprezentant = reprezentantRepository.findById(savedReprezentant.getId());
            if (verificationReprezentant == null) {
                throw new RuntimeException("Reprezentantul nu a fost salvat corect în baza de date!");
            }
            
            return new ReprezentantCreationResult(savedReprezentant, temporaryPassword);
            
        } catch (Exception e) {
            // În caz de eroare, tranzacția va face rollback automat
            throw new RuntimeException("Eroare la crearea reprezentantului: " + e.getMessage(), e);
        }
    }
    
    // Clasă internă pentru returnarea rezultatului
    public static class ReprezentantCreationResult {
        private final Reprezentant reprezentant;
        private final String temporaryPassword;
        
        public ReprezentantCreationResult(Reprezentant reprezentant, String temporaryPassword) {
            this.reprezentant = reprezentant;
            this.temporaryPassword = temporaryPassword;
        }
        
        public Reprezentant getReprezentant() {
            return reprezentant;
        }
        
        public String getTemporaryPassword() {
            return temporaryPassword;
        }
    }
}