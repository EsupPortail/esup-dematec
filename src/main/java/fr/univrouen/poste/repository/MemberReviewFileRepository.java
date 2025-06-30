package fr.univrouen.poste.repository;

import fr.univrouen.poste.domain.MemberReviewFile;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface MemberReviewFileRepository extends JpaRepository<MemberReviewFile, Long> {
}
