package ro.upt.ac.conventii.prodecan;

import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import com.lowagie.text.*;
import com.lowagie.text.Document;
import com.lowagie.text.pdf.*;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

import org.apache.poi.xwpf.usermodel.ParagraphAlignment;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.apache.poi.xwpf.usermodel.XWPFParagraph;
import org.apache.poi.xwpf.usermodel.XWPFRun;
import org.apache.poi.xwpf.usermodel.XWPFTable;
import org.apache.poi.xwpf.usermodel.XWPFTableCell;
import org.apache.poi.xwpf.usermodel.XWPFTableRow;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.CTPageMar;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.CTSectPr;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.lowagie.text.Chunk;
import com.lowagie.text.Element;
import com.lowagie.text.Font;
import com.lowagie.text.Paragraph;
import com.lowagie.text.pdf.BaseFont;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;

import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.PageRequest;

import ro.upt.ac.conventii.conventie.Conventie;
import ro.upt.ac.conventii.conventie.ConventieRepository;
import ro.upt.ac.conventii.conventie.ConventieStatus;
import ro.upt.ac.conventii.security.User;
import ro.upt.ac.conventii.security.UserRepository;
import ro.upt.ac.conventii.service.PasswordGeneratorService;
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
    
    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private PasswordGeneratorService passwordGeneratorService;
    
    @Autowired
    private BCryptPasswordEncoder passwordEncoder;

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
            
            // Preluăm convențiile nesemnate
            List<Conventie> conventiiNesemnate = conventieRepository.findByStatus(ConventieStatus.IN_ASTEPTARE);
            model.addAttribute("conventiiNesemnate", conventiiNesemnate != null ? conventiiNesemnate : new ArrayList<>());

            // Preluăm doar ultimele 5 convenții semnate
            Pageable topFive = PageRequest.of(0, 5);
            List<Conventie> conventiiSemnate = conventieRepository
                .findTop5ByStatusOrderByDataIntocmiriiDesc(ConventieStatus.APROBATA, topFive);
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
    
    
    @GetMapping("/studenti")
    public String studenti(Model model, Authentication authentication) {
        User user = (User) authentication.getPrincipal();
        model.addAttribute("user", user);
        
        try {
            List<Student> studenti = studentRepository.findAll();
            model.addAttribute("studenti", studenti != null ? studenti : new ArrayList<>());
        } catch (Exception e) {
            System.err.println("Eroare la încărcarea studenților: " + e.getMessage());
            model.addAttribute("studenti", new ArrayList<>());
        }
        return "prodecan/studenti";
    }
    // Management Studenți
    @GetMapping("/student-create")
    public String afiseazaFormularStudent(Model model) {
        model.addAttribute("student", new Student());
        return "prodecan/student-form";
    }

    @PostMapping("/student-create")
    public String creazaStudent(@ModelAttribute Student student, RedirectAttributes redirectAttributes) {
        try {
            // Salvăm studentul în baza de date
            studentRepository.save(student);
            
            // Generăm parola și creăm cont de utilizator
            String parolaTemporara = student.getNume() + student.getPrenume() + student.getAnDeStudiu();
            User userStudent = new User();
            userStudent.setEmail(student.getEmail());
            userStudent.setNume(student.getNume());
            userStudent.setPrenume(student.getPrenume());
            userStudent.setPassword(passwordEncoder.encode(parolaTemporara));
            userStudent.setRole("ROLE_STUDENT");
            userStudent.setEnabled(true);
            userStudent.setFacultate(student.getFacultate());
            userStudent.setSpecializare(student.getSpecializare());
            userStudent.setFirstLogin(true);  // Setăm explicit doar pentru studenți
            
            userRepository.save(userStudent);
            
            redirectAttributes.addFlashAttribute("successMessage", 
                "Student creat cu succes!\n" +
                "----------------------------------------\n" +
                "Email: " + student.getEmail() + "\n" +
                "PAROLA TEMPORARĂ: " + parolaTemporara + "\n" +
                "----------------------------------------\n" +
                "IMPORTANT: Salvați această parolă!");
                
            return "redirect:/prodecan/studenti";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", 
                "Eroare la crearea studentului: " + e.getMessage());
            return "redirect:/prodecan/studenti";
        }
    }

    @GetMapping("/student-edit/{id}")
    public String showEditStudentForm(@PathVariable int id, Model model, Authentication authentication) {
        User user = (User) authentication.getPrincipal();
        model.addAttribute("user", user);
        
        Student student = studentRepository.findById(id);
        if (student != null) {
            model.addAttribute("student", student);
            return "prodecan/student-form";
        }
        return "redirect:/prodecan/studenti";
    }

    @PostMapping("/student-edit/{id}")
    public String updateStudent(@PathVariable int id, @ModelAttribute Student student, RedirectAttributes redirectAttributes) {
        try {
            student.setId(id);
            studentRepository.save(student);
            
            User userStudent = userRepository.findByEmail(student.getEmail());
            if (userStudent != null) {
                userStudent.setNume(student.getNume());
                userStudent.setPrenume(student.getPrenume());
                userStudent.setFacultate(student.getFacultate());
                userStudent.setSpecializare(student.getSpecializare());
                userRepository.save(userStudent);
            }
            redirectAttributes.addFlashAttribute("successMessage", "Student actualizat cu succes!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Eroare la actualizarea studentului: " + e.getMessage());
        }
        return "redirect:/prodecan/studenti";
    }

    @PostMapping("/student-reset-password/{id}")
    public String resetStudentPassword(@PathVariable int id, RedirectAttributes redirectAttributes) {
        try {
            Student student = studentRepository.findById(id);
            if (student != null) {
                User userStudent = userRepository.findByEmail(student.getEmail());
                if (userStudent != null) {
                    String newPassword = passwordGeneratorService.generateRandomPassword();
                    userStudent.setPassword(passwordEncoder.encode(newPassword));
                    userRepository.save(userStudent);
                    
                    redirectAttributes.addFlashAttribute("successMessage", 
                        "Parolă resetată cu succes!\n" +
                        "----------------------------------------\n" +
                        "Student: " + student.getNume() + " " + student.getPrenume() + "\n" +
                        "Email: " + student.getEmail() + "\n" +
                        "NOUA PAROLĂ: " + newPassword + "\n" +
                        "----------------------------------------\n" +
                        "IMPORTANT: Salvați această parolă!");
                }
            }
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", 
                "Eroare la resetarea parolei: " + e.getMessage());
        }
        return "redirect:/prodecan/studenti";
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

    // Adăugare companie nouă - formular
    @GetMapping("/companie-create")
    public String showCreateCompanieForm(Model model) {
        model.addAttribute("companie", new Companie());
        return "prodecan/companie-form";
    }

    // Salvare companie nouă
    @PostMapping("/companie-create")
    public String createCompanie(@ModelAttribute Companie companie) {
        companieRepository.save(companie);
        // Modificat redirect către pagina prodecanului
        return "redirect:/prodecan/companii";
    }

    // Editare companie - formular
    @GetMapping("/companie-edit/{id}")
    public String showEditCompanieForm(@PathVariable int id, Model model) {
        Companie companie = companieRepository.findById(id);
        if (companie != null) {
            model.addAttribute("companie", companie);
            return "prodecan/companie-form";
        }
        // Modificat redirect către pagina prodecanului
        return "redirect:/prodecan/companii";
    }

    // Salvare modificări companie
    @PostMapping("/companie-edit/{id}")
    public String updateCompanie(@PathVariable int id, @ModelAttribute Companie companie) {
        companie.setId(id);
        companieRepository.save(companie);
        // Modificat redirect către pagina prodecanului
        return "redirect:/prodecan/companii";
    }

    // Ștergere companie
    @GetMapping("/companie-delete/{id}")
    public String deleteCompanie(@PathVariable int id) {
        Companie companie = companieRepository.findById(id);
        if (companie != null) {
            companieRepository.delete(companie);
        }
        // Modificat redirect către pagina prodecanului
        return "redirect:/prodecan/companii";
    }
    @PostMapping("/conventie/aproba/{id}")
    public String aprobaConventie(@PathVariable int id, RedirectAttributes redirectAttributes) {
        try {
            Conventie conventie = conventieRepository.findById(id);
            if (conventie != null) {
                conventie.setStatus(ConventieStatus.APROBATA);
                conventie.setDataIntocmirii(new java.sql.Date(System.currentTimeMillis()));
                conventieRepository.save(conventie);
                redirectAttributes.addFlashAttribute("successMessage", "Convenția a fost aprobată cu succes!");
            }
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Eroare la aprobarea convenției: " + e.getMessage());
        }
        return "redirect:/prodecan/conventii";
    }

    @PostMapping("/conventie/respinge/{id}")
    public String respingeConventie(@PathVariable int id, RedirectAttributes redirectAttributes) {
        try {
            Conventie conventie = conventieRepository.findById(id);
            if (conventie != null) {
                conventie.setStatus(ConventieStatus.RESPINSA);
                conventie.setDataIntocmirii(new java.sql.Date(System.currentTimeMillis()));
                conventieRepository.save(conventie);
                redirectAttributes.addFlashAttribute("successMessage", "Convenția a fost respinsă.");
            }
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Eroare la respingerea convenției: " + e.getMessage());
        }
        return "redirect:/prodecan/conventii";
    }
   

 // Lista cadre didactice
    @GetMapping("/cadre-didactice")
    public String cadreDidactice(Model model, Authentication authentication) {
        User user = (User) authentication.getPrincipal();
        model.addAttribute("user", user);
        
        try {
            List<CadruDidactic> cadreDidactice = cadruDidacticRepository.findAll();
            model.addAttribute("cadreDidactice", cadreDidactice != null ? cadreDidactice : new ArrayList<>());
        } catch (Exception e) {
            System.err.println("Eroare la încărcarea cadrelor didactice: " + e.getMessage());
            model.addAttribute("cadreDidactice", new ArrayList<>());
        }
        return "prodecan/cadre-didactice";
    }

    // Formular adăugare cadru didactic
    @GetMapping("/cadru-didactic-create")
    public String showCreateCadruDidacticForm(Model model, Authentication authentication) {
        User user = (User) authentication.getPrincipal();
        model.addAttribute("user", user);
        model.addAttribute("cadruDidactic", new CadruDidactic());
        return "prodecan/cadru-didactic-form";
    }

    // Salvare cadru didactic nou
    @PostMapping("/cadru-didactic-create")
    public String createCadruDidactic(@ModelAttribute("cadruDidactic") CadruDidactic cadruDidactic) {
        try {
            cadruDidacticRepository.save(cadruDidactic);
        } catch (Exception e) {
            System.err.println("Eroare la salvarea cadrului didactic: " + e.getMessage());
        }
        return "redirect:/prodecan/cadre-didactice";
    }

    // Formular editare cadru didactic
    @GetMapping("/cadru-didactic-edit/{id}")
    public String showEditCadruDidacticForm(@PathVariable int id, Model model, Authentication authentication) {
        User user = (User) authentication.getPrincipal();
        model.addAttribute("user", user);
        
        CadruDidactic cadruDidactic = cadruDidacticRepository.findById(id);
        if (cadruDidactic != null) {
            model.addAttribute("cadruDidactic", cadruDidactic);
            return "prodecan/cadru-didactic-form";
        }
        return "redirect:/prodecan/cadre-didactice";
    }
    
    @GetMapping("/conventii")
    public String listaConventii(Model model, Authentication authentication) {
        System.out.println("Încercare accesare endpoint conventii"); // logging pentru debug
        
        try {
            User user = (User) authentication.getPrincipal();
            model.addAttribute("user", user);
            
            // Verificăm dacă repository-ul este injectat corect
            if (conventieRepository == null) {
                System.out.println("EROARE: conventieRepository este null!");
                throw new RuntimeException("Repository-ul nu este injectat");
            }

            // Încercăm să preluăm toate convențiile mai întâi
            List<Conventie> toateConventiile = conventieRepository.findAll();
            System.out.println("Total convenții găsite: " + toateConventiile.size());

            // Apoi filtrăm după status
            List<Conventie> conventiiNesemnate = conventieRepository.findByStatus(ConventieStatus.IN_ASTEPTARE);
            List<Conventie> conventiiSemnate = conventieRepository.findByStatus(ConventieStatus.APROBATA);
            
            System.out.println("Convenții nesemnate: " + 
                (conventiiNesemnate != null ? conventiiNesemnate.size() : "null"));
            System.out.println("Convenții semnate: " + 
                (conventiiSemnate != null ? conventiiSemnate.size() : "null"));

            // Inițializăm listele goale dacă e cazul
            conventiiNesemnate = conventiiNesemnate != null ? conventiiNesemnate : new ArrayList<>();
            conventiiSemnate = conventiiSemnate != null ? conventiiSemnate : new ArrayList<>();

            model.addAttribute("conventiiNesemnate", conventiiNesemnate);
            model.addAttribute("conventiiSemnate", conventiiSemnate);
            
            return "prodecan/conventii";
            
        } catch (Exception e) {
            System.err.println("Eroare în controller: " + e.getMessage());
            e.printStackTrace(); // Pentru a vedea stack trace-ul complet
            
            // Adăugăm liste goale și mesaj de eroare în model
            model.addAttribute("conventiiNesemnate", new ArrayList<>());
            model.addAttribute("conventiiSemnate", new ArrayList<>());
            model.addAttribute("errorMessage", "A apărut o eroare: " + e.getMessage());
            
            return "prodecan/conventii";
        }
    }

    // Salvare modificări cadru didactic
    @PostMapping("/cadru-didactic-edit/{id}")
    public String updateCadruDidactic(@PathVariable int id, @ModelAttribute("cadruDidactic") CadruDidactic cadruDidactic) {
        try {
            cadruDidactic.setId(id);
            cadruDidacticRepository.save(cadruDidactic);
        } catch (Exception e) {
            System.err.println("Eroare la actualizarea cadrului didactic: " + e.getMessage());
        }
        return "redirect:/prodecan/cadre-didactice";
    }

    // Ștergere cadru didactic
    @GetMapping("/cadru-didactic-delete/{id}")
    public String deleteCadruDidactic(@PathVariable int id) {
        try {
            CadruDidactic cadruDidactic = cadruDidacticRepository.findById(id);
            if (cadruDidactic != null) {
                cadruDidacticRepository.delete(cadruDidactic);
            }
        } catch (Exception e) {
            System.err.println("Eroare la ștergerea cadrului didactic: " + e.getMessage());
        }
        return "redirect:/prodecan/cadre-didactice";
    }
    
    @GetMapping("/conventie-export-word/{id}")
    public ResponseEntity<byte[]> exportConventieWord(@PathVariable("id") int id) throws IOException {
        Conventie conventie = conventieRepository.findById(id);
        if (conventie == null) {
            return ResponseEntity.notFound().build();
        }

        XWPFDocument document = generateWordDocument(conventie);
        
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        document.write(out);
        document.close();
        
        String filename = String.format("conventie_%s_%s.docx", 
            conventie.getStudent().getNume(),
            conventie.getCompanie().getNume());

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);
        headers.setContentDispositionFormData("attachment", filename);

        return new ResponseEntity<>(out.toByteArray(), headers, HttpStatus.OK);
    }

    @GetMapping("/conventie-export/{id}")
    public ResponseEntity<byte[]> exportConventie(@PathVariable("id") int id) {
        Conventie conventie = conventieRepository.findById(id);
        if (conventie == null) {
            return ResponseEntity.notFound().build();
        }

        String filename = String.format("conventie_%s_%s.html", 
            conventie.getStudent().getNume(),
            conventie.getCompanie().getNume());

        String htmlContent = generateConventieHtml(conventie);
        
        // Adăugăm BOM și convertim la bytes cu UTF-8
        byte[] bomBytes = new byte[] { (byte)0xEF, (byte)0xBB, (byte)0xBF };
        byte[] contentBytes = htmlContent.getBytes(StandardCharsets.UTF_8);
        byte[] fullContent = new byte[bomBytes.length + contentBytes.length];
        System.arraycopy(bomBytes, 0, fullContent, 0, bomBytes.length);
        System.arraycopy(contentBytes, 0, fullContent, bomBytes.length, contentBytes.length);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.TEXT_HTML);
        headers.set(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + filename);
        headers.set(HttpHeaders.CONTENT_TYPE, "text/html; charset=UTF-8");

        return new ResponseEntity<>(fullContent, headers, HttpStatus.OK);
    }
    
    @GetMapping("/conventie-export-pdf/{id}")
    public ResponseEntity<byte[]> exportConventiePdf(@PathVariable("id") int id) throws IOException, DocumentException {
        Conventie conventie = conventieRepository.findById(id);
        if (conventie == null) {
            return ResponseEntity.notFound().build();
        }

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        Document document = new Document(PageSize.A4, 50, 50, 50, 50);
        PdfWriter writer = PdfWriter.getInstance(document, out);
        document.open();
        
     // Font pentru diacritice
        BaseFont baseFont = BaseFont.createFont("c:/windows/fonts/arial.ttf", BaseFont.IDENTITY_H, BaseFont.EMBEDDED);
        Font font = new Font(baseFont, 11);
        Font boldFont = new Font(baseFont, 11, Font.BOLD);
        Font titleFont = new Font(baseFont, 14, Font.BOLD);
        
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd.MM.yyyy");

        // Header
        Paragraph header = new Paragraph("ANEXA 3", boldFont);
        header.setAlignment(Element.ALIGN_RIGHT);
        header.add(new Chunk("\nNr. _____ / " + dateFormat.format(new java.util.Date())));
        document.add(header);
        document.add(Chunk.NEWLINE);

        // Titlu
        Paragraph title = new Paragraph("CONVENȚIE-CADRU", titleFont);
        title.setAlignment(Element.ALIGN_CENTER);
        document.add(title);
        
        Paragraph subtitle = new Paragraph("privind efectuarea stagiului de practică în cadrul\nprogramelor de studii universitare de licență sau masterat", titleFont);
        subtitle.setAlignment(Element.ALIGN_CENTER);
        document.add(subtitle);
        document.add(Chunk.NEWLINE);

        // Părți contractante
        document.add(new Paragraph("Prezenta convenție-cadru se încheie între:", font));
        document.add(Chunk.NEWLINE);

        // UPT
        Paragraph upt = new Paragraph();
        upt.add(new Chunk("1. Universitatea Politehnica Timișoara", boldFont));
        upt.add(new Chunk(", reprezentată de Rector, conf. univ. dr. ing. Florin DRĂGAN, cu sediul în TIMIȘOARA, " +
                "Piața Victoriei, Nr. 2, cod 300006, telefon: 0256-403011, email: rector@upt.ro, " +
                "cod unic de înregistrare: 4269282, denumită în continuare ", font));
        upt.add(new Chunk("organizator de practică", boldFont));
        document.add(upt);
        document.add(Chunk.NEWLINE);

        // Companie
        Paragraph comp = new Paragraph();
        comp.add(new Chunk("2. " + conventie.getCompanie().getNume(), boldFont));
        comp.add(new Chunk(", reprezentată de " + conventie.getCompanie().getReprezentant() +
                " în calitate de " + conventie.getCompanie().getCalitate() +
                ", cu sediul în " + conventie.getCompanie().getAdresa() +
                ", telefon " + conventie.getCompanie().getTelefon() +
                ", denumită în continuare ", font));
        comp.add(new Chunk("partener de practică", boldFont));
        document.add(comp);
        document.add(Chunk.NEWLINE);

        // Student
        Paragraph stud = new Paragraph();
        stud.add(new Chunk("3. Student " + conventie.getStudent().getNume() + " " + 
                conventie.getStudent().getPrenume(), boldFont));
        stud.add(new Chunk(", CNP " + conventie.getStudent().getCnp() +
                ", data nașterii " + dateFormat.format(conventie.getStudent().getDataNasterii()) +
                ", locul nașterii " + conventie.getStudent().getLoculNasterii() +
                ", cetățenie " + conventie.getStudent().getCetatenie() +
                ", CI seria " + conventie.getStudent().getSerieCi() +
                " nr. " + conventie.getStudent().getNumarCi() +
                ", adresa " + conventie.getStudent().getAdresa() +
                ", înscris în anul universitar " + conventie.getStudent().getAnUniversitar() +
                ", facultatea " + conventie.getStudent().getFacultate() +
                ", specializarea " + conventie.getStudent().getSpecializare() +
                ", anul de studiu " + conventie.getStudent().getAnDeStudiu() +
                ", email " + conventie.getStudent().getEmail() +
                ", telefon " + conventie.getStudent().getTelefon() +
                ", denumit în continuare ", font));
        stud.add(new Chunk("practicant", boldFont));
        document.add(stud);
        document.add(Chunk.NEWLINE);

        // Art. 1
        addArticleTitle(document, "Art. 1. Obiectul convenției-cadru", boldFont);
        addParagraph(document, "(1) Convenția-cadru stabilește modul în care se organizează și se " +
                     "desfășoară stagiul de practică în vederea consolidării cunoștințelor teoretice și " +
                     "formarea abilităților practice, spre a le aplica în concordanță cu specializarea pentru " +
                     "care se instruiește studentul practicant.", font);
        addParagraph(document, "(2) Stagiul de practică este realizat de practicant în vederea dobândirii " +
                     "competențelor profesionale menționate în Portofoliul de practică care este corelat cu fișa disciplinei de practică, " +
                     "parte integrantă a prezentei convenții. " +
                     "Locul desfășurării stagiului de practică este: " + conventie.getLoculDesfasurarii(), font);
        addParagraph(document, "(3) Modalitățile de derulare și conținutul stagiului de practică sunt descrise în " +
                     "prezenta convenție-cadru și în portofoliul de practică din anexă.", font);

        // Art. 2
        addArticleTitle(document, "Art. 2. Statutul practicantului", boldFont);
        addParagraph(document, "Practicantul rămâne, pe toată durata stagiului de pregătire practică, student al " +
                     "Universității Politehnica Timișoara.", font);

        // Art. 3
        addArticleTitle(document, "Art. 3. Durata și perioada desfășurării stagiului de practică", boldFont);
        addParagraph(document, "(1) Durata stagiului de practică, precizată în planul de învățământ, este de " +
                     conventie.getDurataInPlanulDeInvatamant() + " [h].", font);
        addParagraph(document, "(2) Perioada desfășurării stagiului de practică este conformă structurii anului universitar curent " +
                     "de la " + dateFormat.format(conventie.getDataInceput()) +
                     " până la " + dateFormat.format(conventie.getDataSfarsit()), font);

        // Continuă cu Art. 4-13...
        
        // Art. 4
        addArticleTitle(document, "Art. 4. Plata și obligațiile sociale", boldFont);
        addParagraph(document, "(1) Stagiul de pregătire practică (se bifează situația corespunzătoare):", font);
        addParagraph(document, "☐ - se efectuează în cadrul unui contract de muncă, cei doi parteneri putând să beneficieze " +
                     "de prevederile Legii nr. 72/2007 privind stimularea încadrării în muncă a elevilor și studenților;", font);
        addParagraph(document, "☐ - nu se efectuează în cadrul unui contract de muncă;", font);
        addParagraph(document, "☐ - se efectuează în cadrul unui proiect finanțat prin Fondul Social European;", font);
        addParagraph(document, "☐ - se efectuează în cadrul proiectului ...", font);
        addParagraph(document, "(2) În cazul angajării ulterioare, perioada stagiului nu va fi considerată ca vechime " +
                "în muncă în situația în care convenția nu se derulează în cadrul unui contract de muncă.", font);
   addParagraph(document, "(3) Practicantul nu poate pretinde un salariu din partea partenerului de practică, cu " +
                "excepția situației în care practicantul are statut de angajat.", font);
   addParagraph(document, "(4) Partenerul de practică poate totuși acorda practicantului o indemnizație, " +
                "gratificare, primă sau avantaje în natură, conform legislației în vigoare.", font);

   // Art. 5
   addArticleTitle(document, "Art. 5. Responsabilitățile practicantului", boldFont);
   addParagraph(document, "(1) Practicantul are obligația, ca pe durata derulării stagiului de practică, să " +
                "respecte programul de lucru stabilit și să execute activitățile specificate de tutore " +
                "în conformitate cu portofoliul de practică, în condițiile respectării cadrului legal cu " +
                "privire la volumul și dificultatea acestora.", font);
   addParagraph(document, "(2) Pe durata stagiului, practicantul respectă regulamentul de ordine interioară al " +
                "partenerului de practică. În cazul nerespectării acestui regulament, conducătorul " +
                "partenerului de practică își rezervă dreptul de a anula convenția-cadru, după ce în " +
                "prealabil a ascultat punctul de vedere al practicantului și al îndrumătorului de " +
                "practică și a înștiințat conducătorul facultății unde practicantul este înmatriculat " +
                "și după primirea confirmării de primire a acestei informații.", font);
   addParagraph(document, "(3) Practicantul are obligația de a respecta normele de securitate și sănătate în " +
                "muncă pe care le-a însușit de la reprezentantul partenerului de practică înainte de " +
                "începerea stagiului de practică.", font);
   addParagraph(document, "(4) Practicantul se angajează să nu folosească, în niciun caz, informațiile la care " +
                "are acces în timpul stagiului despre partenerul de practică sau clienții săi, pentru a " +
                "le comunica unui terț sau pentru a le publica, chiar după terminarea stagiului, decât " +
                "cu acordul respectivului partener de practică.", font);

   // Art. 6
   addArticleTitle(document, "Art. 6. Responsabilitățile partenerului de practică", boldFont);
   addParagraph(document, "(1) Partenerul de practică va stabili un tutore pentru stagiul de practică, " +
                "selectat dintre salariații proprii și ale cărui obligații sunt menționate în portofoliul " +
                "de practică, parte integrantă a convenției-cadru.", font);
   addParagraph(document, "(2) În cazul nerespectării obligațiilor de către practicant, tutorele va contacta " +
                "cadrul didactic supervizor, responsabil de practică, aplicându-se sancțiuni conform " +
                "legilor și regulamentelor în vigoare.", font);
   addParagraph(document, "(3) Înainte de începerea stagiului de practică, partenerul are obligația de a face " +
                "practicantului instructajul cu privire la normele de securitate și sănătate în muncă, " +
                "în conformitate cu legislația în vigoare. Printre responsabilitățile sale, partenerul de practică " +
                "va lua măsurile necesare pentru securitatea și sănătatea în muncă a practicantului, precum " +
                "și pentru comunicarea regulilor de prevenire a riscurilor profesionale.", font);
   addParagraph(document, "(4) Partenerul de practică trebuie să pună la dispoziția practicantului toate " +
                "mijloacele necesare pentru dobândirea competențelor precizate în portofoliul de practică.", font);
   addParagraph(document, "(5) Partenerul de practică are obligația de a asigura practicantului accesul liber la " +
                "serviciul de medicina muncii, pe durata derulării pregătirii practice.", font);

   // Art. 7
   addArticleTitle(document, "Art. 7. Obligațiile organizatorului de practică", boldFont);
   addParagraph(document, "(1) Organizatorul de practică desemnează un cadru didactic supervizor, responsabil " +
                "cu planificarea, organizarea și supravegherea desfășurării pregătirii practice. " +
                "Cadrul didactic supervizor, împreună cu tutorele desemnat de partenerul de practică " +
                "stabilesc tematica de practică și competențele profesionale care fac obiectul stagiului " +
                "de pregătire practică.", font);
   addParagraph(document, "(2) În cazul în care derularea stagiului de pregătire practică nu este conformă cu " +
                "angajamentele luate de către partenerul de practică în cadrul prezentei convenții, " +
                "conducătorul organizatorului de practică poate decide întreruperea stagiului de pregătire " +
                "practică conform convenției-cadru, după informarea prealabilă a conducătorului partenerului " +
                "de practică și după primirea confirmării de primire a acestei informații.", font);

   // Art. 8
   addArticleTitle(document, "Art. 8. Persoane desemnate de organizatorul de practică și partenerul de practică", boldFont);
   addParagraph(document, "(1) Tutorele (persoana care va avea responsabilitatea practicantului din partea partenerului de practică):", font);
   addParagraph(document, "Dl/Dna " + conventie.getTutore().getNume() + " " + conventie.getTutore().getPrenume() +
                "\nFuncția: " + conventie.getTutore().getFunctie() +
                "\nTelefon: " + conventie.getTutore().getTelefon() +
                "\nEmail: " + conventie.getTutore().getEmail(), font);
   addParagraph(document, "(2) Cadrul didactic supervizor, responsabil cu urmărirea derulării stagiului de practică din partea organizatorului de practică:", font);
   addParagraph(document, "Dl/Dna " + conventie.getCadruDidactic().getNume() + " " + conventie.getCadruDidactic().getPrenume() +
                "\nFuncția: " + conventie.getCadruDidactic().getFunctie() +
                "\nTelefon: " + conventie.getCadruDidactic().getTelefon() +
                "\nEmail: " + conventie.getCadruDidactic().getEmail(), font);

   // Art. 9
   addArticleTitle(document, "Art. 9. Evaluarea stagiului de pregătire practică prin credite transferabile", boldFont);
   addParagraph(document, "Numărul de credite transferabile ce vor fi obținute în urma desfășurării stagiului " +
                "de practică este de " + conventie.getNumarCredite() + ".", font);

   // Art. 10
   addArticleTitle(document, "Art. 10. Raportul privind stagiul de pregătire practică", boldFont);
   addParagraph(document, "(1) În timpul derulării stagiului de practică, tutorele împreună cu cadrul " +
                "didactic supervizor vor evalua practicantul în permanență, pe baza unei fișe de " +
                "observație/evaluare. Vor fi evaluate atât nivelul de dobândire a competențelor " +
                "profesionale, cât și comportamentul și modalitatea de integrare a practicantului în " +
                "activitatea partenerului de practică (disciplină, punctualitate, responsabilitate în " +
                "rezolvarea sarcinilor, respectarea regulamentului de ordine interioară al partenerului de practică etc.).", font);
   addParagraph(document, "(2) La finalul stagiului de practică, tutorele elaborează un raport, pe baza " +
                "evaluării nivelului de dobândire a competențelor de către practicant. Rezultatul " +
                "acestei evaluări va sta la baza notării practicantului de către cadrul didactic " +
                "supervizor.", font);
   addParagraph(document, "(3) Periodic și după încheierea stagiului de practică, practicantul va prezenta un caiet de practică care va cuprinde:", font);
   addParagraph(document, "• denumirea modulului de pregătire;\n" +
                "• competențe exersate;\n" +
                "• activități desfășurate pe perioada stagiului de practică;\n" +
                "• observații personale privitoare la activitatea depusă.", font);

   // Art. 11
   addArticleTitle(document, "Art. 11. Sănătatea și securitatea în muncă. Protecția socială a practicantului", boldFont);
   addParagraph(document, "(1) Practicantul anexează prezentului contract dovada asigurării medicale valabile " +
                "în perioada și pe teritoriul statului unde se desfășoară stagiul de practică.", font);
   addParagraph(document, "(2) Partenerul de practică are obligația respectării prevederilor legale cu privire " +
                "la sănătatea și securitatea în muncă a practicantului pe durata stagiului de practică.", font);
   addParagraph(document, "(3) Practicantului i se asigură protecție socială conform legislației în vigoare. Ca " +
                "urmare, conform dispozițiilor Legii nr. 346/2002 privind asigurările pentru accidente de " +
                "muncă și boli profesionale, cu modificările și completările ulterioare, practicantul " +
                "beneficiază de legislația privitoare la accidentele de muncă pe toata durata efectuării " +
                "pregătirii practice.", font);
   addParagraph(document, "(4) În cazul unui accident suportat de practicant, fie în cursul lucrului, fie în " +
                "timpul deplasării la lucru, partenerul de practică se angajează să înștiințeze " +
                "asiguratorul cu privire la accidentul care a avut loc.", font);

   // Art. 12
   addArticleTitle(document, "Art. 12. Condiții facultative de desfășurare a stagiului de pregătire practică", boldFont);
   addParagraph(document, "(1) Îndemnizație, gratificări sau prime acordate practicantului:", font);
   addParagraph(document, conventie.getIndemnizatii() != null ? conventie.getIndemnizatii() : "Nu este cazul", font);
   addParagraph(document, "(2) Avantaje eventuale:", font);
   addParagraph(document, conventie.getAvantaje() != null ? conventie.getAvantaje() : "Nu este cazul", font);
   addParagraph(document, "(3) Alte precizări:", font);
   addParagraph(document, conventie.getAltePrecizari() != null ? conventie.getAltePrecizari() : "Nu este cazul", font);

   // Art. 13
   addArticleTitle(document, "Art. 13. Prevederi finale", boldFont);
   addParagraph(document, "Prezenta convenție-cadru s-a încheiat în trei exemplare la data: " + 
                dateFormat.format(conventie.getDataIntocmirii()), font);
// La final, adăugăm tabelul de semnături
   document.add(Chunk.NEWLINE);
   PdfPTable table = new PdfPTable(3);
   table.setWidthPercentage(100);

   // Adăugăm celulele pentru tabel
   PdfPCell cell1 = new PdfPCell(new Paragraph("Universitatea Politehnica Timișoara\nRector", boldFont));
   PdfPCell cell2 = new PdfPCell(new Paragraph("Partener de practică", boldFont));
   PdfPCell cell3 = new PdfPCell(new Paragraph("Student", boldFont));

   cell1.setHorizontalAlignment(Element.ALIGN_CENTER);
   cell2.setHorizontalAlignment(Element.ALIGN_CENTER);
   cell3.setHorizontalAlignment(Element.ALIGN_CENTER);

   table.addCell(cell1);
   table.addCell(cell2);
   table.addCell(cell3);

   // Adăugăm rândurile pentru semnături
   PdfPCell sign1 = new PdfPCell(new Paragraph("conf. univ. dr. ing. Florin DRĂGAN\n\nSemnătura:________\nData:________", font));
   PdfPCell sign2 = new PdfPCell(new Paragraph(conventie.getCompanie().getReprezentant() + 
                                              "\n\nSemnătura:________\nData:________", font));
   PdfPCell sign3 = new PdfPCell(new Paragraph(conventie.getStudent().getNume() + " " + 
                                              conventie.getStudent().getPrenume() + 
                                              "\n\nSemnătura:________\nData:________", font));

   sign1.setHorizontalAlignment(Element.ALIGN_CENTER);
   sign2.setHorizontalAlignment(Element.ALIGN_CENTER);
   sign3.setHorizontalAlignment(Element.ALIGN_CENTER);

   table.addCell(sign1);
   table.addCell(sign2);
   table.addCell(sign3);

   document.add(table);

        
        document.close();

        String filename = String.format("conventie_%s_%s.pdf", 
            conventie.getStudent().getNume(),
            conventie.getCompanie().getNume());

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_PDF);
        headers.setContentDispositionFormData("attachment", filename);

        return new ResponseEntity<>(out.toByteArray(), headers, HttpStatus.OK);
    }

    
    private void addArticleTitle(Document document, String title, Font font) throws DocumentException {
       
    	Paragraph paragraph = new Paragraph(title, font);
    	   paragraph.setSpacingBefore(10f);
    	   paragraph.setSpacingAfter(10f);
    	   document.add(paragraph);
    }

    private void addParagraph(Document document, String text, Font font) throws DocumentException {
        Paragraph paragraph = new Paragraph(text, font);
        paragraph.setIndentationLeft(20f);
        paragraph.setSpacingAfter(5f);
        document.add(paragraph);
    }

    private XWPFDocument generateWordDocument(Conventie conventie) throws IOException {
        XWPFDocument document = new XWPFDocument();
        
        // Setează marginile documentului (1440 twips = 1 inch)
        CTSectPr sectPr = document.getDocument().getBody().addNewSectPr();
        CTPageMar pageMar = sectPr.addNewPgMar();
        pageMar.setLeft(BigInteger.valueOf(1440));
        pageMar.setRight(BigInteger.valueOf(1440));
        pageMar.setTop(BigInteger.valueOf(1440));
        pageMar.setBottom(BigInteger.valueOf(1440));

        addHeader(document);
        addTitle(document);
        addParties(document, conventie);
        addArticles(document, conventie);
        addSignatures(document, conventie);
        addAnnex(document, conventie);

        return document;
    }

    private void addHeader(XWPFDocument document) {
        XWPFParagraph headerPara = document.createParagraph();
        headerPara.setAlignment(ParagraphAlignment.RIGHT);
        XWPFRun headerRun = headerPara.createRun();
        headerRun.setBold(true);
        headerRun.setText("ANEXA 3");
        headerRun.addBreak();
        headerRun.setText("Nr. _____ / " + new SimpleDateFormat("dd.MM.yyyy").format(new java.util.Date()));
        headerRun.addBreak();
    }

    private void addTitle(XWPFDocument document) {
        XWPFParagraph titlePara = document.createParagraph();
        titlePara.setAlignment(ParagraphAlignment.CENTER);
        XWPFRun titleRun = titlePara.createRun();
        titleRun.setBold(true);
        titleRun.setFontSize(14);
        titleRun.setText("CONVENȚIE-CADRU");
        titleRun.addBreak();
        titleRun.setText("privind efectuarea stagiului de practică în cadrul");
        titleRun.addBreak();
        titleRun.setText("programelor de studii universitare de licență sau masterat");
        titleRun.addBreak();
        titleRun.addBreak();
    }

    private void addParties(XWPFDocument document, Conventie conventie) {
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd.MM.yyyy");

        addParagraph(document, "Prezenta convenție-cadru se încheie între:");

        // UPT
        addParagraph(document, "1. Universitatea Politehnica Timișoara, reprezentată de Rector, " +
                     "conf. univ. dr. ing. Florin DRĂGAN, cu sediul în TIMIȘOARA, Piața Victoriei, Nr. 2, " +
                     "cod 300006, telefon: 0256-403011, email: rector@upt.ro, " +
                     "cod unic de înregistrare: 4269282, denumită în continuare organizator de practică");

        // Companie
        addParagraph(document, "2. " + conventie.getCompanie().getNume() + ", " +
                     "reprezentată de " + conventie.getCompanie().getReprezentant() +
                     " în calitate de " + conventie.getCompanie().getCalitate() +
                     ", cu sediul în " + conventie.getCompanie().getAdresa() +
                     ", telefon " + conventie.getCompanie().getTelefon() +
                     ", denumită în continuare partener de practică");

        // Student
        addParagraph(document, "3. Student " + conventie.getStudent().getNume() + " " + 
                     conventie.getStudent().getPrenume() + ", " +
                     "CNP " + conventie.getStudent().getCnp() +
                     ", data nașterii " + dateFormat.format(conventie.getStudent().getDataNasterii()) +
                     ", locul nașterii " + conventie.getStudent().getLoculNasterii() +
                     ", cetățenie " + conventie.getStudent().getCetatenie() +
                     ", CI seria " + conventie.getStudent().getSerieCi() +
                     " nr. " + conventie.getStudent().getNumarCi() +
                     ", adresa " + conventie.getStudent().getAdresa() +
                     ", înscris în anul universitar " + conventie.getStudent().getAnUniversitar() +
                     ", facultatea " + conventie.getStudent().getFacultate() +
                     ", specializarea " + conventie.getStudent().getSpecializare() +
                     ", anul de studiu " + conventie.getStudent().getAnDeStudiu() +
                     ", email " + conventie.getStudent().getEmail() +
                     ", telefon " + conventie.getStudent().getTelefon() +
                     ", denumit în continuare practicant");
    }

    private void addArticles(XWPFDocument document, Conventie conventie) {
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd.MM.yyyy");

        // Art. 1
        addArticleTitle(document, "Art. 1. Obiectul convenției-cadru");
        addParagraph(document, "(1) Convenția-cadru stabilește modul în care se organizează și se " +
                     "desfășoară stagiul de practică în vederea consolidării cunoștințelor teoretice și " +
                     "formarea abilităților practice, spre a le aplica în concordanță cu specializarea pentru " +
                     "care se instruiește studentul practicant.");
        addParagraph(document, "(2) Stagiul de practică este realizat de practicant în vederea dobândirii " +
                     "competențelor profesionale menționate în Portofoliul de practică care este corelat cu fișa disciplinei de practică, " +
                     "parte integrantă a prezentei convenții. " +
                     "Locul desfășurării stagiului de practică este: " + conventie.getLoculDesfasurarii());
        addParagraph(document, "(3) Modalitățile de derulare și conținutul stagiului de practică sunt descrise în " +
                     "prezenta convenție-cadru și în portofoliul de practică din anexă.");

        // Art. 2
        addArticleTitle(document, "Art. 2. Statutul practicantului");
        addParagraph(document, "Practicantul rămâne, pe toată durata stagiului de pregătire practică, student al " +
                     "Universității Politehnica Timișoara.");

        // Art. 3
        addArticleTitle(document, "Art. 3. Durata și perioada desfășurării stagiului de practică");
        addParagraph(document, "(1) Durata stagiului de practică, precizată în planul de învățământ, este de " +
                     conventie.getDurataInPlanulDeInvatamant() + " [h].");
        addParagraph(document, "(2) Perioada desfășurării stagiului de practică este conformă structurii anului universitar curent " +
                     "de la " + dateFormat.format(conventie.getDataInceput()) +
                     " până la " + dateFormat.format(conventie.getDataSfarsit()));

        addArticleTitle(document, "Art. 4. Plata și obligațiile sociale");
        addParagraph(document, "(1) Stagiul de pregătire practică (se bifează situația corespunzătoare):");
        addParagraph(document, "☐ - se efectuează în cadrul unui contract de muncă, cei doi parteneri putând să beneficieze " +
                     "de prevederile Legii nr. 72/2007 privind stimularea încadrării în muncă a elevilor și studenților;");
        addParagraph(document, "☐ - nu se efectuează în cadrul unui contract de muncă;");
        addParagraph(document, "☐ - se efectuează în cadrul unui proiect finanțat prin Fondul Social European;");
        addParagraph(document, "☐ - se efectuează în cadrul proiectului ...");
        addParagraph(document, "(2) În cazul angajării ulterioare, perioada stagiului nu va fi considerată ca vechime " +
                     "în muncă în situația în care convenția nu se derulează în cadrul unui contract de muncă.");
        addParagraph(document, "(3) Practicantul nu poate pretinde un salariu din partea partenerului de practică, cu " +
                     "excepția situației în care practicantul are statut de angajat.");
        addParagraph(document, "(4) Partenerul de practică poate totuși acorda practicantului o indemnizație, " +
                     "gratificare, primă sau avantaje în natură, conform legislației în vigoare.");

        // Art. 5
        addArticleTitle(document, "Art. 5. Responsabilitățile practicantului");
        addParagraph(document, "(1) Practicantul are obligația, ca pe durata derulării stagiului de practică, să " +
                     "respecte programul de lucru stabilit și să execute activitățile specificate de tutore " +
                     "în conformitate cu portofoliul de practică, în condițiile respectării cadrului legal cu " +
                     "privire la volumul și dificultatea acestora.");
        addParagraph(document, "(2) Pe durata stagiului, practicantul respectă regulamentul de ordine interioară al " +
                     "partenerului de practică. În cazul nerespectării acestui regulament, conducătorul " +
                     "partenerului de practică își rezervă dreptul de a anula convenția-cadru, după ce în " +
                     "prealabil a ascultat punctul de vedere al practicantului și al îndrumătorului de " +
                     "practică și a înștiințat conducătorul facultății unde practicantul este înmatriculat " +
                     "și după primirea confirmării de primire a acestei informații. Această situație conduce la refacerea stagiului de practică și la susținerea unui nou colocviu de evaluare în anul universitar următor.");
        addParagraph(document, "(3) Practicantul are obligația de a respecta normele de securitate și sănătate în " +
                     "muncă pe care le-a însușit de la reprezentantul partenerului de practică înainte de " +
                     "începerea stagiului de practică.");
        addParagraph(document, "(4) Practicantul se angajează să nu folosească, în niciun caz, informațiile la care " +
                     "are acces în timpul stagiului despre partenerul de practică sau clienții săi, pentru a " +
                     "le comunica unui terț sau pentru a le publica, chiar după terminarea stagiului, decât " +
                     "cu acordul respectivului partener de practică.");

        // Art. 6
        addArticleTitle(document, "Art. 6. Responsabilitățile partenerului de practică");
        addParagraph(document, "(1) Partenerul de practică va stabili un tutore pentru stagiul de practică, " +
                     "selectat dintre salariații proprii și ale cărui obligații sunt menționate în portofoliul " +
                     "de practică, parte integrantă a convenției-cadru.");
        addParagraph(document, "(2) În cazul nerespectării obligațiilor de către practicant, tutorele va contacta " +
                     "cadrul didactic supervizor, responsabil de practică, aplicându-se sancțiuni conform " +
                     "legilor și regulamentelor în vigoare.");
        addParagraph(document, "(3) Înainte de începerea stagiului de practică, partenerul are obligația de a face " +
                     "practicantului instructajul cu privire la normele de securitate și sănătate în muncă, " +
                     "pentru fiecare loc distinct de practică, în conformitate cu legislația în vigoare. " +
                     "Printre responsabilitățile sale, partenerul de practică va lua măsurile necesare pentru " +
                     "securitatea și sănătatea în muncă a practicantului, precum și pentru comunicarea " +
                     "regulilor de prevenire a riscurilor profesionale.");
        addParagraph(document, "(4) Partenerul de practică trebuie să pună la dispoziția practicantului toate " +
                     "mijloacele necesare pentru desfășurarea activităților precizate în portofoliul de practică.");
        addParagraph(document, "(5) Partenerul de practică are obligația de a asigura practicantului accesul liber " +
                     "la serviciul de medicina muncii, pe durata derulării pregătirii practice.");
        addParagraph(document, "(6) În urma desfășurării cu succes a stagiului, partenerul de practică va acorda " +
                     "studentului, la cerere, o adeverință constatatoare.");

        // Art. 7
        addArticleTitle(document, "Art. 7. Obligațiile organizatorului de practică");
        addParagraph(document, "(1) Organizatorul de practică desemnează un cadru didactic supervizor, responsabil " +
                     "cu planificarea, organizarea și supravegherea desfășurării pregătirii practice. " +
                     "Cadrul didactic supervizor responsabil de practică, împreună cu tutorele desemnat de " +
                     "partenerul de practică stabilesc tematica de practică și competențele profesionale " +
                     "care fac obiectul stagiului de pregătire practică.");
        addParagraph(document, "(2) În cazul în care derularea stagiului de pregătire practică nu este conformă cu " +
                     "angajamentele luate de către partenerul de practică în cadrul prezentei convenții, " +
                     "conducătorul organizatorului de practică poate decide întreruperea stagiului de " +
                     "pregătire practică conform convenției-cadru, după informarea prealabilă a " +
                     "conducătorului partenerului de practică și după primirea confirmării de primire a " +
                     "acestei informații.");

        // Art. 8
        addArticleTitle(document, "Art. 8. Persoane desemnate de organizatorul de practică și partenerul de practică");
        addParagraph(document, "(1) Tutorele (persoana care va avea responsabilitatea practicantului din partea partenerului de practică):");
        addParagraph(document, "Dl/Dna " + conventie.getTutore().getNume() + " " + conventie.getTutore().getPrenume());
        addParagraph(document, "Funcția: " + conventie.getTutore().getFunctie());
        addParagraph(document, "Telefon: " + conventie.getTutore().getTelefon());
        addParagraph(document, "Email: " + conventie.getTutore().getEmail());
        
        addParagraph(document, "(2) Cadrul didactic supervizor, responsabil cu urmărirea derulării stagiului de practică din partea organizatorului de practică:");
        addParagraph(document, "Dl/Dna " + conventie.getCadruDidactic().getNume() + " " + conventie.getCadruDidactic().getPrenume());
        addParagraph(document, "Funcția: " + conventie.getCadruDidactic().getFunctie());
        addParagraph(document, "Telefon: " + conventie.getCadruDidactic().getTelefon());
        addParagraph(document, "Email: " + conventie.getCadruDidactic().getEmail());

        // Art. 9
        addArticleTitle(document, "Art. 9. Evaluarea stagiului de pregătire practică prin credite transferabile");
        addParagraph(document, "Numărul de credite transferabile ce vor fi obținute în urma desfășurării stagiului " +
                     "de practică este de " + conventie.getNumarCredite() + ".");

        // Art. 10
        addArticleTitle(document, "Art. 10. Raportul privind stagiul de pregătire practică");
        addParagraph(document, "(1) În timpul derulării stagiului de practică, tutorele, împreună cu cadrul " +
                     "didactic supervizor, vor evalua practicantul în permanență. Vor fi monitorizate și " +
                     "evaluate atât nivelul de dobândire a competențelor profesionale, cât și " +
                     "comportamentul și modalitatea de integrare a practicantului în activitatea " +
                     "partenerului de practică (disciplină, punctualitate, responsabilitate în rezolvarea " +
                     "sarcinilor, respectarea regulamentului de ordine interioară al partenerului de practică).");
        addParagraph(document, "(2) La finalul stagiului de practică, tutorele completează atestatul de practică " +
                     "și opțional fișa de evaluare, pe baza evaluării nivelului de dobândire a " +
                     "competențelor de către practicant. Rezultatul acestei evaluări va sta la baza notării " +
                     "practicantului de către cadrul didactic supervizor.");
        addParagraph(document, "La finalul stagiului de practică, studentul elaborează un caiet de practică, " +
                     "însușit și de tutorele din partea partenerului de practică. Atestatul și fișa de " +
                     "evaluare completate de tutore vor sta la baza notării studentului conform " +
                     "Regulamentului cadru de organizare și desfășurare a practicii studenților în UPT.");

        // Art. 11
        addArticleTitle(document, "Art. 11. Sănătatea și securitatea în muncă");
        addParagraph(document, "(1) Practicantul anexează prezentului contract dovada asigurării medicale " +
                     "valabile în perioada și pe teritoriul statului unde se desfășoară stagiul de practică.");
        addParagraph(document, "(2) Partenerul de practică are obligația respectării prevederilor legale cu " +
                     "privire la sănătatea și securitatea în muncă a practicatului pe durata stagiului de practică.");
        addParagraph(document, "(3) Practicantului i se asigură protecție socială conform legislației în vigoare. " +
                     "Ca urmare, conform dispozițiilor Legii nr. 346/2002 privind asigurările pentru " +
                     "accidente de muncă și boli profesionale, cu modificările și completările ulterioare, " +
                     "practicantul beneficiază de legislația privitoare la accidentele de muncă pe toată " +
                     "durata efectuării pregătirii practice.");
        addParagraph(document, "(4) În cazul unui accident suferit de practicant, fie în cursul lucrului, fie în " +
                     "timpul deplasării la lucru, partenerul de practică se angajează să înștiințeze " +
                     "asiguratorul cu privire la accidentul care a avut loc.");

        // Art. 12
        addArticleTitle(document, "Art. 12. Condiții facultative de desfășurare a stagiului de pregătire practică");
        addParagraph(document, "(1) Îndemnizație, gratificări sau prime acordate practicantului:");
        addParagraph(document, conventie.getIndemnizatii() != null ? conventie.getIndemnizatii() : "Nu este cazul");
        addParagraph(document, "(2) Avantaje eventuale (plata transportului de la și la locul desfășurării stagiului de practică, " +
                     "tichete de masă, acces la cantina partenerului de practică etc.):");
        addParagraph(document, conventie.getAvantaje() != null ? conventie.getAvantaje() : "Nu este cazul");
        addParagraph(document, "(3) Alte precizări:");
        addParagraph(document, conventie.getAltePrecizari() != null ? conventie.getAltePrecizari() : "Nu este cazul");

        // Art. 13
        addArticleTitle(document, "Art. 13. Prevederi finale");
        addParagraph(document, "Această convenție-cadru s-a încheiat în trei exemplare la data: " + 
                     dateFormat.format(conventie.getDataIntocmirii()));
    }

    private void addSignatures(XWPFDocument document, Conventie conventie) {
        document.createParagraph().createRun().addBreak();
        
        XWPFTable table = document.createTable(2, 3);
        table.setWidth("100%");
        
        // Prima linie - titluri
        XWPFTableRow headerRow = table.getRow(0);
        setCellText(headerRow.getCell(0), "Universitatea Politehnica Timișoara\nRector");
        setCellText(headerRow.getCell(1), conventie.getCompanie().getNume());
        setCellText(headerRow.getCell(2), "Student");
        
        // A doua linie - nume și spațiu semnătură
        XWPFTableRow sigRow = table.getRow(1);
        setCellText(sigRow.getCell(0), 
            "conf. univ. dr. ing. Florin DRĂGAN\n\nSemnătura: ____________\nData: ____________");
        setCellText(sigRow.getCell(1), 
            conventie.getCompanie().getReprezentant() + "\n\nSemnătura: ____________\nData: ____________");
        setCellText(sigRow.getCell(2), 
            conventie.getStudent().getNume() + " " + conventie.getStudent().getPrenume() + 
            "\n\nSemnătura: ____________\nData: ____________");
    }

    private void addAnnex(XWPFDocument document, Conventie conventie) {
        document.createParagraph().createRun().addBreak();
        
        addArticleTitle(document, "ANEXĂ LA CONVENȚIA-CADRU");
        addArticleTitle(document, "PORTOFOLIU DE PRACTICĂ");
        
        // Adaugă detaliile portofoliului...
    }

    private void addArticleTitle(XWPFDocument document, String title) {
        XWPFParagraph para = document.createParagraph();
        XWPFRun run = para.createRun();
        run.setBold(true);
        run.setText(title);
        run.addBreak();
    }

    private void addParagraph(XWPFDocument document, String text) {
        XWPFParagraph para = document.createParagraph();
        para.setIndentationLeft(720); // 0.5 inch indent
        XWPFRun run = para.createRun();
        run.setText(text);
        run.addBreak();
    }

    private void setCellText(XWPFTableCell cell, String text) {
        XWPFParagraph para = cell.getParagraphs().get(0);
        para.setAlignment(ParagraphAlignment.CENTER);
        XWPFRun run = para.createRun();
        run.setText(text);
    }
    
    private String generateConventieHtml(Conventie conventie) {
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

        // Header
        html.append("<div class=\"header\">")
            .append("<p><strong>ANEXA 3</strong></p>")
            .append("Nr. _____ / ").append(dateFormat.format(new java.util.Date()))
            .append("</div>");

        // Titlu
        html.append("<h1>CONVENȚIE-CADRU</h1>")
            .append("<h2>privind efectuarea stagiului de practică în cadrul programelor de studii universitare<br>")
            .append("de licență sau masterat</h2>");

        // Părți contractante
        html.append("<div class=\"content\">")
            .append("<p>Prezenta convenție-cadru se încheie între:</p>")
            .append("<p><strong>1. Universitatea Politehnica Timișoara</strong>, reprezentată de Rector, ")
            .append("conf. univ. dr. ing. Florin DRĂGAN, cu sediul în TIMIȘOARA, Piața Victoriei, Nr. 2, ")
            .append("cod 300006, telefon: 0256-403011, email: rector@upt.ro, ")
            .append("cod unic de înregistrare: 4269282, denumită în continuare <strong>organizator de practică</strong>,</p>");

        // Companie
        html.append("<p><strong>2. ").append(conventie.getCompanie().getNume()).append("</strong>, ")
            .append("reprezentată de ").append(conventie.getCompanie().getReprezentant())
            .append(" în calitate de ").append(conventie.getCompanie().getCalitate())
            .append(", cu sediul în ").append(conventie.getCompanie().getAdresa())
            .append(", telefon ").append(conventie.getCompanie().getTelefon())
            .append(", denumită în continuare <strong>partener de practică</strong>,</p>");

        // Student
        html.append("<p><strong>3. Student ").append(conventie.getStudent().getNume())
            .append(" ").append(conventie.getStudent().getPrenume()).append("</strong>, ")
            .append("CNP ").append(conventie.getStudent().getCnp())
            .append(", data nașterii ").append(dateFormat.format(conventie.getStudent().getDataNasterii()))
            .append(", locul nașterii ").append(conventie.getStudent().getLoculNasterii())
            .append(", cetățenie ").append(conventie.getStudent().getCetatenie())
            .append(", CI seria ").append(conventie.getStudent().getSerieCi())
            .append(" nr. ").append(conventie.getStudent().getNumarCi())
            .append(", adresa ").append(conventie.getStudent().getAdresa())
            .append(", înscris în anul universitar ").append(conventie.getStudent().getAnUniversitar())
            .append(", Universitatea Politehnica Timișoara, ")
            .append("facultatea ").append(conventie.getStudent().getFacultate())
            .append(", specializarea ").append(conventie.getStudent().getSpecializare())
            .append(", anul de studiu ").append(conventie.getStudent().getAnDeStudiu())
            .append(", email ").append(conventie.getStudent().getEmail())
            .append(", telefon ").append(conventie.getStudent().getTelefon())
            .append(", denumit în continuare <strong>practicant</strong></p></div>");

        // Articolul 1
        html.append("<h3>Art. 1. Obiectul convenției-cadru</h3>")
            .append("<p>(1) <em>Convenția-cadru</em> stabilește modul în care se organizează și se ")
            .append("desfășoară stagiul de practică în vederea consolidării cunoștințelor teoretice și ")
            .append("formarea abilităților practice, spre a le aplica în concordanță cu specializarea pentru ")
            .append("care se instruiește studentul practicant.</p>")
            .append("<p>(2) Stagiul de practică este realizat de practicant în vederea dobândirii ")
            .append("competențelor profesionale menționate în Portofoliul de practică care este corelat cu fișa disciplinei de practică, ")
            .append("parte integrantă a prezentei convenții. ")
            .append("Locul desfășurării stagiului de practică este: ").append(conventie.getLoculDesfasurarii()).append("</p>")
            .append("<p>(3) Modalitățile de derulare și conținutul stagiului de practică sunt descrise în ")
            .append("prezenta convenție-cadru și în portofoliul de practică din anexă.</p>");

        // Articolul 2
        html.append("<h3>Art. 2. Statutul practicantului</h3>")
            .append("<p>Practicantul rămâne, pe toată durata stagiului de pregătire practică, student al ")
            .append("Universității Politehnica Timișoara.</p>");

        // Articolul 3
        html.append("<h3>Art. 3. Durata și perioada desfășurării stagiului de practică</h3>")
            .append("<p>(1) Durata stagiului de practică, precizată în planul de învățământ, este de ")
            .append(conventie.getDurataInPlanulDeInvatamant()).append(" [h].</p>")
            .append("<p>(2) Perioada desfășurării stagiului de practică este conformă structurii anului universitar curent ")
            .append("de la ").append(dateFormat.format(conventie.getDataInceput()))
            .append(" până la ").append(dateFormat.format(conventie.getDataSfarsit())).append("</p>");

        // Articolul 4
        html.append("<h3>Art. 4. Plata și obligațiile sociale</h3>")
            .append("<p>(1) Stagiul de pregătire practică (se bifează situația corespunzătoare):</p>")
            .append("<p>☐ - se efectuează în cadrul unui contract de muncă, cei doi parteneri putând să beneficieze ")
            .append("de prevederile Legii nr. 72/2007 privind stimularea încadrării în muncă a elevilor și studenților;</p>")
            .append("<p>☐ - nu se efectuează în cadrul unui contract de muncă;</p>")
            .append("<p>☐ - se efectuează în cadrul unui proiect finanțat prin Fondul Social European;</p>")
            .append("<p>☐ - se efectuează în cadrul proiectului ...</p>")
            .append("<p>(2) În cazul angajării ulterioare, perioada stagiului nu va fi considerată ca vechime ")
            .append("în muncă în situația în care convenția nu se derulează în cadrul unui contract de muncă.</p>")
            .append("<p>(3) Practicantul nu poate pretinde un salariu din partea partenerului de practică, cu ")
            .append("excepția situației în care practicantul are statut de angajat.</p>")
            .append("<p>(4) Partenerul de practică poate totuși acorda practicantului o indemnizație, ")
            .append("gratificare, primă sau avantaje în natură, conform legislației în vigoare.</p>");

        // Articolul 5
        html.append("<h3>Art. 5. Responsabilitățile practicantului</h3>")
            .append("<p>(1) Practicantul are obligația, ca pe durata derulării stagiului de practică, să ")
            .append("respecte programul de lucru stabilit și să execute activitățile specificate de tutore ")
            .append("în conformitate cu portofoliul de practică, în condițiile respectării cadrului legal cu ")
            .append("privire la volumul și dificultatea acestora.</p>")
            .append("<p>(2) Pe durata stagiului, practicantul respectă regulamentul de ordine interioară al ")
            .append("partenerului de practică. În cazul nerespectării acestui regulament, conducătorul ")
            .append("partenerului de practică își rezervă dreptul de a anula convenția-cadru, după ce în ")
            .append("prealabil a ascultat punctul de vedere al practicantului și al îndrumătorului de ")
            .append("practică și a înștiințat conducătorul facultății unde practicantul este înmatriculat ")
            .append("și după primirea confirmării de primire a acestei informații. Această situație conduce la refacerea stagiului de practică și la susținerea unui nou colocviu de evaluare în anul universitar următor.</p>")
            .append("<p>(3) Practicantul are obligația de a respecta normele de securitate și sănătate în ")
            .append("muncă pe care le-a însușit de la reprezentantul partenerului de practică înainte de ")
            .append("începerea stagiului de practică.</p>")
            .append("<p>(4) Practicantul se angajează să nu folosească, în niciun caz, informațiile la care ")
            .append("are acces în timpul stagiului despre partenerul de practică sau clienții săi, pentru a ")
            .append("le comunica unui terț sau pentru a le publica, chiar după terminarea stagiului, decât ")
            .append("cu acordul respectivului partener de practică.</p>");

        // Articolul 6
        html.append("<h3>Art. 6. Responsabilitățile partenerului de practică</h3>")
            .append("<p>(1) Partenerul de practică va stabili un <em>tutore pentru stagiul de practică</em>, ")
            .append("selectat dintre salariații proprii și ale cărui obligații sunt menționate în portofoliul ")
            .append("de practică, parte integrantă a convenției-cadru.</p>")
            .append("<p>(2) În cazul nerespectării obligațiilor de către practicant, tutorele va contacta ")
            .append("cadrul didactic supervizor, responsabil de practică, aplicându-se sancțiuni conform ")
            .append("legilor și regulamentelor în vigoare.</p>")
            .append("<p>(3) Înainte de începerea stagiului de practică, partenerul are obligația de a face ")
            .append("practicantului instructajul cu privire la normele de securitate și sănătate în muncă, ")
            .append("pentru fiecare loc distinct de practică, în conformitate cu legislația în vigoare. ")
            .append("Printre responsabilitățile sale, partenerul de practică va lua măsurile necesare pentru ")
            .append("securitatea și sănătatea în muncă a practicantului, precum și pentru comunicarea ")
            .append("regulilor de prevenire a riscurilor profesionale.</p>")
            .append("<p>(4) Partenerul de practică trebuie să pună la dispoziția practicantului toate ")
            .append("mijloacele necesare pentru desfășurarea activităților precizate în portofoliul de practică.</p>")
            .append("<p>(5) Partenerul de practică are obligația de a asigura practicantului accesul liber ")
            .append("la serviciul de medicina muncii, pe durata derulării pregătirii practice.</p>")
            .append("<p>(6) În urma desfășurării cu succes a stagiului, partenerul de practică va acorda ")
            .append("studentului, la cerere, o adeverință constatatoare.</p>");

        // Articolul 7
        html.append("<h3>Art. 7. Obligațiile organizatorului de practică</h3>")
            .append("<p>(1) Organizatorul de practică desemnează un cadru didactic supervizor, responsabil ")
            .append("cu planificarea, organizarea și supravegherea desfășurării pregătirii practice. ")
            .append("Cadrul didactic supervizor responsabil de practică, împreună cu tutorele desemnat de ")
            .append("partenerul de practică stabilesc tematica de practică și competențele profesionale ")
            .append("care fac obiectul stagiului de pregătire practică.</p>")
            .append("<p>(2) În cazul în care derularea stagiului de pregătire practică nu este conformă cu ")
            .append("angajamentele luate de către partenerul de practică în cadrul prezentei convenții, ")
            .append("conducătorul organizatorului de practică poate decide întreruperea stagiului de ")
            .append("pregătire practică conform convenției-cadru, după informarea prealabilă a ")
            .append("conducătorului partenerului de practică și după primirea confirmării de primire a ")
            .append("acestei informații.</p>");

        // Articolul 8
        html.append("<h3>Art. 8. Persoane desemnate de organizatorul de practică și partenerul de practică</h3>")
            .append("<p>(1) <strong>Tutorele</strong> (persoana care va avea responsabilitatea practicantului din partea partenerului de practică):</p>")
            .append("<p>Dl/Dna ").append(conventie.getTutore().getNume()).append(" ").append(conventie.getTutore().getPrenume()).append("<br>")
            .append("Funcția: ").append(conventie.getTutore().getFunctie()).append("<br>")
            .append("Telefon: ").append(conventie.getTutore().getTelefon()).append("<br>")
            .append("Email: ").append(conventie.getTutore().getEmail()).append("</p>")
            .append("<p>(2) <strong>Cadrul didactic supervizor</strong>, responsabil cu urmărirea derulării stagiului de practică din partea organizatorului de practică:</p>")
            .append("<p>Dl/Dna ").append(conventie.getCadruDidactic().getNume()).append(" ").append(conventie.getCadruDidactic().getPrenume()).append("<br>")
            .append("Funcția: ").append(conventie.getCadruDidactic().getFunctie()).append("<br>")
            .append("Telefon: ").append(conventie.getCadruDidactic().getTelefon()).append("<br>")
            .append("Email: ").append(conventie.getCadruDidactic().getEmail()).append("</p>");

        // Articolul 9
        html.append("<h3>Art. 9. Evaluarea stagiului de pregătire practică prin credite transferabile</h3>")
            .append("<p>Numărul de credite transferabile ce vor fi obținute în urma desfășurării stagiului ")
            .append("de practică este de ").append(conventie.getNumarCredite()).append(".</p>");

        // Articolul 10
        html.append("<h3>Art. 10. Raportul privind stagiul de pregătire practică</h3>")
            .append("<p>(1) În timpul derulării stagiului de practică, tutorele, împreună cu cadrul ")
            .append("didactic supervizor, vor evalua practicantul în permanență. Vor fi monitorizate și ")
            .append("evaluate atât nivelul de dobândire a competențelor profesionale, cât și ")
            .append("comportamentul și modalitatea de integrare a practicantului în activitatea ")
            .append("partenerului de practică (disciplină, punctualitate, responsabilitate în rezolvarea ")
            .append("sarcinilor, respectarea regulamentului de ordine interioară al partenerului de practică).</p>")
            .append("<p>(2) La finalul stagiului de practică, tutorele completează atestatul de practică ")
            .append("și opțional fișa de evaluare, pe baza evaluării nivelului de dobândire a ")
            .append("competențelor de către practicant. Rezultatul acestei evaluări va sta la baza notării ")
            .append("practicantului de către cadrul didactic supervizor.</p>")
            .append("<p>La finalul stagiului de practică, studentul elaborează un caiet de practică, ")
            .append("însușit și de tutorele din partea partenerului de practică. Atestatul și fișa de ")
            .append("evaluare completate de tutore vor sta la baza notării studentului conform ")
            .append("<em>Regulamentului cadru de organizare și desfășurare a practicii studenților în UPT.</em></p>")
            .append("<p>(3) Periodic și după încheierea stagiului de practică, practicantul va prezenta un ")
            .append("<em>caiet de practică</em> care va cuprinde:</p>")
            .append("<ul>")
            .append("<li>denumirea modulului de pregătire;</li>")
            .append("<li>competențe exersate;</li>")
            .append("<li>activități desfășurate pe perioada stagiului de practică;</li>")
            .append("<li>observații personale privitoare la activitatea depusă.</li>")
            .append("</ul>")
            .append("<p>(4) Pentru studiile de licență, în urma unui colocviu susținut în instituția de ")
            .append("învățământ superior, pe baza documentelor de practică, calificativul foarte bine/ ")
            .append("bine/ satisfăcător emis de instituția gazdă se omologhează cu calificativul ")
            .append("<em>promovat</em> în catalogul disciplinei practică, iar calificativul nesatisfăcător ")
            .append("emis de instituția gazdă se omologhează cu calificativul <em>nepromovat</em> în ")
            .append("catalogul disciplinei practică.</p>")
            .append("<p>(5) Pentru studiile de master, în urma unui colocviu susținut în instituția de ")
            .append("învățământ superior, pe baza documentelor de practică, calificativul foarte bine/ ")
            .append("bine/ satisfăcător emis de instituția gazdă se echivalează cu note de promovare în ")
            .append("catalogul disciplinei practică (5-10), iar calificativul nesatisfăcător emis de ")
            .append("instituția gazdă se echivalează cu note de nepromovare în catalogul disciplinei practică.</p>");

        // Articolul 11
        html.append("<h3>Art. 11. Sănătatea și securitatea în muncă</h3>")
            .append("<p>(1) Practicantul anexează prezentului contract dovada asigurării medicale ")
            .append("valabile în perioada și pe teritoriul statului unde se desfășoară stagiul de practică.</p>")
            .append("<p>(2) Partenerul de practică are obligația respectării prevederilor legale cu ")
            .append("privire la sănătatea și securitatea în muncă a practicatului pe durata stagiului de practică.</p>")
            .append("<p>(3) Practicantului i se asigură protecție socială conform legislației în vigoare. ")
            .append("Ca urmare, conform dispozițiilor Legii nr. 346/2002 privind asigurările pentru ")
            .append("accidente de muncă și boli profesionale, cu modificările și completările ulterioare, ")
            .append("practicantul beneficiază de legislația privitoare la accidentele de muncă pe toată ")
            .append("durata efectuării pregătirii practice.</p>")
            .append("<p>(4) În cazul unui accident suferit de practicant, fie în cursul lucrului, fie în ")
            .append("timpul deplasării la lucru, partenerul de practică se angajează să înștiințeze ")
            .append("asiguratorul cu privire la accidentul care a avut loc.</p>");

        // Articolul 12
        html.append("<h3>Art. 12. Condiții facultative de desfășurare a stagiului de pregătire practică</h3>")
            .append("<p>(1) Îndemnizație, gratificări sau prime acordate practicantului:</p>")
            .append("<p>").append(conventie.getIndemnizatii()).append("</p>")
            .append("<p>(2) Avantaje eventuale (plata transportului de la și la locul desfășurării stagiului de practică, ")
            .append("tichete de masă, acces la cantina partenerului de practică etc.):</p>")
            .append("<p>").append(conventie.getAvantaje()).append("</p>")
            .append("<p>(3) Alte precizări:</p>")
            .append("<p>").append(conventie.getAltePrecizari()).append("</p>");

        // Articolul 13
        html.append("<h3>Art. 13. Prevederi finale</h3>")
            .append("<p>Această convenție-cadru s-a încheiat în trei exemplare la data: ")
            .append(dateFormat.format(conventie.getDataIntocmirii())).append("</p>");

        // Tabel semnături
        html.append("<table class='signature-table'>")
            .append("<tr>")
            .append("<th>Universitatea Politehnica Timișoara<br>Rector</th>")
            .append("<th>").append(conventie.getCompanie().getNume()).append("<br>")
            .append(conventie.getCompanie().getReprezentant()).append("</th>")
            .append("<th>Student<br>")
            .append(conventie.getStudent().getNume()).append(" ")
            .append(conventie.getStudent().getPrenume()).append("</th>")
            .append("</tr>")
            .append("<tr>")
            .append("<td>Conf. univ. dr. ing. Florin DRĂGAN<br><br>Semnătura: ____________<br>Data: ____________</td>")
            .append("<td>Semnătura: ____________<br>Data: ____________</td>")
            .append("<td>Semnătura: ____________<br>Data: ____________</td>")
            .append("</tr>")
            .append("</table>");

        // Am luat la cunoștință
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
            .append("Funcția: ").append(conventie.getTutore().getFunctie()).append("<br><br>")
            .append("Semnătura: ____________<br>")
            .append("Data: ____________</td>")
            .append("</tr>")
            .append("</table>");

        html.append("</body></html>");
        return html.toString();
    }
    
    
}