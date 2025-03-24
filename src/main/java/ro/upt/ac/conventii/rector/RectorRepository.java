package ro.upt.ac.conventii.rector;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface RectorRepository extends JpaRepository<Rector, Integer> {
    Rector findById(int id);
    Rector findByEmail(String email);
}