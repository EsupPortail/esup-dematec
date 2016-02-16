package fr.univrouen.poste.domain;

import java.util.List;

import org.springframework.roo.addon.javabean.RooJavaBean;
import org.springframework.roo.addon.jpa.activerecord.RooJpaActiveRecord;
import org.springframework.roo.addon.tostring.RooToString;

@RooJavaBean
@RooToString
@RooJpaActiveRecord
public class AppliVersion {

	private static String cacheVersion;
	
	String esupDematEcVersion;
	

	public static String getCacheVersion() {
		if(cacheVersion == null) {
			AppliVersion appliVersion = null;
			List<AppliVersion> appliVersions = AppliVersion.findAllAppliVersions();
			if(appliVersions.isEmpty()) {
				cacheVersion = "?!";
			} else {
				cacheVersion = appliVersions.get(0).getEsupDematEcVersion();
			}
		}
		return cacheVersion;
	}

}
