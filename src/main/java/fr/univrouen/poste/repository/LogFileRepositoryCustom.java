package fr.univrouen.poste.repository;

import fr.univrouen.poste.domain.LogFile;
import fr.univrouen.poste.web.searchcriteria.LogSearchCriteria;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface LogFileRepositoryCustom {
    Page<LogFile> findLogFilesByCriteria(LogSearchCriteria criteria, Pageable pageable);
}

