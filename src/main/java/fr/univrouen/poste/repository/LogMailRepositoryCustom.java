package fr.univrouen.poste.repository;

import fr.univrouen.poste.domain.LogMail;

import java.util.List;

public interface LogMailRepositoryCustom {
    List<String> getAllMailTo();
    List<LogMail> findLogMails(String status, String mailTo, String sortFieldName, String sortOrder);
    long countFindLogMails(String status, String mailTo);
}
