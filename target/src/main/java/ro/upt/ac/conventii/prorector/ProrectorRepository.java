package ro.upt.ac.conventii.prorector;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ProrectorRepository extends JpaRepository<Prorector, Integer> {
    Prorector findByEmail(String email);
    Prorector findById(int id);
}