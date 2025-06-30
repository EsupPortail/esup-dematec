package fr.univrouen.poste.repository;

import fr.univrouen.poste.domain.ManagerReview;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ManagerReviewRepository extends JpaRepository<ManagerReview, Long> {
}
