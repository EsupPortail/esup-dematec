package fr.univrouen.poste.repository;

import fr.univrouen.poste.domain.LogFile;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface LogFileRepository extends JpaRepository<LogFile, Long> {
}
