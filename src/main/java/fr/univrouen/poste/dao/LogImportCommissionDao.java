package fr.univrouen.poste.dao;

import fr.univrouen.poste.domain.LogImportCommission;
import fr.univrouen.poste.repository.LogImportCommissionRepository;
import fr.univrouen.poste.web.searchcriteria.LogSearchCriteria;
import jakarta.annotation.Resource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@Transactional
public class LogImportCommissionDao {

    @Resource
    LogImportCommissionRepository log_import_commissionRepository;

    public LogImportCommission findLogImportCommission(Long id) {
        if (id == null) return null;
        Optional<LogImportCommission> result = log_import_commissionRepository.findById(id);
        return result.orElse(null);
    }

    public LogImportCommission saveLogImportCommission(LogImportCommission log_import_commission) {
        return log_import_commissionRepository.save(log_import_commission);
    }

    public Page<LogImportCommission> findLogImportCommissions(LogSearchCriteria logSearchCriteria, Pageable pageable) {
        if(logSearchCriteria.getStatus() == null || logSearchCriteria.getStatus().isEmpty()) {
            return log_import_commissionRepository.findAll(pageable);
        }
        return log_import_commissionRepository.findByStatusEquals(logSearchCriteria.getStatus(), pageable);
    }
}
