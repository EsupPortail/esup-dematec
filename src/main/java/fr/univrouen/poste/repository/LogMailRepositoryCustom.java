package fr.univrouen.poste.repository;

import fr.univrouen.poste.domain.LogMail;
import fr.univrouen.poste.web.searchcriteria.LogSearchCriteria;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface LogMailRepositoryCustom {
    List<String> getAllMailTo();
    List<String> getAllDistinctStatuses();
    List<LogMail> findLogMails(String status, String mailTo, String sortFieldName, String sortOrder);
    long countFindLogMails(String status, String mailTo);
    Page<LogMail> findLogMailsByCriteria(LogSearchCriteria criteria, Pageable pageable);
}
