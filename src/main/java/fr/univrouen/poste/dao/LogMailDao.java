package fr.univrouen.poste.dao;

import fr.univrouen.poste.domain.LogMail;
import fr.univrouen.poste.repository.LogMailRepository;
import jakarta.annotation.Resource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class LogMailDao {

    @Resource
    LogMailRepository log_mailRepository;


    public List<LogMail> findAllLogMails(String sortFieldName, String sortOrder) {
        Sort.Direction direction = "DESC".equalsIgnoreCase(sortOrder) ? Sort.Direction.DESC : Sort.Direction.ASC;
        Sort sort = Sort.by(direction, sortFieldName);
        return log_mailRepository.findAll(sort);
    }

    public LogMail findLogMail(Long id) {
        if (id == null) return null;
        Optional<LogMail> result = log_mailRepository.findById(id);
        return result.orElse(null);
    }

    public Page<LogMail> findLogMailEntries(Pageable pageable) {
        return log_mailRepository.findAll(pageable);
    }

    public Page<LogMail> findLogMailEntries(Pageable pageable, String sortFieldName, String sortOrder) {
        if (sortFieldName != null && sortOrder != null) {
            Sort.Direction direction = "DESC".equalsIgnoreCase(sortOrder) ? Sort.Direction.DESC : Sort.Direction.ASC;
            Sort sort = Sort.by(direction, sortFieldName);
            return log_mailRepository.findAll(PageRequest.of(pageable.getPageNumber(), pageable.getPageSize(), sort));
        }
        return log_mailRepository.findAll(pageable);
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

    public List<LogMail> findLogMails(String status, String mailTo, String sortFieldName, String sortOrder) {
        return log_mailRepository.findLogMails(status, mailTo, sortFieldName, sortOrder);
    }

}
