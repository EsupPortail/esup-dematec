package fr.univrouen.poste.repository;

import fr.univrouen.poste.domain.LogPosteFile;
import fr.univrouen.poste.web.searchcriteria.LogSearchCriteria;
import org.springframework.beans.factory.annotation.Autowired;

import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import java.util.ArrayList;
import java.util.List;

public class LogPosteFileRepositoryImpl implements LogPosteFileRepositoryCustom {

    @Autowired
    EntityManager entityManager;

    static final java.util.List<String> fieldNames4OrderClauseFilter = java.util.Arrays.asList(
            "action", "email", "numEmploi", "actionDate", "id"
    );

    @Override
    public Page<LogPosteFile> findLogPosteFilesByCriteria(LogSearchCriteria criteria, Pageable pageable) {
        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        CriteriaQuery<LogPosteFile> cq = cb.createQuery(LogPosteFile.class);
        Root<LogPosteFile> logPosteFile = cq.from(LogPosteFile.class);

        List<Predicate> predicates = buildPredicates(cb, logPosteFile, criteria);
        cq.where(predicates.toArray(new Predicate[0]));

        // Tri
        if (pageable.getSort().isSorted()) {
            pageable.getSort().forEach(order -> {
                if (order.isAscending()) {
                    cq.orderBy(cb.asc(logPosteFile.get(order.getProperty())));
                } else {
                    cq.orderBy(cb.desc(logPosteFile.get(order.getProperty())));
                }
            });
        }

        List<LogPosteFile> resultList = entityManager.createQuery(cq)
                .setFirstResult((int) pageable.getOffset())
                .setMaxResults(pageable.getPageSize())
                .getResultList();

        // Count
        CriteriaQuery<Long> countQuery = cb.createQuery(Long.class);
        Root<LogPosteFile> countRoot = countQuery.from(LogPosteFile.class);
        countQuery.select(cb.count(countRoot));
        countQuery.where(buildPredicates(cb, countRoot, criteria).toArray(new Predicate[0]));
        Long total = entityManager.createQuery(countQuery).getSingleResult();

        return new PageImpl<>(resultList, pageable, total);
    }

    private List<Predicate> buildPredicates(CriteriaBuilder cb, Root<LogPosteFile> root, LogSearchCriteria criteria) {
        List<Predicate> predicates = new ArrayList<>();
        if (criteria.getEmail() != null && !criteria.getEmail().isEmpty()) {
            predicates.add(cb.like(cb.lower(root.get("email")),
                    "%" + criteria.getEmail().toLowerCase() + "%"));
        }
        if (criteria.getAction() != null && !criteria.getAction().isEmpty()) {
            predicates.add(cb.equal(root.get("action"), criteria.getAction()));
        }
        if (criteria.getNumEmploi() != null && !criteria.getNumEmploi().isEmpty()) {
            predicates.add(cb.like(cb.lower(root.get("numEmploi")),
                    "%" + criteria.getNumEmploi().toLowerCase() + "%"));
        }
        return predicates;
    }

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

