// TutoreRepository.java
package ro.upt.ac.conventii.tutore;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import ro.upt.ac.conventii.companie.Companie;

import java.util.List;
import java.util.Optional;

@Repository
public interface TutoreRepository extends JpaRepository<Tutore, Integer> {
	   @Query("SELECT t FROM Tutore t WHERE t.id = :id")
	    Tutore findTutoreById(@Param("id") int id);
	    
	    // Metodă pentru căutare după email
	   Optional<Tutore> findByEmail(String email);
	    
	    List<Tutore> findByCompanie(Companie companie);
	    List<Tutore> findByCompanieId(int companieId);
}