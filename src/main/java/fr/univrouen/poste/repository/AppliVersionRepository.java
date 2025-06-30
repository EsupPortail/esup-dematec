package fr.univrouen.poste.repository;

import fr.univrouen.poste.domain.AppliVersion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AppliVersionRepository extends JpaRepository<AppliVersion, Long> {
}
