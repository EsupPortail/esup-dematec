package fr.univrouen.poste.repository;

import fr.univrouen.poste.domain.PosteCandidatureTagValue;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PosteCandidatureTagValueRepository extends JpaRepository<PosteCandidatureTagValue, Long> {
}
