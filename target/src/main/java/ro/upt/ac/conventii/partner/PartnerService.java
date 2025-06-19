package ro.upt.ac.conventii.partner;

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
public class PartnerService {
	
	
    @Autowired
    private PartnerRepository partnerRepository;
    
    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private CompanieRepository companieRepository;
    
    @Autowired
    private PasswordGeneratorService passwordGeneratorService;
    
    @Autowired
    private BCryptPasswordEncoder passwordEncoder;
    
    @Transactional
    public PartnerCreationResult createPartnerWithUser(Partner partner) {
        try {
            // 1. Verificăm și încărcăm compania
            Companie companie = companieRepository.findById(partner.getCompanie().getId()).orElseThrow(() -> new EntityNotFoundException("Compania cu ID " + 
                    partner.getCompanie().getId() + " nu a fost găsită"));;
            if (companie == null) {
                throw new RuntimeException("Compania cu ID " + partner.getCompanie().getId() + " nu a fost găsită!");
            }
            partner.setCompanie(companie);
            
            // 2. Salvăm partenerul
            Partner savedPartner = partnerRepository.save(partner);
            
            // Forțăm flush pentru a ne asigura că datele sunt scrise în baza de date
            partnerRepository.flush();
            
            // 3. Creăm contul de utilizator
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
            
            // 4. Verificăm că partenerul a fost salvat
            Partner verificationPartner = partnerRepository.findById(savedPartner.getId());
            if (verificationPartner == null) {
                throw new RuntimeException("Partenerul nu a fost salvat corect în baza de date!");
            }
            
            return new PartnerCreationResult(savedPartner, temporaryPassword);
            
        } catch (Exception e) {
            // În caz de eroare, tranzacția va face rollback automat
            throw new RuntimeException("Eroare la crearea partenerului: " + e.getMessage(), e);
        }
    }
    
    // Clasă internă pentru returnarea rezultatului
    public static class PartnerCreationResult {
        private final Partner partner;
        private final String temporaryPassword;
        
        public PartnerCreationResult(Partner partner, String temporaryPassword) {
            this.partner = partner;
            this.temporaryPassword = temporaryPassword;
        }
        
        public Partner getPartner() {
            return partner;
        }
        
        public String getTemporaryPassword() {
            return temporaryPassword;
        }
    }
}