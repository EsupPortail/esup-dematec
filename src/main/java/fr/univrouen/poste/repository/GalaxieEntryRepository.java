package fr.univrouen.poste.repository;

import fr.univrouen.poste.domain.GalaxieEntry;
import fr.univrouen.poste.domain.PosteCandidature;
import fr.univrouen.poste.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface GalaxieEntryRepository extends JpaRepository<GalaxieEntry, Long> {
    List<GalaxieEntry> findByCandidatIsNull();
    List<GalaxieEntry> findByPosteIsNull();
    List<GalaxieEntry> findByCandidat(User candidat);
    GalaxieEntry findByCandidature(PosteCandidature candidature);
    List<GalaxieEntry> findByCandidatureIsNull();
}
