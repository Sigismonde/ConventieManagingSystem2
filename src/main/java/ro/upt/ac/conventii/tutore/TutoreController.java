package ro.upt.ac.conventii.tutore;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import ro.upt.ac.conventii.companie.Companie;
import ro.upt.ac.conventii.conventie.Conventie;
import ro.upt.ac.conventii.conventie.ConventieRepository;
import ro.upt.ac.conventii.conventie.ConventieStatus;
import ro.upt.ac.conventii.security.User;
import ro.upt.ac.conventii.security.UserRepository;

@Controller
@RequestMapping("/tutore")
public class TutoreController {

    @Autowired
    private TutoreRepository tutoreRepository;
    
    @Autowired
    private ConventieRepository conventieRepository;
    
    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private BCryptPasswordEncoder passwordEncoder;
    
    // Dashboard endpoint
    @GetMapping("/dashboard")
    public String dashboard(Authentication authentication, Model model) {
        User user = (User) authentication.getPrincipal();
        Tutore tutore = tutoreRepository.findByEmail(user.getEmail())
                .orElseThrow(() -> new RuntimeException("Tutore not found"));
        
        model.addAttribute("user", user);
        model.addAttribute("tutore", tutore);
        
        // Get company info
        Companie companie = tutore.getCompanie();
        model.addAttribute("companie", companie);
        
        // Get pending conventions
        List<Conventie> conventiiTrimise = conventieRepository.findByStatusAndCompanieId(
                ConventieStatus.TRIMISA_TUTORE, companie.getId());
        model.addAttribute("conventiiTrimise", conventiiTrimise);
        
        // Get recently approved conventions (top 5)
        Pageable topFive = PageRequest.of(0, 5);
        List<Conventie> conventiiAprobate = conventieRepository.findTop5ByStatusAndCompanieIdOrderByDataIntocmiriiDesc(
                ConventieStatus.APROBATA_TUTORE, companie.getId(), topFive);
        model.addAttribute("conventiiAprobate", conventiiAprobate != null ? conventiiAprobate : new ArrayList<>());
        
        return "tutore/dashboard";
    }
    
    // List all conventions
    @GetMapping("/conventii")
    public String conventii(Authentication authentication, Model model) {
        User user = (User) authentication.getPrincipal();
        Tutore tutore = tutoreRepository.findByEmail(user.getEmail())
                .orElseThrow(() -> new RuntimeException("Tutore not found"));
        
        model.addAttribute("user", user);
        model.addAttribute("tutore", tutore);
        
        // Get company info
        Companie companie = tutore.getCompanie();
        
        // Get all conventions for this company that are either sent to tutor or approved by tutor
        List<Conventie> conventii = conventieRepository.findByStatusInAndCompanieId(
                List.of(ConventieStatus.TRIMISA_TUTORE, ConventieStatus.APROBATA_TUTORE, ConventieStatus.APROBATA),
                companie.getId());
        model.addAttribute("conventii", conventii);
        
        return "tutore/conventii";
    }
    
    // Approve convention
    @PostMapping("/conventie/aproba/{id}")
    public String aprobaConventie(@PathVariable("id") int id, 
                                 Authentication authentication, 
                                 RedirectAttributes redirectAttributes) {
        try {
            User user = (User) authentication.getPrincipal();
            Tutore tutore = tutoreRepository.findByEmail(user.getEmail())
                    .orElseThrow(() -> new RuntimeException("Tutore not found"));
            
            // Verificăm dacă tutorele are semnătură încărcată
            if (tutore.getSemnatura() == null) {
                redirectAttributes.addFlashAttribute("errorMessage", 
                    "Nu puteți aproba convenția fără o semnătură încărcată. Vă rugăm să încărcați mai întâi semnătura în panoul de control.");
                return "redirect:/tutore/conventii";
            }
            
            Conventie conventie = conventieRepository.findById(id);
            
            if (conventie == null) {
                redirectAttributes.addFlashAttribute("errorMessage", 
                    "Convenția nu a fost găsită!");
                return "redirect:/tutore/conventii";
            }
            
            // Check if convention belongs to tutore's company
            if (conventie.getCompanie().getId() != tutore.getCompanie().getId()) {
                redirectAttributes.addFlashAttribute("errorMessage", 
                    "Nu aveți permisiunea să aprobați această convenție!");
                return "redirect:/tutore/conventii";
            }
            
            // Check if convention is in the right status
            if (conventie.getStatus() != ConventieStatus.TRIMISA_TUTORE) {
                redirectAttributes.addFlashAttribute("errorMessage", 
                    "Această convenție nu este în stare de trimitere către tutore!");
                return "redirect:/tutore/conventii";
            }
            
            // Update status
            conventie.setStatus(ConventieStatus.APROBATA_TUTORE);
            conventieRepository.save(conventie);
            
            redirectAttributes.addFlashAttribute("successMessage", 
                "Convenția a fost aprobată cu succes și semnată digital!");
                
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", 
                "Eroare la aprobarea convenției: " + e.getMessage());
        }
        
