package fr.univrouen.poste.repository;

import fr.univrouen.poste.domain.LogPosteFile;
import org.springframework.beans.factory.annotation.Autowired;

import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;
import java.util.List;

public class LogPosteFileRepositoryImpl implements LogPosteFileRepositoryCustom {

    @Autowired
    EntityManager entityManager;

    static final java.util.List<String> fieldNames4OrderClauseFilter = java.util.Arrays.asList(
            "action", "email", "numEmploi", "actionDate", "id"
    );

    @Override
    public List<LogPosteFile> findLogPosteFiles(String action2, String userId2, String sortFieldName, String sortOrder) {
        if ("".equals(userId2)) {
            return findLogPosteFilesByActionEquals(action2, sortFieldName, sortOrder);
        }
        if ("".equals(action2)) {
            return findLogPosteFilesByEmailEquals(userId2, sortFieldName, sortOrder);
        }
        return findLogPosteFilesByActionEqualsAndEmailEquals(action2, userId2, sortFieldName, sortOrder);
    }

    @Override
    public long countFindLogPosteFiles(String action2, String userId2) {
        if ("".equals(userId2)) {
            return countFindLogPosteFilesByActionEquals(action2);
        }
        if ("".equals(action2)) {
            return countFindLogPosteFilesByEmailEquals(userId2);
        }
        return countFindLogPosteFilesByActionEqualsAndEmailEquals(action2, userId2);
    }

    List<LogPosteFile> findLogPosteFilesByActionEquals(String action, String sortFieldName, String sortOrder) {
        if (action == null || action.length() == 0) throw new IllegalArgumentException("The action argument is required");
        StringBuilder queryBuilder = new StringBuilder("SELECT o FROM LogPosteFile AS o WHERE o.action = :action");
        if (fieldNames4OrderClauseFilter.contains(sortFieldName)) {
            queryBuilder.append(" ORDER BY ").append(sortFieldName);
            if ("ASC".equalsIgnoreCase(sortOrder) || "DESC".equalsIgnoreCase(sortOrder)) {
                queryBuilder.append(" ").append(sortOrder);
            }
        }
        TypedQuery<LogPosteFile> q = entityManager.createQuery(queryBuilder.toString(), LogPosteFile.class);
        q.setParameter("action", action);
        return q.getResultList();
    }

    List<LogPosteFile> findLogPosteFilesByEmailEquals(String email, String sortFieldName, String sortOrder) {
        if (email == null || email.length() == 0) throw new IllegalArgumentException("The email argument is required");
        StringBuilder queryBuilder = new StringBuilder("SELECT o FROM LogPosteFile AS o WHERE o.email = :email");
        if (fieldNames4OrderClauseFilter.contains(sortFieldName)) {
            queryBuilder.append(" ORDER BY ").append(sortFieldName);
            if ("ASC".equalsIgnoreCase(sortOrder) || "DESC".equalsIgnoreCase(sortOrder)) {
                queryBuilder.append(" ").append(sortOrder);
            }
        }
        TypedQuery<LogPosteFile> q = entityManager.createQuery(queryBuilder.toString(), LogPosteFile.class);
        q.setParameter("email", email);
        return q.getResultList();
    }

    List<LogPosteFile> findLogPosteFilesByActionEqualsAndEmailEquals(String action, String email, String sortFieldName, String sortOrder) {
        if (action == null || action.length() == 0) throw new IllegalArgumentException("The action argument is required");
        if (email == null || email.length() == 0) throw new IllegalArgumentException("The email argument is required");
        StringBuilder queryBuilder = new StringBuilder("SELECT o FROM LogPosteFile AS o WHERE o.action = :action  AND o.email = :email");
        if (fieldNames4OrderClauseFilter.contains(sortFieldName)) {
            queryBuilder.append(" ORDER BY ").append(sortFieldName);
            if ("ASC".equalsIgnoreCase(sortOrder) || "DESC".equalsIgnoreCase(sortOrder)) {
                queryBuilder.append(" ").append(sortOrder);
            }
        }
        TypedQuery<LogPosteFile> q = entityManager.createQuery(queryBuilder.toString(), LogPosteFile.class);
        q.setParameter("action", action);
        q.setParameter("email", email);
        return q.getResultList();
    }

    long countFindLogPosteFilesByActionEquals(String action) {
        if (action == null || action.length() == 0) throw new IllegalArgumentException("The action argument is required");
        TypedQuery<Long> q = entityManager.createQuery("SELECT COUNT(o) FROM LogPosteFile AS o WHERE o.action = :action", Long.class);
        q.setParameter("action", action);
        return q.getSingleResult();
    }

    long countFindLogPosteFilesByEmailEquals(String email) {
        if (email == null || email.length() == 0) throw new IllegalArgumentException("The email argument is required");
        TypedQuery<Long> q = entityManager.createQuery("SELECT COUNT(o) FROM LogPosteFile AS o WHERE o.email = :email", Long.class);
        q.setParameter("email", email);
        return q.getSingleResult();
    }

    long countFindLogPosteFilesByActionEqualsAndEmailEquals(String action, String email) {
        if (action == null || action.length() == 0) throw new IllegalArgumentException("The action argument is required");
        if (email == null || email.length() == 0) throw new IllegalArgumentException("The email argument is required");
        TypedQuery<Long> q = entityManager.createQuery("SELECT COUNT(o) FROM LogPosteFile AS o WHERE o.action = :action  AND o.email = :email", Long.class);
        q.setParameter("action", action);
        q.setParameter("email", email);
        return q.getSingleResult();
    }
}
