package fr.univrouen.poste.repository;

import fr.univrouen.poste.domain.LogPosteFile;
import fr.univrouen.poste.web.searchcriteria.LogSearchCriteria;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface LogPosteFileRepositoryCustom {
    List<LogPosteFile> findLogPosteFiles(String action2, String userId2, String sortFieldName, String sortOrder);
    long countFindLogPosteFiles(String action2, String userId2);
    Page<LogPosteFile> findLogPosteFilesByCriteria(LogSearchCriteria criteria, Pageable pageable);
}


