package ro.upt.ac.conventii.conventie;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import ro.upt.ac.conventii.cadruDidactic.CadruDidactic;
import ro.upt.ac.conventii.companie.Companie;
import ro.upt.ac.conventii.student.Student;
import java.util.List;

@Repository
public interface ConventieRepository extends JpaRepository<Conventie, Integer> {
    Conventie findById(int id);
    List<Conventie> findByStudentEmail(String email);
    
    // Metode noi pentru Prodecan
    List<Conventie> findByStatus(ConventieStatus status);
    List<Conventie> findByStatusOrderByDataIntocmiriiDesc(ConventieStatus status);
    List<Conventie> findTop10ByStatusOrderByDataIntocmiriiDesc(ConventieStatus status);
    List<Conventie> findByCadruDidactic(CadruDidactic cadruDidactic);
    List<Conventie> findByStudent(Student student);
    long countByStatus(ConventieStatus status);
    
    // Metode pentru Partner
    List<Conventie> findByCompanie(Companie companie);
    List<Conventie> findByCompanieId(int companieId);
    List<Conventie> findByCompanieAndStatus(Companie companie, ConventieStatus status);
    List<Conventie> findTop5ByCompanieAndStatusOrderByDataIntocmiriiDesc(Companie companie, ConventieStatus status, Pageable pageable);
    
 // Metode adăugate în ConventieRepository
    List<Conventie> findByStatusAndCompanieId(ConventieStatus status, int companieId);
    List<Conventie> findByStatusInAndCompanieId(List<ConventieStatus> statuses, int companieId);
    List<Conventie> findTop5ByStatusAndCompanieIdOrderByDataIntocmiriiDesc(ConventieStatus status, int companieId, Pageable pageable);
    
    @Query("SELECT c FROM Conventie c WHERE c.student.email = ?1 ORDER BY c.dataIntocmirii DESC")
    List<Conventie> findTop3ByStudentEmailOrderByDataIntocmiriiDesc(String email, Pageable pageable);
    
    @Query("SELECT c FROM Conventie c WHERE c.status = ?1 ORDER BY c.dataIntocmirii DESC")
    List<Conventie> findTop5ByStatusOrderByDataIntocmiriiDesc(ConventieStatus status, Pageable pageable);
    
    @Query("SELECT c FROM Conventie c WHERE c.status = ?1 ORDER BY c.dataIntocmirii DESC")
    List<Conventie> findTop3ByStatusOrderByDataIntocmiriiDesc(ConventieStatus inAsteptare, Pageable lastThree);
}