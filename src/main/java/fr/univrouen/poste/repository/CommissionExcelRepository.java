package fr.univrouen.poste.repository;

import fr.univrouen.poste.domain.CommissionExcel;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CommissionExcelRepository extends JpaRepository<CommissionExcel, Long> {
}
