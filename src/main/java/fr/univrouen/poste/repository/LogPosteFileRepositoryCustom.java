package fr.univrouen.poste.repository;

import fr.univrouen.poste.domain.LogPosteFile;

import java.util.List;

public interface LogPosteFileRepositoryCustom {
    List<LogPosteFile> findLogPosteFiles(String action2, String userId2, String sortFieldName, String sortOrder);
    long countFindLogPosteFiles(String action2, String userId2);
}
