package fr.univrouen.poste.repository;

import fr.univrouen.poste.domain.LogMail;
import org.springframework.beans.factory.annotation.Autowired;

import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;
import java.util.List;

public class LogMailRepositoryImpl implements LogMailRepositoryCustom {

    @Autowired
    EntityManager entityManager;

    static final java.util.List<String> fieldNames4OrderClauseFilter = java.util.Arrays.asList(
            "status", "mailTo", "mailsubject", "maildatetime", "id"
    );

    @Override
    public List<String> getAllMailTo() {
        TypedQuery<String> q = entityManager.createQuery("select distinct(o.mailTo) FROM LogMail o ORDER BY o.mailTo", String.class);
        return q.getResultList();
    }

    @Override
    public List<LogMail> findLogMails(String status, String mailTo, String sortFieldName, String sortOrder) {
        if ("".equals(mailTo)) {
            return findLogMailsByStatusEquals(status, sortFieldName, sortOrder);
        }
        if ("".equals(status)) {
            return findLogMailsByMailToEquals(mailTo, sortFieldName, sortOrder);
        }
        return findLogMailsByStatusEqualsAndMailToEquals(status, mailTo, sortFieldName, sortOrder);
    }

    @Override
    public long countFindLogMails(String status, String mailTo) {
        if ("".equals(mailTo)) {
            return countFindLogMailsByStatusEquals(status);
        }
        if ("".equals(status)) {
            return countFindLogMailsByMailToEquals(mailTo);
        }
        return countFindLogMailsByStatusEqualsAndMailToEquals(status, mailTo);
    }

    List<LogMail> findLogMailsByStatusEquals(String status, String sortFieldName, String sortOrder) {
        if (status == null || status.length() == 0) throw new IllegalArgumentException("The status argument is required");
        String jpaQuery = "SELECT o FROM LogMail AS o WHERE o.status = :status";
        if (fieldNames4OrderClauseFilter.contains(sortFieldName)) {
            jpaQuery = jpaQuery + " ORDER BY " + sortFieldName;
            if ("ASC".equalsIgnoreCase(sortOrder) || "DESC".equalsIgnoreCase(sortOrder)) {
                jpaQuery = jpaQuery + " " + sortOrder;
            }
        }
        TypedQuery<LogMail> q = entityManager.createQuery(jpaQuery, LogMail.class);
        q.setParameter("status", status);
        return q.getResultList();
    }

    List<LogMail> findLogMailsByMailToEquals(String mailTo, String sortFieldName, String sortOrder) {
        if (mailTo == null || mailTo.length() == 0) throw new IllegalArgumentException("The mailTo argument is required");
        String jpaQuery = "SELECT o FROM LogMail AS o WHERE o.mailTo = :mailTo";
        if (fieldNames4OrderClauseFilter.contains(sortFieldName)) {
            jpaQuery = jpaQuery + " ORDER BY " + sortFieldName;
            if ("ASC".equalsIgnoreCase(sortOrder) || "DESC".equalsIgnoreCase(sortOrder)) {
                jpaQuery = jpaQuery + " " + sortOrder;
            }
        }
        TypedQuery<LogMail> q = entityManager.createQuery(jpaQuery, LogMail.class);
        q.setParameter("mailTo", mailTo);
        return q.getResultList();
    }

    List<LogMail> findLogMailsByStatusEqualsAndMailToEquals(String status, String mailTo, String sortFieldName, String sortOrder) {
        if (status == null || status.length() == 0) throw new IllegalArgumentException("The status argument is required");
        if (mailTo == null || mailTo.length() == 0) throw new IllegalArgumentException("The mailTo argument is required");
        String jpaQuery = "SELECT o FROM LogMail AS o WHERE o.status = :status  AND o.mailTo = :mailTo";
        if (fieldNames4OrderClauseFilter.contains(sortFieldName)) {
            jpaQuery = jpaQuery + " ORDER BY " + sortFieldName;
            if ("ASC".equalsIgnoreCase(sortOrder) || "DESC".equalsIgnoreCase(sortOrder)) {
                jpaQuery = jpaQuery + " " + sortOrder;
            }
        }
        TypedQuery<LogMail> q = entityManager.createQuery(jpaQuery, LogMail.class);
        q.setParameter("status", status);
        q.setParameter("mailTo", mailTo);
        return q.getResultList();
    }

    long countFindLogMailsByStatusEquals(String status) {
        if (status == null || status.length() == 0) throw new IllegalArgumentException("The status argument is required");
        TypedQuery<Long> q = entityManager.createQuery("SELECT COUNT(o) FROM LogMail AS o WHERE o.status = :status", Long.class);
        q.setParameter("status", status);
        return q.getSingleResult();
    }

    long countFindLogMailsByMailToEquals(String mailTo) {
        if (mailTo == null || mailTo.length() == 0) throw new IllegalArgumentException("The mailTo argument is required");
        TypedQuery<Long> q = entityManager.createQuery("SELECT COUNT(o) FROM LogMail AS o WHERE o.mailTo = :mailTo", Long.class);
        q.setParameter("mailTo", mailTo);
        return q.getSingleResult();
    }

    long countFindLogMailsByStatusEqualsAndMailToEquals(String status, String mailTo) {
        if (status == null || status.length() == 0) throw new IllegalArgumentException("The status argument is required");
        if (mailTo == null || mailTo.length() == 0) throw new IllegalArgumentException("The mailTo argument is required");
        TypedQuery<Long> q = entityManager.createQuery("SELECT COUNT(o) FROM LogMail AS o WHERE o.status = :status  AND o.mailTo = :mailTo", Long.class);
        q.setParameter("status", status);
        q.setParameter("mailTo", mailTo);
        return q.getSingleResult();
    }
}
