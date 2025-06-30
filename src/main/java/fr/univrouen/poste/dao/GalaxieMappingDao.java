package fr.univrouen.poste.dao;

import fr.univrouen.poste.domain.GalaxieMapping;
import fr.univrouen.poste.repository.GalaxieMappingRepository;
import jakarta.annotation.Resource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class GalaxieMappingDao {

    @Resource
    GalaxieMappingRepository galaxieMappingRepository;

    public List<GalaxieMapping> findAllGalaxieMappings() {
        return galaxieMappingRepository.findAll();
    }

    public GalaxieMapping findGalaxieMapping(Long id) {
        if (id == null) return null;
        Optional<GalaxieMapping> result = galaxieMappingRepository.findById(id);
        return result.orElse(null);
    }

    public Page<GalaxieMapping> findGalaxieMappingEntries(Pageable pageable) {
        return galaxieMappingRepository.findAll(pageable);
    }

    public GalaxieMapping saveGalaxieMapping(GalaxieMapping galaxie_mapping) {
        return galaxieMappingRepository.save(galaxie_mapping);
    }

    public void flush() {
        galaxieMappingRepository.flush();
    }
}
