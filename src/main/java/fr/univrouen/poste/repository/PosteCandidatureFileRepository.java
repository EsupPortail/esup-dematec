package fr.univrouen.poste.repository;

import fr.univrouen.poste.domain.AppliConfigFileType;
import fr.univrouen.poste.domain.PosteCandidatureFile;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PosteCandidatureFileRepository extends JpaRepository<PosteCandidatureFile, Long> {
    long countByFileType(AppliConfigFileType fileType);
}
