package fr.univrouen.poste.repository;

import fr.univrouen.poste.domain.LogPosteFile;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface LogPosteFileRepository extends JpaRepository<LogPosteFile, Long>, LogPosteFileRepositoryCustom {
}
