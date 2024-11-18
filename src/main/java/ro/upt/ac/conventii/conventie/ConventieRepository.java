package ro.upt.ac.conventii.conventie;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
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
    long countByStatus(ConventieStatus status);
}