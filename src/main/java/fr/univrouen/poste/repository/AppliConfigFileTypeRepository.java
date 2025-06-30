package fr.univrouen.poste.repository;

import fr.univrouen.poste.domain.AppliConfigFileType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AppliConfigFileTypeRepository extends JpaRepository<AppliConfigFileType, Long> {
}
