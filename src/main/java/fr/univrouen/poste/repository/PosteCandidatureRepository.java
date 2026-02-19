package fr.univrouen.poste.repository;

import fr.univrouen.poste.domain.PosteAPourvoir;
import fr.univrouen.poste.domain.PosteCandidature;
import fr.univrouen.poste.domain.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

@Repository
public interface PosteCandidatureRepository extends JpaRepository<PosteCandidature, Long>, PosteCandidatureRepositoryCustom {
    Page<PosteCandidature> findByCandidat(User candidat, Pageable pageable);
    Page<PosteCandidature> findByCandidatAndPoste(User candidat, PosteAPourvoir poste, Pageable pageable);
    Page<PosteCandidature> findByPosteIn(List<PosteAPourvoir> postes, Pageable pageable);

    @Query("""
        SELECT pc FROM PosteCandidature pc WHERE 
                pc.poste IN :postes 
                AND pc.recevableEnum = 'RECEVABLE'
                AND (:auditionnable is NULL OR pc.auditionnable = :auditionnable)
        """)
    Page<PosteCandidature> findPosteCandidaturesRecevableByPostesAndByAuditionnable(@Param("postes") Set<PosteAPourvoir> postes, @Param("auditionnable") Boolean auditionnable, Pageable pageable);

    @Query("""
        SELECT pc FROM PosteCandidature pc WHERE 
                pc.candidat = :candidat 
                AND (
                    (pc.poste.dateEndSignupCandidat > :date AND pc.auditionnable = FALSE) 
                    OR 
                    (pc.poste.dateEndCandidatAuditionnable > :date AND pc.auditionnable = TRUE)
                )
        """)
    Page<PosteCandidature> findPosteCandidaturesByCandidatAndByDateEndCandidatGreaterThanAndNoAuditionnableOrByDateEndCandidatAuditionnableGreaterThanAndAuditionnable(@Param("candidat") User candidat, @Param("date") LocalDateTime date, Pageable pageable);

}
