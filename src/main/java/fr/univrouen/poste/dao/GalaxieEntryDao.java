package fr.univrouen.poste.dao;

import fr.univrouen.poste.domain.GalaxieEntry;
import fr.univrouen.poste.domain.PosteCandidature;
import fr.univrouen.poste.domain.User;
import fr.univrouen.poste.repository.GalaxieEntryRepository;
import jakarta.annotation.Resource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class GalaxieEntryDao {

    @Resource
    GalaxieEntryRepository galaxieEntryRepository;

    public List<GalaxieEntry> findAllGalaxieEntrys() {
        return galaxieEntryRepository.findAll();
    }

    public List<GalaxieEntry> findAllGalaxieEntrys(String sortFieldName, String sortOrder) {
        Sort.Direction direction = "DESC".equalsIgnoreCase(sortOrder) ? Sort.Direction.DESC : Sort.Direction.ASC;
        Sort sort = Sort.by(direction, sortFieldName);
        return galaxieEntryRepository.findAll(sort);
    }

    public GalaxieEntry findGalaxieEntry(Long id) {
        if (id == null) return null;
        Optional<GalaxieEntry> result = galaxieEntryRepository.findById(id);
        return result.orElse(null);
    }

    public List<GalaxieEntry> findAllGalaxieEntrysWithCandidatNull() {
        return galaxieEntryRepository.findByCandidatIsNull();
    }

    public List<GalaxieEntry> findAllGalaxieEntrysWithPosteNull() {
        return galaxieEntryRepository.findByPosteIsNull();
    }

    public List<GalaxieEntry> findGalaxieEntrysByCandidat(User candidat) {
        return galaxieEntryRepository.findByCandidat(candidat);
    }

    public Page<GalaxieEntry> findGalaxieEntryEntries(Pageable pageable, String sortFieldName, String sortOrder) {
        if (sortFieldName != null && sortOrder != null) {
            Sort.Direction direction = "DESC".equalsIgnoreCase(sortOrder) ? Sort.Direction.DESC : Sort.Direction.ASC;
            Sort sort = Sort.by(direction, sortFieldName);
            return galaxieEntryRepository.findAll(PageRequest.of(pageable.getPageNumber(), pageable.getPageSize(), sort));
        }
        return galaxieEntryRepository.findAll(pageable);
    }

    public GalaxieEntry saveGalaxieEntry(GalaxieEntry galaxie_entry) {
        return galaxieEntryRepository.save(galaxie_entry);
    }

    public void deleteGalaxieEntry(Long id) {
        galaxieEntryRepository.deleteById(id);
    }

    public void flush() {
        galaxieEntryRepository.flush();
    }

    public GalaxieEntry findGalaxieEntrysByCandidature(PosteCandidature candidature) {
        return galaxieEntryRepository.findByCandidature(candidature);
    }

    public List<GalaxieEntry> findGalaxieEntrysByPosteIsNull() {
        return galaxieEntryRepository.findByPosteIsNull();
    }

    public List<GalaxieEntry> findGalaxieEntrysByCandidatIsNull() {
        return galaxieEntryRepository.findByCandidatIsNull();
    }

    public List<GalaxieEntry> findGalaxieEntrysByCandidatureIsNull() {
        return galaxieEntryRepository.findByCandidatureIsNull();
    }
}
