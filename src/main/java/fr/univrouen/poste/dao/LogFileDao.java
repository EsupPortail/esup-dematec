package fr.univrouen.poste.dao;

import fr.univrouen.poste.domain.LogFile;
import fr.univrouen.poste.repository.LogFileRepository;
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
public class LogFileDao {

    @Resource
    LogFileRepository log_fileRepository;

    @PersistenceContext
    EntityManager entityManager;

    public long countLogFiles() {
        return log_fileRepository.count();
    }

    public List<LogFile> findAllLogFiles() {
        return log_fileRepository.findAll();
    }

    public List<LogFile> findAllLogFiles(String sortFieldName, String sortOrder) {
        Sort.Direction direction = "DESC".equalsIgnoreCase(sortOrder) ? Sort.Direction.DESC : Sort.Direction.ASC;
        Sort sort = Sort.by(direction, sortFieldName);
        return log_fileRepository.findAll(sort);
    }

    public LogFile findLogFile(Long id) {
        if (id == null) return null;
        Optional<LogFile> result = log_fileRepository.findById(id);
        return result.orElse(null);
    }

    public Page<LogFile> findLogFileEntries(Pageable pageable, String sortFieldName, String sortOrder) {
        if (sortFieldName != null && sortOrder != null) {
            Sort.Direction direction = "DESC".equalsIgnoreCase(sortOrder) ? Sort.Direction.DESC : Sort.Direction.ASC;
            Sort sort = Sort.by(direction, sortFieldName);
            return log_fileRepository.findAll(PageRequest.of(pageable.getPageNumber(), pageable.getPageSize(), sort));
        }
        return log_fileRepository.findAll(pageable);
    }

    public LogFile saveLogFile(LogFile log_file) {
        return log_fileRepository.save(log_file);
    }

    public void flush() {
        log_fileRepository.flush();
    }

    public List<LogFile> findLogFiles(LogSearchCriteria logSearchCriteria, String sortFieldName, String sortOrder) {
        CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
        CriteriaQuery<LogFile> query = criteriaBuilder.createQuery(LogFile.class);
        Root<LogFile> c = query.from(LogFile.class);
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

    public List<Object[]> countUploadLogFilesBydate() {
        String sql = "SELECT date_part('year', action_date) as year, date_part('month', action_date) as month, date_part('day', action_date) as day, count(*) as count FROM log_file WHERE action='UPLOAD' GROUP BY year, month, day ORDER BY year, month, day";
        Query q = entityManager.createNativeQuery(sql);
        return q.getResultList();
    }

    Expression<Boolean> computeLogSearchCriteriaRestriction(LogSearchCriteria logSearchCriteria,
                                                                     Root<LogFile> c, CriteriaBuilder criteriaBuilder) {
        final List<Predicate> predicates = new ArrayList<>();
        if (logSearchCriteria.getUserId() != null && !logSearchCriteria.getUserId().isEmpty()) {
            predicates.add(c.get("email").in(logSearchCriteria.getUserId()));
        }
        if (logSearchCriteria.getStatus() != null && !logSearchCriteria.getStatus().isEmpty()) {
            predicates.add(c.get("action").in(logSearchCriteria.getStatus()));
        }
        if (logSearchCriteria.getNom() != null && !logSearchCriteria.getNom().isEmpty()) {
            predicates.add(c.get("nom").in(logSearchCriteria.getNom()));
        }
        return criteriaBuilder.and(predicates.toArray(new Predicate[predicates.size()]));
    }
}
