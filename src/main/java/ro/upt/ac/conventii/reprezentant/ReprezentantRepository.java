package ro.upt.ac.conventii.reprezentant;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import ro.upt.ac.conventii.companie.Companie;

@Repository
public interface ReprezentantRepository extends JpaRepository<Reprezentant, Integer> {
    Reprezentant findById(int id);
    Optional<Reprezentant> findByEmail(String email);
    List<Reprezentant> findByCompanie(Companie companie);
    List<Reprezentant> findByCompanieId(int companieId);
}