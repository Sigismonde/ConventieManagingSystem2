package ro.upt.ac.conventii.partner;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import ro.upt.ac.conventii.companie.Companie;

@Repository
@Transactional
public interface PartnerRepository extends JpaRepository<Partner, Integer> {
    Partner findById(int id);
//    Optional<Partner> findByEmail(String email);
    Optional<Partner> findByEmail(String email);
    List<Partner> findByCompanie(Companie companie);
    List<Partner> findByCompanieId(int companieId);
    
    @Query("SELECT p FROM Partner p LEFT JOIN FETCH p.companie")
    List<Partner> findAllWithCompanies();
    
    // Metodă pentru debugging
    @Query(value = "SELECT * FROM partners", nativeQuery = true)
    List<Partner> findAllNative();
}