        return "redirect:/tutore/conventii";
    }
    
    // Reject convention
    @PostMapping("/conventie/respinge/{id}")
    public String respingeConventie(@PathVariable("id") int id, 
                                   Authentication authentication, 
                                   RedirectAttributes redirectAttributes) {
        try {
            User user = (User) authentication.getPrincipal();
            Tutore tutore = tutoreRepository.findByEmail(user.getEmail())
                    .orElseThrow(() -> new RuntimeException("Tutore not found"));
            
            Conventie conventie = conventieRepository.findById(id);
            
            if (conventie == null) {
                redirectAttributes.addFlashAttribute("errorMessage", 
                    "Convenția nu a fost găsită!");
                return "redirect:/tutore/conventii";
            }
            
            // Check if convention belongs to tutore's company
            if (conventie.getCompanie().getId() != tutore.getCompanie().getId()) {
                redirectAttributes.addFlashAttribute("errorMessage", 
                    "Nu aveți permisiunea să respingeți această convenție!");
                return "redirect:/tutore/conventii";
            }
            
            // Update status
            conventie.setStatus(ConventieStatus.RESPINSA);
            conventieRepository.save(conventie);
            
            redirectAttributes.addFlashAttribute("successMessage", 
                "Convenția a fost respinsă.");
                
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", 
                "Eroare la respingerea convenției: " + e.getMessage());
        }
        
        return "redirect:/tutore/conventii";
    }
    
    // View convention details
    @GetMapping("/conventie-view/{id}")
    public String viewConventie(@PathVariable("id") int id, 
                              Authentication authentication, 
                              Model model) {
        User user = (User) authentication.getPrincipal();
        Tutore tutore = tutoreRepository.findByEmail(user.getEmail())
                .orElseThrow(() -> new RuntimeException("Tutore not found"));
        
        Conventie conventie = conventieRepository.findById(id);
        
        if (conventie == null || conventie.getCompanie().getId() != tutore.getCompanie().getId()) {
            return "redirect:/tutore/conventii";
        }
        
        model.addAttribute("user", user);
        model.addAttribute("tutore", tutore);
        model.addAttribute("conventie", conventie);
        
        return "tutore/conventie-view";
    }
    
    // Upload signature
    @PostMapping("/upload-semnatura")
    public String uploadSemnatura(@RequestParam("semnatura") MultipartFile file, 
                                Authentication authentication,
                                RedirectAttributes redirectAttributes) {
        try {
            User user = (User) authentication.getPrincipal();
            Tutore tutore = tutoreRepository.findByEmail(user.getEmail())
                    .orElseThrow(() -> new RuntimeException("Tutore not found"));
            
            // Verificăm dacă fișierul este imagine
            if (!file.getContentType().startsWith("image/")) {
                redirectAttributes.addFlashAttribute("errorMessage", 
                    "Vă rugăm să încărcați doar fișiere imagine (.jpg, .png).");
                return "redirect:/tutore/dashboard";
            }

            // Salvăm semnătura în obiectul Tutore
            tutore.setSemnatura(file.getBytes());
            
            // Salvăm în baza de date
            tutoreRepository.save(tutore);
            
            redirectAttributes.addFlashAttribute("successMessage", 
                "Semnătura a fost încărcată cu succes!");
            
        } catch (IOException e) {
            redirectAttributes.addFlashAttribute("errorMessage", 
                "Eroare la încărcarea semnăturii: " + e.getMessage());
        }
        
        return "redirect:/tutore/dashboard";
    }
    
    // Reset password
    @PostMapping("/reset-password")
    public String resetPassword(Authentication authentication, 
                              @RequestParam("currentPassword") String currentPassword,
                              @RequestParam("newPassword") String newPassword,
                              @RequestParam("confirmPassword") String confirmPassword,
                              RedirectAttributes redirectAttributes) {
        
        try {
            User user = (User) authentication.getPrincipal();
            
            // Verificăm parola curentă
            if (!passwordEncoder.matches(currentPassword, user.getPassword())) {
                redirectAttributes.addFlashAttribute("errorMessage", 
                    "Parola curentă este incorectă!");
                return "redirect:/tutore/dashboard";
            }
            
            // Verificăm că parolele noi coincid
            if (!newPassword.equals(confirmPassword)) {
                redirectAttributes.addFlashAttribute("errorMessage", 
                    "Noua parolă și confirmarea ei nu coincid!");
                return "redirect:/tutore/dashboard";
            }
            
            // Actualizăm parola
            user.setPassword(passwordEncoder.encode(newPassword));
            user.setFirstLogin(false);
            userRepository.save(user);
            
            redirectAttributes.addFlashAttribute("successMessage", 
                "Parola a fost schimbată cu succes!");
            
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", 
                "Eroare la schimbarea parolei: " + e.getMessage());
        }
        
        return "redirect:/tutore/dashboard";
    }
    
    // Export convention as HTML
    @GetMapping("/conventie-export/{id}")
    public ResponseEntity<String> exportConventie(@PathVariable("id") int id, Authentication authentication) {
        User user = (User) authentication.getPrincipal();
        Tutore tutore = tutoreRepository.findByEmail(user.getEmail())
                .orElseThrow(() -> new RuntimeException("Tutore not found"));
        
        Conventie conventie = conventieRepository.findById(id);
        
        if (conventie == null || conventie.getCompanie().getId() != tutore.getCompanie().getId()) {
            return ResponseEntity.notFound().build();
        }

        String filename = String.format("conventie_%s_%s.html", 
            conventie.getStudent().getNume(),
            conventie.getCompanie().getNume());

        String htmlContent = generateConventieHtml(conventie, tutore);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.TEXT_HTML);
        headers.set(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + filename);
        headers.set(HttpHeaders.CONTENT_TYPE, "text/html; charset=UTF-8");

        return new ResponseEntity<>(htmlContent, headers, HttpStatus.OK);
    }
    
    // Export convention as PDF
    @GetMapping("/conventie-export-pdf/{id}")
    public ResponseEntity<byte[]> exportConventiePdf(@PathVariable("id") int id, Authentication authentication) {
        User user = (User) authentication.getPrincipal();
        Tutore tutore = tutoreRepository.findByEmail(user.getEmail())
                .orElseThrow(() -> new RuntimeException("Tutore not found"));
        
        Conventie conventie = conventieRepository.findById(id);
        
        if (conventie == null || conventie.getCompanie().getId() != tutore.getCompanie().getId()) {
            return ResponseEntity.notFound().build();
        }

        // Aici ar trebui să fie implementată generarea PDF-ului
        // Utilizând librării precum iText, similar cu implementarea din PartnerController
        
        // Pentru acest exemplu, vom returna un răspuns gol
        String filename = String.format("conventie_%s_%s.pdf", 
            conventie.getStudent().getNume(),
            conventie.getCompanie().getNume());

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_PDF);
        headers.setContentDispositionFormData("attachment", filename);

        // Implementare incompletă - necesită generare reală de PDF
        return new ResponseEntity<>(new byte[0], headers, HttpStatus.OK);
    }
    
    // Export convention as Word
    @GetMapping("/conventie-export-word/{id}")
    public ResponseEntity<byte[]> exportConventieWord(@PathVariable("id") int id, Authentication authentication) {
        User user = (User) authentication.getPrincipal();
        Tutore tutore = tutoreRepository.findByEmail(user.getEmail())
                .orElseThrow(() -> new RuntimeException("Tutore not found"));
        
        Conventie conventie = conventieRepository.findById(id);
        
        if (conventie == null || conventie.getCompanie().getId() != tutore.getCompanie().getId()) {
            return ResponseEntity.notFound().build();
        }

        // Aici ar trebui să fie implementată generarea Word-ului
        // Utilizând librării precum Apache POI, similar cu implementarea din PartnerController
        
        // Pentru acest exemplu, vom returna un răspuns gol
        String filename = String.format("conventie_%s_%s.docx", 
            conventie.getStudent().getNume(),
            conventie.getCompanie().getNume());

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);
        headers.setContentDispositionFormData("attachment", filename);

        // Implementare incompletă - necesită generare reală de Word
        return new ResponseEntity<>(new byte[0], headers, HttpStatus.OK);
    }
    
    private String generateConventieHtml(Conventie conventie, Tutore tutore) {
        // Aici ar trebui să fie implementată generarea HTML-ului
        // Similar cu implementarea din PartnerController sau StudentController
        
        // Pentru acest exemplu, vom returna un HTML simplu
        StringBuilder html = new StringBuilder();
        html.append("<!DOCTYPE html>\n")
            .append("<html>\n")
            .append("<head>\n")
            .append("<meta charset=\"UTF-8\">\n")
            .append("<title>Convenție de practică</title>\n")
            .append("</head>\n")
            .append("<body>\n")
            .append("<h1>Convenție de practică</h1>\n")
            .append("<p>Student: ").append(conventie.getStudent().getNumeComplet()).append("</p>\n")
            .append("<p>Companie: ").append(conventie.getCompanie().getNume()).append("</p>\n")
            .append("<p>Tutore: ").append(tutore.getNumeComplet()).append("</p>\n")
            .append("</body>\n")
            .append("</html>");
            
        return html.toString();
    }
}