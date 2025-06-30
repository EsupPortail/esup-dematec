package fr.univrouen.poste.dao;

import fr.univrouen.poste.domain.AppliConfigFileType;
import fr.univrouen.poste.repository.AppliConfigFileTypeRepository;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@Transactional
public class AppliConfigFileTypeDao {

    @Resource
    AppliConfigFileTypeRepository appliConfigFileTypeRepository;

    public AppliConfigFileType findAppliConfigFileType(Long id) {
        if (id == null) return null;
        Optional<AppliConfigFileType> result = appliConfigFileTypeRepository.findById(id);
        return result.orElse(null);
    }

    public Page<AppliConfigFileType> findAllAppliConfigFileTypes(Pageable pageable) {
        return appliConfigFileTypeRepository.findAll(pageable);
    }
    public Page<AppliConfigFileType> findAllAppliConfigFileTypes() {
        return findAllAppliConfigFileTypes(Pageable.unpaged(Sort.by(Sort.Direction.ASC, "listIndex", "id")));
    }

    public AppliConfigFileType getDefaultFileType() {
        return appliConfigFileTypeRepository.findAll().get(0);
    }

    public void delete(AppliConfigFileType appliConfigFileType) {
        appliConfigFileTypeRepository.delete(appliConfigFileType);
    }

    public void saveAppliConfigFileType(@Valid AppliConfigFileType appliConfigFileType) {
        appliConfigFileTypeRepository.save(appliConfigFileType);
    }
}
