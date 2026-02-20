package fr.univrouen.poste.repository;

import fr.univrouen.poste.domain.LogFile;
import fr.univrouen.poste.web.searchcriteria.LogSearchCriteria;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import java.util.ArrayList;
import java.util.List;

public class LogFileRepositoryImpl implements LogFileRepositoryCustom {

    @PersistenceContext
    EntityManager entityManager;

    @Override
    public Page<LogFile> findLogFilesByCriteria(LogSearchCriteria criteria, Pageable pageable) {
        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        CriteriaQuery<LogFile> cq = cb.createQuery(LogFile.class);
        Root<LogFile> logFile = cq.from(LogFile.class);

        List<Predicate> predicates = new ArrayList<>();

        // Filtre par action
        if (criteria.getAction() != null && !criteria.getAction().isEmpty()) {
            predicates.add(cb.like(cb.lower(logFile.get("action")),
                "%" + criteria.getAction().toLowerCase() + "%"));
        }

        // Filtre par email
        if (criteria.getEmail() != null && !criteria.getEmail().isEmpty()) {
            predicates.add(cb.like(cb.lower(logFile.get("email")),
                "%" + criteria.getEmail().toLowerCase() + "%"));
        }

        // Filtre par nom
        if (criteria.getNom() != null && !criteria.getNom().isEmpty()) {
            predicates.add(cb.like(cb.lower(logFile.get("nom")),
                "%" + criteria.getNom().toLowerCase() + "%"));
        }

        // Filtre par userId
        if (criteria.getUserId() != null && !criteria.getUserId().isEmpty()) {
            predicates.add(cb.equal(logFile.get("userId"), criteria.getUserId()));
        }

        cq.where(predicates.toArray(new Predicate[0]));

        // Appliquer le tri
        if (pageable.getSort().isSorted()) {
            pageable.getSort().forEach(order -> {
                if (order.isAscending()) {
                    cq.orderBy(cb.asc(logFile.get(order.getProperty())));
                } else {
                    cq.orderBy(cb.desc(logFile.get(order.getProperty())));
                }
            });
        }

        // Exécuter la requête avec pagination
        List<LogFile> resultList = entityManager.createQuery(cq)
                .setFirstResult((int) pageable.getOffset())
                .setMaxResults(pageable.getPageSize())
                .getResultList();

        // Compter le total
        CriteriaQuery<Long> countQuery = cb.createQuery(Long.class);
        Root<LogFile> countRoot = countQuery.from(LogFile.class);
        countQuery.select(cb.count(countRoot));

        List<Predicate> countPredicates = new ArrayList<>();
        if (criteria.getAction() != null && !criteria.getAction().isEmpty()) {
            countPredicates.add(cb.like(cb.lower(countRoot.get("action")),
                "%" + criteria.getAction().toLowerCase() + "%"));
        }
        if (criteria.getEmail() != null && !criteria.getEmail().isEmpty()) {
            countPredicates.add(cb.like(cb.lower(countRoot.get("email")),
                "%" + criteria.getEmail().toLowerCase() + "%"));
        }
        if (criteria.getNom() != null && !criteria.getNom().isEmpty()) {
            countPredicates.add(cb.like(cb.lower(countRoot.get("nom")),
                "%" + criteria.getNom().toLowerCase() + "%"));
        }
        if (criteria.getUserId() != null && !criteria.getUserId().isEmpty()) {
            countPredicates.add(cb.equal(countRoot.get("userId"), criteria.getUserId()));
        }

        countQuery.where(countPredicates.toArray(new Predicate[0]));
        Long total = entityManager.createQuery(countQuery).getSingleResult();

        return new PageImpl<>(resultList, pageable, total);
    }
}

