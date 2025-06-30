package fr.univrouen.poste.repository;

import fr.univrouen.poste.domain.LogAuth;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface LogAuthRepository extends JpaRepository<LogAuth, Long> {
}
