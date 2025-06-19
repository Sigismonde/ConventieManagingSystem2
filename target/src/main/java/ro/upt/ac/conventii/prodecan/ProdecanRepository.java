package ro.upt.ac.conventii.prodecan;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ProdecanRepository extends JpaRepository<Prodecan, Integer> {
    Prodecan findById(int id);
    Prodecan findByEmail(String email);
}