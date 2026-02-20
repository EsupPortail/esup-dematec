package fr.univrouen.poste.dao;

import fr.univrouen.poste.domain.LogAuth;
import fr.univrouen.poste.repository.LogAuthRepository;
import jakarta.annotation.Resource;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.Query;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class LogAuthDao {

    @Resource
    LogAuthRepository log_authRepository;

    @PersistenceContext
    EntityManager entityManager;

    public LogAuth findLogAuth(Long id) {
        if (id == null) return null;
        Optional<LogAuth> result = log_authRepository.findById(id);
        return result.orElse(null);
    }

    public Page<LogAuth> findLogAuthEntries(Pageable pageable) {
        return log_authRepository.findAll(pageable);
    }

    public LogAuth saveLogAuth(LogAuth log_auth) {
        return log_authRepository.save(log_auth);
    }

    public void flush() {
        log_authRepository.flush();
    }

    public List<Object[]> countSuccessLogAuthsByDate() {
        String sql = "SELECT date_part('year', action_date) as year, date_part('month', action_date) as month, date_part('day', action_date) as day, count(*) as count FROM log_auth WHERE action='AUTH SUCCESS' GROUP BY year, month, day ORDER BY year, month, day";
        Query q = entityManager.createNativeQuery(sql);
        return q.getResultList();
    }

}
