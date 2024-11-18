package ro.upt.ac.conventii.prodecan;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import ro.upt.ac.conventii.conventie.Conventie;
import ro.upt.ac.conventii.conventie.ConventieRepository;
import ro.upt.ac.conventii.conventie.ConventieStatus;
import ro.upt.ac.conventii.security.User;
import ro.upt.ac.conventii.student.Student;
import ro.upt.ac.conventii.student.StudentRepository;
import ro.upt.ac.conventii.companie.Companie;
import ro.upt.ac.conventii.companie.CompanieRepository;
import ro.upt.ac.conventii.cadruDidactic.CadruDidactic;
import ro.upt.ac.conventii.cadruDidactic.CadruDidacticRepository;

@Controller
@RequestMapping("/prodecan")
public class ProdecanController {

    @Autowired
    private ConventieRepository conventieRepository;
    
    @Autowired
    private StudentRepository studentRepository;
    
    @Autowired
    private CompanieRepository companieRepository;
    
    @Autowired
    private CadruDidacticRepository cadruDidacticRepository;

    @Autowired
    private ProdecanRepository prodecanRepository;

    // Dashboard endpoint - pagina principală pentru prodecan
    @GetMapping("/dashboard")
    public String dashboard(Model model, Authentication authentication) {
        if (authentication == null || !(authentication.getPrincipal() instanceof User)) {
            return "redirect:/login";
        }

        User user = (User) authentication.getPrincipal();
        model.addAttribute("user", user);

        try {
            // Statistici pentru dashboard
            long totalStudenti = studentRepository.count();
            long totalCompanii = companieRepository.count();
            long totalCadreDidactice = cadruDidacticRepository.count();
            
            model.addAttribute("totalStudenti", totalStudenti);
            model.addAttribute("totalCompanii", totalCompanii);
            model.addAttribute("totalCadreDidactice", totalCadreDidactice);
            
            // Preluăm convențiile
            List<Conventie> conventiiNesemnate = conventieRepository.findByStatus(ConventieStatus.IN_ASTEPTARE);
            List<Conventie> conventiiSemnate = conventieRepository.findByStatus(ConventieStatus.APROBATA);
            
            model.addAttribute("conventiiNesemnate", conventiiNesemnate != null ? conventiiNesemnate : new ArrayList<>());
            model.addAttribute("conventiiSemnate", conventiiSemnate != null ? conventiiSemnate : new ArrayList<>());
        } catch (Exception e) {
            // Log eroarea
            System.err.println("Eroare la încărcarea datelor: " + e.getMessage());
            // Setăm valori default
            model.addAttribute("totalStudenti", 0);
            model.addAttribute("totalCompanii", 0);
            model.addAttribute("totalCadreDidactice", 0);
            model.addAttribute("conventiiNesemnate", new ArrayList<>());
            model.addAttribute("conventiiSemnate", new ArrayList<>());
        }
        
        return "prodecan/dashboard";
    }

    @PostMapping("/conventie/aproba/{id}")
    public String aprobaConventie(@PathVariable int id) {
        try {
            Conventie conventie = conventieRepository.findById(id);
            if (conventie != null) {
                conventie.setStatus(ConventieStatus.APROBATA);
                conventieRepository.save(conventie);
            }
        } catch (Exception e) {
            System.err.println("Eroare la aprobarea convenției: " + e.getMessage());
        }
        return "redirect:/prodecan/conventii";
    }

    @PostMapping("/conventie/respinge/{id}")
    public String respingeConventie(@PathVariable int id) {
        try {
            Conventie conventie = conventieRepository.findById(id);
            if (conventie != null) {
                conventie.setStatus(ConventieStatus.RESPINSA);
                conventieRepository.save(conventie);
            }
        } catch (Exception e) {
            System.err.println("Eroare la respingerea convenției: " + e.getMessage());
        }
        return "redirect:/prodecan/conventii";
    }

    @GetMapping("/studenti")
    public String studenti(Model model) {
        try {
            List<Student> studenti = studentRepository.findAll();
            model.addAttribute("studenti", studenti != null ? studenti : new ArrayList<>());
        } catch (Exception e) {
            System.err.println("Eroare la încărcarea studenților: " + e.getMessage());
            model.addAttribute("studenti", new ArrayList<>());
        }
        return "prodecan/studenti";
    }

    @GetMapping("/companii")
    public String companii(Model model) {
        try {
            List<Companie> companii = companieRepository.findAll();
            model.addAttribute("companii", companii != null ? companii : new ArrayList<>());
        } catch (Exception e) {
            System.err.println("Eroare la încărcarea companiilor: " + e.getMessage());
            model.addAttribute("companii", new ArrayList<>());
        }
        return "prodecan/companii";
    }

    @GetMapping("/cadre-didactice")
    public String cadreDidactice(Model model) {
        try {
            List<CadruDidactic> cadreDidactice = cadruDidacticRepository.findAll();
            model.addAttribute("cadreDidactice", cadreDidactice != null ? cadreDidactice : new ArrayList<>());
        } catch (Exception e) {
            System.err.println("Eroare la încărcarea cadrelor didactice: " + e.getMessage());
            model.addAttribute("cadreDidactice", new ArrayList<>());
        }
        return "prodecan/cadre-didactice";
    }
}