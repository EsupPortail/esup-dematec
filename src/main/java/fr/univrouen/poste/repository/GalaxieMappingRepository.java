package fr.univrouen.poste.repository;

import fr.univrouen.poste.domain.GalaxieMapping;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface GalaxieMappingRepository extends JpaRepository<GalaxieMapping, Long> {
}
