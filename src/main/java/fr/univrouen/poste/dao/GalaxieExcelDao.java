package fr.univrouen.poste.dao;

import fr.univrouen.poste.domain.GalaxieExcel;
import fr.univrouen.poste.repository.GalaxieExcelRepository;
import jakarta.annotation.Resource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class GalaxieExcelDao {

    @Resource
    GalaxieExcelRepository galaxie_excelRepository;

    public List<GalaxieExcel> findAllGalaxieExcels(String sortFieldName, String sortOrder) {
        Sort.Direction direction = "DESC".equalsIgnoreCase(sortOrder) ? Sort.Direction.DESC : Sort.Direction.ASC;
        Sort sort = Sort.by(direction, sortFieldName);
        return galaxie_excelRepository.findAll(sort);
    }

    public GalaxieExcel findGalaxieExcel(Long id) {
        if (id == null) return null;
        Optional<GalaxieExcel> result = galaxie_excelRepository.findById(id);
        return result.orElse(null);
    }

    public Page<GalaxieExcel> findGalaxieExcelEntries(Pageable pageable) {
        return galaxie_excelRepository.findAll(pageable);
    }

    public GalaxieExcel saveGalaxieExcel(GalaxieExcel galaxie_excel) {
        return galaxie_excelRepository.save(galaxie_excel);
    }

    public void deleteGalaxieExcel(Long id) {
        galaxie_excelRepository.deleteById(id);
    }

    public void flush() {
        galaxie_excelRepository.flush();
    }
}
