package ro.upt.ac.conventii.student;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;
import org.springframework.stereotype.Repository;

@Repository
public interface StudentRepository extends JpaRepository<Student, Integer> {
    Student findById(int id);
    Optional<Student> findByEmail(String email);
}