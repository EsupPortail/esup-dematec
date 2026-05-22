package fr.univrouen.poste.repository;

import fr.univrouen.poste.domain.LogAuth;
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

public class LogAuthRepositoryImpl implements LogAuthRepositoryCustom {

    @PersistenceContext
    EntityManager entityManager;

    @Override
    public Page<LogAuth> findLogAuthsByCriteria(LogSearchCriteria criteria, Pageable pageable) {
        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        CriteriaQuery<LogAuth> cq = cb.createQuery(LogAuth.class);
        Root<LogAuth> logAuth = cq.from(LogAuth.class);

        List<Predicate> predicates = buildPredicates(cb, logAuth, criteria);
        cq.where(predicates.toArray(new Predicate[0]));

        // Tri
        if (pageable.getSort().isSorted()) {
            pageable.getSort().forEach(order -> {
                if (order.isAscending()) {
                    cq.orderBy(cb.asc(logAuth.get(order.getProperty())));
                } else {
                    cq.orderBy(cb.desc(logAuth.get(order.getProperty())));
                }
            });
        }

        List<LogAuth> resultList = entityManager.createQuery(cq)
                .setFirstResult((int) pageable.getOffset())
                .setMaxResults(pageable.getPageSize())
                .getResultList();

        // Count
        CriteriaQuery<Long> countQuery = cb.createQuery(Long.class);
        Root<LogAuth> countRoot = countQuery.from(LogAuth.class);
        countQuery.select(cb.count(countRoot));
        countQuery.where(buildPredicates(cb, countRoot, criteria).toArray(new Predicate[0]));
        Long total = entityManager.createQuery(countQuery).getSingleResult();

        return new PageImpl<>(resultList, pageable, total);
    }

    private List<Predicate> buildPredicates(CriteriaBuilder cb, Root<LogAuth> root, LogSearchCriteria criteria) {
        List<Predicate> predicates = new ArrayList<>();
        if (criteria.getUserId() != null && !criteria.getUserId().isEmpty()) {
            predicates.add(cb.equal(root.get("userId"), criteria.getUserId()));
        }
        if (criteria.getAction() != null && !criteria.getAction().isEmpty()) {
            predicates.add(cb.equal(root.get("action"), criteria.getAction()));
        }
        return predicates;
    }
}

