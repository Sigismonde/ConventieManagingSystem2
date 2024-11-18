package ro.upt.ac.conventii.tutore;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TutoreRepository extends JpaRepository<Tutore,Integer>
{
	Tutore findById(int id);
}
