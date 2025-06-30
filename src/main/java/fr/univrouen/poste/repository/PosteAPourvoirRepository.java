package fr.univrouen.poste.repository;

import fr.univrouen.poste.domain.PosteAPourvoir;
import fr.univrouen.poste.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Date;
import java.util.List;
import java.util.Optional;

@Repository
public interface PosteAPourvoirRepository extends JpaRepository<PosteAPourvoir, Long> {
    Optional<PosteAPourvoir> findByNumEmploi(String numEmploi);
    List<PosteAPourvoir> findByNumEmploiIn(List<String> numEmplois);
    List<PosteAPourvoir> findByMembresContains(User membre);
    long countByDateEndSignupCandidatGreaterThan(Date dateEndSignupCandidat);
}
