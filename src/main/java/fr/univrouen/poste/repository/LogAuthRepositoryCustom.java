package fr.univrouen.poste.repository;

import fr.univrouen.poste.domain.LogAuth;
import fr.univrouen.poste.web.searchcriteria.LogSearchCriteria;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface LogAuthRepositoryCustom {
    Page<LogAuth> findLogAuthsByCriteria(LogSearchCriteria criteria, Pageable pageable);
}

