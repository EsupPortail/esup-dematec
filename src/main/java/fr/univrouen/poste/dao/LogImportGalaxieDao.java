package fr.univrouen.poste.dao;

import fr.univrouen.poste.domain.LogImportGalaxie;
import fr.univrouen.poste.repository.LogImportGalaxieRepository;
import fr.univrouen.poste.web.searchcriteria.LogSearchCriteria;
import jakarta.annotation.Resource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@Transactional
public class LogImportGalaxieDao {

    @Resource
    LogImportGalaxieRepository log_import_galaxieRepository;

    public LogImportGalaxie findLogImportGalaxie(Long id) {
        if (id == null) return null;
        Optional<LogImportGalaxie> result = log_import_galaxieRepository.findById(id);
        return result.orElse(null);
    }

    public LogImportGalaxie saveLogImportGalaxie(LogImportGalaxie log_import_galaxie) {
        return log_import_galaxieRepository.save(log_import_galaxie);
    }

    public Page<LogImportGalaxie> findLogImportGalaxies(LogSearchCriteria logSearchCriteria, Pageable pageable) {
        if(logSearchCriteria.getStatus() == null || logSearchCriteria.getStatus().isEmpty()) {
            return log_import_galaxieRepository.findAll(pageable);
        }
        return log_import_galaxieRepository.findByStatusEquals(logSearchCriteria.getStatus(), pageable);
    }

}
