package fr.univrouen.poste.dao;

import fr.univrouen.poste.domain.AppliVersion;
import fr.univrouen.poste.repository.AppliVersionRepository;
import jakarta.annotation.Resource;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
public class AppliVersionDao {

    @Resource
    AppliVersionRepository appli_versionRepository;

    public List<AppliVersion> findAllAppliVersions() {
        return appli_versionRepository.findAll();
    }

    public List<AppliVersion> findAllAppliVersions(String sortFieldName, String sortOrder) {
        Sort.Direction direction = "DESC".equalsIgnoreCase(sortOrder) ? Sort.Direction.DESC : Sort.Direction.ASC;
        Sort sort = Sort.by(direction, sortFieldName);
        return appli_versionRepository.findAll(sort);
    }

    public AppliVersion saveAppliVersion(AppliVersion appli_version) {
        return appli_versionRepository.save(appli_version);
    }

    public void flush() {
        appli_versionRepository.flush();
    }
}
