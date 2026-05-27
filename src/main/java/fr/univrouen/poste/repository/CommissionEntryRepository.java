package fr.univrouen.poste.repository;

import fr.univrouen.poste.domain.CommissionEntry;
import fr.univrouen.poste.domain.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CommissionEntryRepository extends JpaRepository<CommissionEntry, Long> {
    List<CommissionEntry> findByNumPosteAndEmail(String numPoste, String email);
    List<CommissionEntry> findByMembre(User membre);
    List<CommissionEntry> findByMembreIsNull();
    List<CommissionEntry> findByPosteIsNull();

    @Query("SELECT c FROM CommissionEntry c WHERE " +
            "LOWER(c.numPoste) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
            "LOWER(c.email) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
            "LOWER(c.nom) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
            "LOWER(c.prenom) LIKE LOWER(CONCAT('%', :search, '%'))")
    Page<CommissionEntry> findBySearch(@Param("search") String search, Pageable pageable);
}
