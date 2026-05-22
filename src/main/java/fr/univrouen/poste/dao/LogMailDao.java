package fr.univrouen.poste.dao;

import fr.univrouen.poste.domain.LogMail;
import fr.univrouen.poste.repository.LogMailRepository;
import fr.univrouen.poste.web.searchcriteria.LogSearchCriteria;
import jakarta.annotation.Resource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class LogMailDao {

    @Resource
    LogMailRepository log_mailRepository;

    public LogMail findLogMail(Long id) {
        if (id == null) return null;
        Optional<LogMail> result = log_mailRepository.findById(id);
        return result.orElse(null);
    }

    public Page<LogMail> findLogMailEntries(Pageable pageable) {
        return log_mailRepository.findAll(pageable);
    }

    public Page<LogMail> findLogMailsByCriteria(LogSearchCriteria criteria, Pageable pageable) {
        boolean hasNoCriteria = (criteria.getUserId() == null || criteria.getUserId().isEmpty()) &&
                                (criteria.getStatus() == null || criteria.getStatus().isEmpty()) &&
                                (criteria.getMessage() == null || criteria.getMessage().isEmpty());
        if (hasNoCriteria) {
            return log_mailRepository.findAll(pageable);
        }
        return log_mailRepository.findLogMailsByCriteria(criteria, pageable);
    }

    public LogMail saveLogMail(LogMail log_mail) {
        return log_mailRepository.save(log_mail);
    }

    public void flush() {
        log_mailRepository.flush();
    }

    public List<String> getAllMailTo() {
        return log_mailRepository.getAllMailTo();
    }

    public List<String> getAllDistinctStatuses() {
        return log_mailRepository.getAllDistinctStatuses();
    }

}


