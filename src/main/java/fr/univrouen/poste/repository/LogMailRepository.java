package fr.univrouen.poste.repository;

import fr.univrouen.poste.domain.LogMail;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface LogMailRepository extends JpaRepository<LogMail, Long>, LogMailRepositoryCustom {
}
