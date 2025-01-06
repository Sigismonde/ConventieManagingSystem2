package ro.upt.ac.conventii.prorector;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;

import ro.upt.ac.conventii.companie.CompanieRepository;
import ro.upt.ac.conventii.conventie.Conventie;
import ro.upt.ac.conventii.conventie.ConventieRepository;
import ro.upt.ac.conventii.conventie.ConventieStatus;
import ro.upt.ac.conventii.security.User;
import ro.upt.ac.conventii.student.StudentRepository;

@Controller
@RequestMapping("/prorector")
public class ProrectorController {

	 	@Autowired
	    private ConventieRepository conventieRepository;
	    
	    @Autowired
	    private StudentRepository studentRepository;
	    
	    @Autowired
	    private CompanieRepository companieRepository;

    // Dashboard pentru prorector
	    @GetMapping("/dashboard")
	    public String dashboard(Model model, Authentication authentication) {
	        if (authentication == null || !(authentication.getPrincipal() instanceof User)) {
	            return "redirect:/login";
	        }

	        User user = (User) authentication.getPrincipal();
	        model.addAttribute("user", user);

	        try {
	            // Initializare liste goale pentru a evita null
	            List<Conventie> conventiiNesemnate = conventieRepository.findByStatus(ConventieStatus.IN_ASTEPTARE);
	            List<Conventie> conventiiSemnate = conventieRepository.findByStatus(ConventieStatus.APROBATA);
	            
	            if (conventiiNesemnate == null) conventiiNesemnate = new ArrayList<>();
	            if (conventiiSemnate == null) conventiiSemnate = new ArrayList<>();

	            // Adăugare în model
	            model.addAttribute("conventiiNesemnate", conventiiNesemnate);
	            model.addAttribute("conventiiSemnate", conventiiSemnate);
	            
	        } catch (Exception e) {
	            // În caz de eroare, inițializăm cu liste goale
	            model.addAttribute("conventiiNesemnate", new ArrayList<>());
	            model.addAttribute("conventiiSemnate", new ArrayList<>());
	            
	            // Log eroare
	            System.err.println("Eroare la încărcarea datelor: " + e.getMessage());
	        }

	        return "prorector/dashboard";
	    }

    // Semnare convenție
    @PostMapping("/semneaza-conventie/{id}")
    public String semneazaConventie(@PathVariable("id") int id) {
        Conventie conventie = conventieRepository.findById(id);
        if (conventie != null) {
            conventie.setStatus(ConventieStatus.APROBATA);
            conventieRepository.save(conventie);
        }
        return "redirect:/prorector/dashboard";
    }

    // Generare PDF convenție
    @GetMapping("/genereaza-pdf/{id}")
    public ResponseEntity<byte[]> genereazaPDF(@PathVariable("id") int id) {
        Conventie conventie = conventieRepository.findById(id);
        if (conventie == null) {
            return ResponseEntity.notFound().build();
        }

        // Aici ar trebui să adăugăm logica de generare PDF
        // Pentru moment returnăm un placeholder
        byte[] pdfContent = new byte[0]; // Înlocuiți cu generarea reală de PDF

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_PDF);
        headers.setContentDispositionFormData("filename", "conventie_" + id + ".pdf");

        return ResponseEntity.ok()
                .headers(headers)
                .body(pdfContent);
    }

    // Vizualizare listă convenții
    @GetMapping("/conventii")
    public String listaConventii(Model model) {
        List<Conventie> conventii = conventieRepository.findAll();
        model.addAttribute("conventii", conventii);
        return "prorector/lista-conventii";
    }
}