package fr.univrouen.poste.dao;

import fr.univrouen.poste.domain.LogFile;
import fr.univrouen.poste.repository.LogFileRepository;
import jakarta.annotation.Resource;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.Query;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class LogFileDao {

    @Resource
    LogFileRepository log_fileRepository;

    @PersistenceContext
    EntityManager entityManager;


    public LogFile findLogFile(Long id) {
        if (id == null) return null;
        Optional<LogFile> result = log_fileRepository.findById(id);
        return result.orElse(null);
    }

    public Page<LogFile> findLogFileEntries(Pageable pageable) {
        return log_fileRepository.findAll(pageable);
    }

    public LogFile saveLogFile(LogFile log_file) {
        return log_fileRepository.save(log_file);
    }

    public void flush() {
        log_fileRepository.flush();
    }

    public List<Object[]> countUploadLogFilesBydate() {
        String sql = "SELECT date_part('year', action_date) as year, date_part('month', action_date) as month, date_part('day', action_date) as day, count(*) as count FROM log_file WHERE action='UPLOAD' GROUP BY year, month, day ORDER BY year, month, day";
        Query q = entityManager.createNativeQuery(sql);
        return q.getResultList();
    }

}
