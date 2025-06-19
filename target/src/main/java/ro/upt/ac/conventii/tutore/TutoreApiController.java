package ro.upt.ac.conventii.tutore;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/tutori")
public class TutoreApiController {

    @Autowired
    private TutoreRepository tutoreRepository;
    
    /**
     * Returnează toți tutorii asociați unei companii
     * 
     * @param companieId ID-ul companiei
     * @return Lista de tutori asociați companiei
     */
    @GetMapping("/by-companie/{companieId}")
    public ResponseEntity<List<Tutore>> getTutoriByCompanie(@PathVariable int companieId) {
        try {
            List<Tutore> tutori = tutoreRepository.findByCompanieId(companieId);
            return ResponseEntity.ok(tutori);
        } catch (Exception e) {
            // Loggăm eroarea și returnăm o listă goală în caz de excepție
            e.printStackTrace();
            return ResponseEntity.ok(new ArrayList<>());
        }
    }
}
