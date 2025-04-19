// TutoreRepository.java
package ro.upt.ac.conventii.tutore;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ro.upt.ac.conventii.companie.Companie;

import java.util.List;
import java.util.Optional;

@Repository
public interface TutoreRepository extends JpaRepository<Tutore, Integer> {
    Tutore findById(int id);
    Optional<Tutore> findByEmail(String email);
    List<Tutore> findByCompanie(Companie companie);
    List<Tutore> findByCompanieId(int companieId);
}