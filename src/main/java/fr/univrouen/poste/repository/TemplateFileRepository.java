package fr.univrouen.poste.repository;

import fr.univrouen.poste.domain.TemplateFile;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TemplateFileRepository extends JpaRepository<TemplateFile, Long> {
    List<TemplateFile> findByTemplateFileType(TemplateFile.TemplateFileType templateFileType);
    List<TemplateFile> findByTemplateFileType(TemplateFile.TemplateFileType templateFileType, Sort sort);
}
