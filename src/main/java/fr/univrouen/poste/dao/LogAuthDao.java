package fr.univrouen.poste.dao;

import fr.univrouen.poste.domain.LogAuth;
import fr.univrouen.poste.repository.LogAuthRepository;
import fr.univrouen.poste.web.searchcriteria.LogSearchCriteria;
import jakarta.annotation.Resource;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.Query;
import jakarta.persistence.criteria.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class LogAuthDao {

    @Resource
    LogAuthRepository log_authRepository;

    @PersistenceContext
    EntityManager entityManager;

    public List<LogAuth> findAllLogAuths(String sortFieldName, String sortOrder) {
        Sort.Direction direction = "DESC".equalsIgnoreCase(sortOrder) ? Sort.Direction.DESC : Sort.Direction.ASC;
        Sort sort = Sort.by(direction, sortFieldName);
        return log_authRepository.findAll(sort);
    }

    public LogAuth findLogAuth(Long id) {
        if (id == null) return null;
        Optional<LogAuth> result = log_authRepository.findById(id);
        return result.orElse(null);
    }

    public Page<LogAuth> findLogAuthEntries(Pageable pageable) {
        return log_authRepository.findAll(pageable);
    }

    public Page<LogAuth> findLogAuthEntries(Pageable pageable, String sortFieldName, String sortOrder) {
        if (sortFieldName != null && sortOrder != null) {
            Sort.Direction direction = "DESC".equalsIgnoreCase(sortOrder) ? Sort.Direction.DESC : Sort.Direction.ASC;
            Sort sort = Sort.by(direction, sortFieldName);
            return log_authRepository.findAll(PageRequest.of(pageable.getPageNumber(), pageable.getPageSize(), sort));
        }
        return log_authRepository.findAll(pageable);
    }

    public LogAuth saveLogAuth(LogAuth log_auth) {
        return log_authRepository.save(log_auth);
    }

    public void deleteLogAuth(LogAuth log_auth) {
        log_authRepository.delete(log_auth);
    }

    public void flush() {
        log_authRepository.flush();
    }

    public List<LogAuth> findLogAuths(LogSearchCriteria logSearchCriteria, String sortFieldName, String sortOrder) {
        CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
        CriteriaQuery<LogAuth> query = criteriaBuilder.createQuery(LogAuth.class);
        Root<LogAuth> c = query.from(LogAuth.class);
        Expression<Boolean> logSearchCriteriaRestriction = computeLogSearchCriteriaRestriction(logSearchCriteria, c, criteriaBuilder);

        final List<jakarta.persistence.criteria.Order> orders = new ArrayList<>();

        if (sortFieldName != null && !sortFieldName.isEmpty()) {
            String[] sortFieldNameSplit = sortFieldName.split("\\.");
            if ("DESC".equalsIgnoreCase(sortOrder)) {
                if (sortFieldNameSplit.length < 2) {
                    orders.add(criteriaBuilder.desc(c.get(sortFieldName)));
                } else {
                    orders.add(criteriaBuilder.desc(c.join(sortFieldNameSplit[0]).get(sortFieldNameSplit[1])));
                }
            } else {
                if (sortFieldNameSplit.length < 2) {
                    orders.add(criteriaBuilder.asc(c.get(sortFieldName)));
                } else {
                    orders.add(criteriaBuilder.asc(c.join(sortFieldNameSplit[0]).get(sortFieldNameSplit[1])));
                }
            }
        }

        query.where(logSearchCriteriaRestriction);
        query.orderBy(orders);
        query.select(c);
        return entityManager.createQuery(query).getResultList();
    }

    public List<Object[]> countSuccessLogAuthsByDate() {
        String sql = "SELECT date_part('year', action_date) as year, date_part('month', action_date) as month, date_part('day', action_date) as day, count(*) as count FROM log_auth WHERE action='AUTH SUCCESS' GROUP BY year, month, day ORDER BY year, month, day";
        Query q = entityManager.createNativeQuery(sql);
        return q.getResultList();
    }

    Expression<Boolean> computeLogSearchCriteriaRestriction(LogSearchCriteria logSearchCriteria,
                                                                     Root<LogAuth> c, CriteriaBuilder criteriaBuilder) {
        final List<Predicate> predicates = new ArrayList<>();
        if (logSearchCriteria.getUserId() != null && !logSearchCriteria.getUserId().isEmpty()) {
            predicates.add(c.get("userId").in(logSearchCriteria.getUserId()));
        }
        if (logSearchCriteria.getStatus() != null && !logSearchCriteria.getStatus().isEmpty()) {
            predicates.add(c.get("action").in(logSearchCriteria.getStatus()));
        }
        return criteriaBuilder.and(predicates.toArray(new Predicate[predicates.size()]));
    }
}
