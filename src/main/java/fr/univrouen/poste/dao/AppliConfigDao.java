package fr.univrouen.poste.dao;

import fr.univrouen.poste.domain.AppliConfig;
import fr.univrouen.poste.repository.AppliConfigRepository;
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
public class AppliConfigDao {

    @Resource
    AppliConfigRepository appliConfigRepository;

    public List<AppliConfig> findAllAppliConfigs() {
        return appliConfigRepository.findAll();
    }

    public List<AppliConfig> findAllAppliConfigs(String sortFieldName, String sortOrder) {
        Sort.Direction direction = "DESC".equalsIgnoreCase(sortOrder) ? Sort.Direction.DESC : Sort.Direction.ASC;
        Sort sort = Sort.by(direction, sortFieldName);
        return appliConfigRepository.findAll(sort);
    }

    public AppliConfig findAppliConfig(Long id) {
        if (id == null) return null;
        Optional<AppliConfig> result = appliConfigRepository.findById(id);
        return result.orElse(null);
    }

    public Page<AppliConfig> findAppliConfigEntries(Pageable pageable, String sortFieldName, String sortOrder) {
        if (sortFieldName != null && sortOrder != null) {
            Sort.Direction direction = "DESC".equalsIgnoreCase(sortOrder) ? Sort.Direction.DESC : Sort.Direction.ASC;
            Sort sort = Sort.by(direction, sortFieldName);
            return appliConfigRepository.findAll(PageRequest.of(pageable.getPageNumber(), pageable.getPageSize(), sort));
        }
        return appliConfigRepository.findAll(pageable);
    }

    public AppliConfig saveAppliConfig(AppliConfig appli_config) {
        return appliConfigRepository.save(appli_config);
    }

    public void flush() {
        appliConfigRepository.flush();
    }

    public AppliConfig getAppliConfig() {
        List<AppliConfig> configs = findAllAppliConfigs();
        if (configs.isEmpty()) {
            return null;
        }
        return configs.get(0);
    }
}
