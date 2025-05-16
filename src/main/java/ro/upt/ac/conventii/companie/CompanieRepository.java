package ro.upt.ac.conventii.companie;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface CompanieRepository extends JpaRepository<Companie, Integer> {
    
    // IMPORTANT: Redenumim metoda să nu fie în conflict cu findById standard
    // Pentru metoda veche care returnează Companie direct
    @Query("SELECT c FROM Companie c WHERE c.id = :id")
    Companie findCompanieById(@Param("id") int id);
    
    // Metoda standard JPA care returnează Optional<Companie>
    // Aceasta este deja definită în JpaRepository, deci nu trebuie redeclarată
    // Optional<Companie> findById(Integer id);
}