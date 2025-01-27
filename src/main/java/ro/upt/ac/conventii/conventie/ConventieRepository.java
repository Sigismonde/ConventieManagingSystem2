package ro.upt.ac.conventii.conventie;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import ro.upt.ac.conventii.cadruDidactic.CadruDidactic;
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
    
    @Query("SELECT c FROM Conventie c WHERE c.student.email = ?1 ORDER BY c.dataIntocmirii DESC")
    List<Conventie> findTop3ByStudentEmailOrderByDataIntocmiriiDesc(String email, Pageable pageable);
    
    @Query("SELECT c FROM Conventie c WHERE c.status = ?1 ORDER BY c.dataIntocmirii DESC")
    List<Conventie> findTop5ByStatusOrderByDataIntocmiriiDesc(ConventieStatus status, Pageable pageable);
    
    @Query("SELECT c FROM Conventie c WHERE c.status = ?1 ORDER BY c.dataIntocmirii DESC")
	List<Conventie> findTop3ByStatusOrderByDataIntocmiriiDesc(ConventieStatus inAsteptare, Pageable lastThree);
}