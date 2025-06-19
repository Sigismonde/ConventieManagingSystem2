package ro.upt.ac.conventii.cadruDidactic;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CadruDidacticRepository extends JpaRepository<CadruDidactic,Integer>
{
	CadruDidactic findById(int id);
}
