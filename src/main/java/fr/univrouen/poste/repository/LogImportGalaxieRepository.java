package fr.univrouen.poste.repository;

import fr.univrouen.poste.domain.LogImportGalaxie;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface LogImportGalaxieRepository extends JpaRepository<LogImportGalaxie, Long> {
    Page<LogImportGalaxie> findByStatusEquals(String status, Pageable pageable);
}
