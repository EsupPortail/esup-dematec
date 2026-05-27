package fr.univrouen.poste.repository;

import fr.univrouen.poste.domain.GalaxieEntry;
import fr.univrouen.poste.domain.PosteCandidature;
import fr.univrouen.poste.domain.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface GalaxieEntryRepository extends JpaRepository<GalaxieEntry, Long> {
    List<GalaxieEntry> findByCandidatIsNull();
    List<GalaxieEntry> findByPosteIsNull();
    List<GalaxieEntry> findByCandidat(User candidat);
    GalaxieEntry findByCandidature(PosteCandidature candidature);
    List<GalaxieEntry> findByCandidatureIsNull();

    @Query("SELECT g FROM GalaxieEntry g WHERE " +
            "LOWER(g.numCandidat) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
            "LOWER(g.numEmploi) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
            "LOWER(g.nom) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
            "LOWER(g.prenom) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
            "LOWER(g.email) LIKE LOWER(CONCAT('%', :search, '%'))")
    Page<GalaxieEntry> findBySearch(@Param("search") String search, Pageable pageable);
}
