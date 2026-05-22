package fr.univrouen.poste.dao;

import fr.univrouen.poste.domain.LogPosteFile;
import fr.univrouen.poste.repository.LogPosteFileRepository;
import fr.univrouen.poste.web.searchcriteria.LogSearchCriteria;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class LogPosteFileDao {

    @Autowired
    LogPosteFileRepository log_poste_fileRepository;

    @PersistenceContext
    EntityManager entityManager;

    public LogPosteFile findLogPosteFile(Long id) {
        if (id == null) return null;
        Optional<LogPosteFile> result = log_poste_fileRepository.findById(id);
        return result.orElse(null);
    }

    public Page<LogPosteFile> findLogPosteFileEntries(Pageable pageable) {
        return log_poste_fileRepository.findAll(pageable);
    }

    public Page<LogPosteFile> findLogPosteFilesByCriteria(LogSearchCriteria criteria, Pageable pageable) {
        boolean hasNoCriteria = (criteria.getEmail() == null || criteria.getEmail().isEmpty()) &&
                                (criteria.getAction() == null || criteria.getAction().isEmpty()) &&
                                (criteria.getNumEmploi() == null || criteria.getNumEmploi().isEmpty());
        if (hasNoCriteria) {
            return log_poste_fileRepository.findAll(pageable);
        }
        return log_poste_fileRepository.findLogPosteFilesByCriteria(criteria, pageable);
    }

    public LogPosteFile saveLogPosteFile(LogPosteFile log_poste_file) {
        return log_poste_fileRepository.save(log_poste_file);
    }

    public void flush() {
        log_poste_fileRepository.flush();
    }

    public List<String> findAllDistinctEmails() {
        String jpql = "SELECT DISTINCT l.email FROM LogPosteFile l WHERE l.email IS NOT NULL ORDER BY l.email";
        return entityManager.createQuery(jpql, String.class).getResultList();
    }

    public List<String> findAllDistinctActions() {
        String jpql = "SELECT DISTINCT l.action FROM LogPosteFile l WHERE l.action IS NOT NULL ORDER BY l.action";
        return entityManager.createQuery(jpql, String.class).getResultList();
    }

    public List<String> findAllDistinctNumEmplois() {
        String jpql = "SELECT DISTINCT l.numEmploi FROM LogPosteFile l WHERE l.numEmploi IS NOT NULL ORDER BY l.numEmploi";
        return entityManager.createQuery(jpql, String.class).getResultList();
    }
}


