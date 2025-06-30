package fr.univrouen.poste.repository;

import fr.univrouen.poste.domain.GalaxieExcel;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface GalaxieExcelRepository extends JpaRepository<GalaxieExcel, Long> {
}
