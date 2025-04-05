package ro.upt.ac.conventii.partner;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.sql.Date;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
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

import com.lowagie.text.Document;
import com.lowagie.text.DocumentException;
import com.lowagie.text.pdf.PdfWriter;

import jakarta.servlet.http.HttpServletResponse;
import ro.upt.ac.conventii.companie.Companie;
import ro.upt.ac.conventii.companie.CompanieRepository;
import ro.upt.ac.conventii.conventie.Conventie;
import ro.upt.ac.conventii.conventie.ConventieRepository;
import ro.upt.ac.conventii.conventie.ConventieStatus;
import ro.upt.ac.conventii.security.User;
import ro.upt.ac.conventii.security.UserRepository;

@Controller
@RequestMapping("/partner")
public class PartnerController {

    @Autowired
    private PartnerRepository partnerRepository;
    
    @Autowired
    private ConventieRepository conventieRepository;
    
    @Autowired
    private CompanieRepository companieRepository;
    
    @Autowired
    private UserRepository userRepository;
    
    // Dashboard endpoint
    @GetMapping("/dashboard")
    public String dashboard(Authentication authentication, Model model) {
        User user = (User) authentication.getPrincipal();
        Partner partner = partnerRepository.findByEmail(user.getEmail())
                .orElseThrow(() -> new RuntimeException("Partner not found"));
        
        model.addAttribute("user", user);
        model.addAttribute("partner", partner);
        
        // Get company info
        Companie companie = partner.getCompanie();
        model.addAttribute("companie", companie);
        
        // Get pending conventions
        List<Conventie> conventiiInAsteptare = conventieRepository.findByCompanieAndStatus(
                companie, ConventieStatus.IN_ASTEPTARE);
        model.addAttribute("conventiiInAsteptare", conventiiInAsteptare);
        
        // Get recently approved conventions (top 5)
        Pageable topFive = PageRequest.of(0, 5);
        List<Conventie> conventiiAprobate = conventieRepository.findTop5ByCompanieAndStatusOrderByDataIntocmiriiDesc(
                companie, ConventieStatus.APROBATA_PARTENER, topFive);
        model.addAttribute("conventiiAprobate", conventiiAprobate != null ? conventiiAprobate : new ArrayList<>());
        
        return "partner/dashboard";
    }
    
    // List all conventions
    @GetMapping("/conventii")
    public String conventii(Authentication authentication, Model model) {
        User user = (User) authentication.getPrincipal();
        Partner partner = partnerRepository.findByEmail(user.getEmail())
                .orElseThrow(() -> new RuntimeException("Partner not found"));
        
        model.addAttribute("user", user);
        model.addAttribute("partner", partner);
        
        // Get company info
        Companie companie = partner.getCompanie();
        
        // Get all conventions for this company
        List<Conventie> conventii = conventieRepository.findByCompanie(companie);
        model.addAttribute("conventii", conventii);
        
        return "partner/conventii";
    }
    
    // Approve convention
    @PostMapping("/conventie/aproba/{id}")
    public String aprobaConventie(@PathVariable("id") int id, 
                                 Authentication authentication, 
                                 RedirectAttributes redirectAttributes) {
        try {
            User user = (User) authentication.getPrincipal();
            Partner partner = partnerRepository.findByEmail(user.getEmail())
                    .orElseThrow(() -> new RuntimeException("Partner not found"));
            
            Conventie conventie = conventieRepository.findById(id);
            
            if (conventie == null) {
                redirectAttributes.addFlashAttribute("errorMessage", 
                    "Convenția nu a fost găsită!");
                return "redirect:/partner/conventii";
            }
            
            // Check if convention belongs to partner's company
            if (conventie.getCompanie().getId() != partner.getCompanie().getId()) {
                redirectAttributes.addFlashAttribute("errorMessage", 
                    "Nu aveți permisiunea să aprobați această convenție!");
                return "redirect:/partner/conventii";
            }
            
            // Update status
            conventie.setStatus(ConventieStatus.APROBATA_PARTENER);
            conventieRepository.save(conventie);
            
            redirectAttributes.addFlashAttribute("successMessage", 
                "Convenția a fost aprobată cu succes!");
                
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", 
                "Eroare la aprobarea convenției: " + e.getMessage());
        }
        
