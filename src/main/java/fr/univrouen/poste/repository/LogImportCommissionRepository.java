package fr.univrouen.poste.repository;

import fr.univrouen.poste.domain.LogImportCommission;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface LogImportCommissionRepository extends JpaRepository<LogImportCommission, Long> {
    Page<LogImportCommission> findByStatusEquals(String status, Pageable pageable);
    long countByStatusEquals(String status);
}
