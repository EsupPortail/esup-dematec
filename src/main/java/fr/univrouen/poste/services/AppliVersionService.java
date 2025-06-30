package fr.univrouen.poste.services;

import fr.univrouen.poste.dao.AppliVersionDao;
import fr.univrouen.poste.domain.AppliVersion;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class AppliVersionService {

    @Autowired
    AppliVersionDao appliVersionDao;

    String cacheVersion;

    public String getCacheVersion() {
        if (cacheVersion == null) {
            List<AppliVersion> appliVersions = appliVersionDao.findAllAppliVersions();
            if (appliVersions.isEmpty()) {
                cacheVersion = "?!";
            } else {
                cacheVersion = appliVersions.get(0).getEsupDematEcVersion();
            }
        }
        return cacheVersion;
    }

    public void clearCache() {
        cacheVersion = null;
    }
}