        return "redirect:/partner/conventii";
    }
    
    // Reject convention
    @PostMapping("/conventie/respinge/{id}")
    public String respingeConventie(@PathVariable("id") int id, 
                                   Authentication authentication, 
                                   RedirectAttributes redirectAttributes) {
        try {
            User user = (User) authentication.getPrincipal();
            Partner partner = partnerRepository.findByEmail(user.getEmail())
                    .orElseThrow(() -> new RuntimeException("Partner not found"));
            
            Conventie conventie = conventieRepository.findById(id);
            
            if (conventie == null) {
                redirectAttributes.addFlashAttribute("errorMessage", 
                    "Convenția nu a fost găsită!");
                return "redirect:/partner/conventii";
            }
            
            // Check if convention belongs to partner's company
            if (conventie.getCompanie().getId() != partner.getCompanie().getId()) {
                redirectAttributes.addFlashAttribute("errorMessage", 
                    "Nu aveți permisiunea să respingeți această convenție!");
                return "redirect:/partner/conventii";
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
        
        return "redirect:/partner/conventii";
    }
    
    // View convention details
    @GetMapping("/conventie-view/{id}")
    public String viewConventie(@PathVariable("id") int id, 
                              Authentication authentication, 
                              Model model) {
        User user = (User) authentication.getPrincipal();
        Partner partner = partnerRepository.findByEmail(user.getEmail())
                .orElseThrow(() -> new RuntimeException("Partner not found"));
        
        Conventie conventie = conventieRepository.findById(id);
        
        if (conventie == null || conventie.getCompanie().getId() != partner.getCompanie().getId()) {
            return "redirect:/partner/conventii";
        }
        
        model.addAttribute("user", user);
        model.addAttribute("partner", partner);
        model.addAttribute("conventie", conventie);
        
        return "partner/conventie-view";
    }
    
    // Export PDF
    @GetMapping("/conventie-export-pdf/{id}")
    public ResponseEntity<byte[]> exportConventiePdf(@PathVariable("id") int id, Authentication authentication) {
        User user = (User) authentication.getPrincipal();
        Partner partner = partnerRepository.findByEmail(user.getEmail())
                .orElseThrow(() -> new RuntimeException("Partner not found"));
        
        Conventie conventie = conventieRepository.findById(id);
        
        if (conventie == null || conventie.getCompanie().getId() != partner.getCompanie().getId()) {
            return ResponseEntity.notFound().build();
        }
        
        try {
            // Use the export functionality you already have
            // Stub for PDF generation - you'll implement this
            
            String filename = String.format("conventie_%s_%s.pdf", 
                conventie.getStudent().getNume(),
                conventie.getCompanie().getNume());
            
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_PDF);
            headers.setContentDispositionFormData("attachment", filename);
            
            return new ResponseEntity<>(new byte[0], headers, HttpStatus.OK);
            
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
    
    // Export Word
    @GetMapping("/conventie-export-word/{id}")
    public ResponseEntity<byte[]> exportConventieWord(@PathVariable("id") int id, Authentication authentication) {
        User user = (User) authentication.getPrincipal();
        Partner partner = partnerRepository.findByEmail(user.getEmail())
                .orElseThrow(() -> new RuntimeException("Partner not found"));
        
        Conventie conventie = conventieRepository.findById(id);
        
        if (conventie == null || conventie.getCompanie().getId() != partner.getCompanie().getId()) {
            return ResponseEntity.notFound().build();
        }
        
        try {
            // Use the export functionality you already have
            // Stub for Word generation - you'll implement this
            
            String filename = String.format("conventie_%s_%s.docx", 
                conventie.getStudent().getNume(),
                conventie.getCompanie().getNume());
            
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);
            headers.setContentDispositionFormData("attachment", filename);
            
            return new ResponseEntity<>(new byte[0], headers, HttpStatus.OK);
            
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
}