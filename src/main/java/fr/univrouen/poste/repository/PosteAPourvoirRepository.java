package fr.univrouen.poste.repository;

import fr.univrouen.poste.domain.PosteAPourvoir;
import fr.univrouen.poste.domain.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Date;
import java.util.List;
import java.util.Optional;

@Repository
public interface PosteAPourvoirRepository extends JpaRepository<PosteAPourvoir, Long> {
    Optional<PosteAPourvoir> findByNumEmploi(String numEmploi);
    List<PosteAPourvoir> findByNumEmploiIn(List<String> numEmplois);
    Page<PosteAPourvoir> findByMembresContains(User membre, Pageable pageable);
    long countByDateEndSignupCandidatGreaterThan(Date dateEndSignupCandidat);
}
