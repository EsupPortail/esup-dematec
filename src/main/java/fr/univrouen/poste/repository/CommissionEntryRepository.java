package fr.univrouen.poste.repository;

import fr.univrouen.poste.domain.CommissionEntry;
import fr.univrouen.poste.domain.User;
import fr.univrouen.poste.domain.PosteAPourvoir;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface CommissionEntryRepository extends JpaRepository<CommissionEntry, Long> {
    List<CommissionEntry> findByNumPosteAndEmail(String numPoste, String email);
    List<CommissionEntry> findByMembre(User membre);
    List<CommissionEntry> findByMembreIsNull();
    List<CommissionEntry> findByPosteIsNull();
}
