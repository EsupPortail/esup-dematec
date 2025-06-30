package fr.univrouen.poste.repository;

import fr.univrouen.poste.domain.PosteAPourvoirFile;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PosteAPourvoirFileRepository extends JpaRepository<PosteAPourvoirFile, Long> {
}
