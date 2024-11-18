package ro.upt.ac.conventii.student;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;

import ro.upt.ac.conventii.conventie.Conventie;
import ro.upt.ac.conventii.conventie.ConventieRepository;
import ro.upt.ac.conventii.conventie.ConventieStatus;
import ro.upt.ac.conventii.security.User;

@Controller
public class StudentController {
    
    @Autowired
    private StudentRepository studentRepository;
    
    @Autowired
    private ConventieRepository conventieRepository;

    // Endpoint pentru dashboard
    @GetMapping("/student/dashboard")
    public String dashboard(Authentication authentication, Model model) {
        User user = (User) authentication.getPrincipal();
        model.addAttribute("user", user);
        
        // Adăugăm convențiile studentului pentru tabel
        List<Conventie> conventii = conventieRepository.findByStudentEmail(user.getEmail());
        model.addAttribute("conventiiRecente", conventii);
        
        return "student/dashboard";
    }

    // Endpoint-uri CRUD existente
    @GetMapping("/student-create")
    public String create(Student student) {
        return "student-create";
    }

    @PostMapping("/student-create-save")
    public String createSave(@Validated Student student, BindingResult result, Model model) {
        if(result.hasErrors()) {
            return "student-create";
        }
        studentRepository.save(student);
        return "redirect:/student-read";
    }
    
    @GetMapping("/student-read")
    public String read(Model model) {
        model.addAttribute("studenti", studentRepository.findAll());
        return "student-read";
    }
    
    @GetMapping("/student-edit/{id}")
    public String edit(@PathVariable("id") int id, Model model) {
        Student student = studentRepository.findById(id);
        model.addAttribute("student", student);
        return "student-update";
    }
    
    @PostMapping("/student-update/{id}")
    public String update(@PathVariable("id") int id, @Validated Student student, 
                        BindingResult result, Model model) {
        if(result.hasErrors()) {
            student.setId(id);
            return "student-update";
        }
        studentRepository.save(student);
        return "redirect:/student-read";
    }
    
    @GetMapping("/student-delete/{id}")
    public String delete(@PathVariable("id") int id, Model model) {
        Student student = studentRepository.findById(id);
        studentRepository.delete(student);
        return "redirect:/student-read";
    }

    // Endpoint-uri pentru convenții
    @GetMapping("/student/conventii")
    public String conventiiStudent(Authentication authentication, Model model) {
        User user = (User) authentication.getPrincipal();
        model.addAttribute("user", user);
        
        List<Conventie> conventii = conventieRepository.findByStudentEmail(user.getEmail());
        model.addAttribute("conventii", conventii);
        return "conventie-read";
    }

    @GetMapping("/student/conventie-noua")
    public String conventieNoua(Authentication authentication, Model model) {
        User user = (User) authentication.getPrincipal();
        model.addAttribute("user", user);
        
        Conventie conventie = new Conventie();
        conventie.setStatus(ConventieStatus.IN_ASTEPTARE);
        model.addAttribute("conventie", conventie);
        
        return "conventie-create";
    }
}