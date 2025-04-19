// TutoreController.java
package ro.upt.ac.conventii.tutore;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
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

import java.text.SimpleDateFormat;

@Controller
@RequestMapping("/tutore")
public class TutoreController {

    @Autowired
    private TutoreRepository tutoreRepository;
    
    @Autowired
    private ConventieRepository conventieRepository;
    
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
    
    private String generateConventieHtml(Conventie conventie, Tutore tutore) {
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd.MM.yyyy");
        StringBuilder html = new StringBuilder();
        html.append("<!DOCTYPE html>\n")
            .append("<html>\n")
            .append("<head>\n")
            .append("<meta charset=\"UTF-8\">\n")
            .append("<title>Convenție de practică</title>\n")
            .append("<style>\n")
            .append("body { font-family: Arial, sans-serif; margin: 40px; line-height: 1.6; }\n")
            .append("h1, h2 { text-align: center; }\n")
            .append("h3 { margin-top: 20px; }\n")
            .append(".header { text-align: right; margin-bottom: 20px; }\n")
            .append(".content { margin: 20px 0; }\n")
            .append("table { width: 100%; border-collapse: collapse; margin: 20px 0; }\n")
            .append("table, th, td { border: 1px solid black; }\n")
            .append("th, td { padding: 8px; text-align: left; }\n")
            .append(".signature-table { border: none; }\n")
            .append(".signature-table td { border: none; text-align: center; padding: 20px; }\n")
            .append("</style>\n")
            .append("</head>\n")
            .append("<body>\n");

        // Adăugare conținut similar cu PartnerController.generateConventieHtml
        // ...

        // Adăugăm semnăturile - incluzând semnătura tutorelui în secțiunea corespunzătoare
        html.append("<table class='signature-table'>")
            .append("<tr>")
            .append("<th>Universitatea Politehnica Timișoara, prin Rector</th>")
            .append("<th>").append(conventie.getCompanie().getNume()).append("<br>")
            .append(conventie.getCompanie().getReprezentant()).append("</th>")
            .append("<th>Student<br>")
            .append(conventie.getStudent().getNume()).append(" ")
            .append(conventie.getStudent().getPrenume()).append("</th>")
            .append("</tr>")
            .append("<tr>")
            .append("<td>Conf. univ. dr. ing. Florin DRĂGAN<br><br>Semnătura: ____________<br>Data: ____________</td>");

        // Pentru partener, afișăm doar linie pentru semnătură
        html.append("<td>").append(conventie.getCompanie().getReprezentant())
            .append("<br><br>Semnătura: ____________<br>Data: ____________</td>");

        // Pentru student, afișăm doar linie pentru semnătură 
        html.append("<td>Semnătura: ____________<br>Data: ____________</td>")
            .append("</tr>")
            .append("</table>");

        // Am luat la cunoștință - aici e locul pentru semnătura tutorelui
        html.append("<p class='mt-4'>Am luat la cunoștință,</p>")
            .append("<table class='signature-table'>")
            .append("<tr>")
            .append("<td><strong>Cadru didactic supervizor</strong><br>")
            .append(conventie.getCadruDidactic().getNume()).append(" ")
            .append(conventie.getCadruDidactic().getPrenume()).append("<br>")
            .append("Funcția: ").append(conventie.getCadruDidactic().getFunctie()).append("<br><br>")
            .append("Semnătura: ____________<br>")
            .append("Data: ____________</td>")
            .append("<td><strong>Tutore</strong><br>")
            .append(conventie.getTutore().getNume()).append(" ")
            .append(conventie.getTutore().getPrenume()).append("<br>")
            .append("Funcția: ").append(conventie.getTutore().getFunctie()).append("<br><br>");

        // Afișăm semnătura tutorelui dacă convenția este APROBATA_TUTORE sau APROBATA
        if (conventie.getStatus() == ConventieStatus.APROBATA_TUTORE || 
            conventie.getStatus() == ConventieStatus.APROBATA) {
            html.append("Semnătura: ");
            
            if (tutore.getSemnatura() != null) {
                String base64Signature = Base64.getEncoder().encodeToString(tutore.getSemnatura());
                html.append("<img src='data:image/png;base64,").append(base64Signature).append("' style='max-width:150px; max-height:70px;'>");
            } else {
                html.append("____________");
            }
            
            html.append("<br>Data: ");
            if (conventie.getDataIntocmirii() != null) {
                html.append(dateFormat.format(conventie.getDataIntocmirii()));
            } else {
                html.append("____________");
            }
            html.append("</td>");
        } else {
            html.append("Semnătura: ____________<br>Data: ____________</td>");
        }

        html.append("</tr>")
            .append("</table>");

        html.append("</body></html>");
        return html.toString();
    }
}