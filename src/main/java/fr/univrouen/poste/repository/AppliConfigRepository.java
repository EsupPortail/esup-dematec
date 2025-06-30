package fr.univrouen.poste.repository;

import fr.univrouen.poste.domain.AppliConfig;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AppliConfigRepository extends JpaRepository<AppliConfig, Long> {
}
