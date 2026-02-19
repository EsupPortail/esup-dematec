package fr.univrouen.poste.utils;

import fr.univrouen.poste.dao.AppliConfigDao;
import fr.univrouen.poste.dao.PosteCandidatureDao;
import fr.univrouen.poste.dao.UserDao;
import fr.univrouen.poste.domain.AppliConfig;
import fr.univrouen.poste.domain.PosteCandidature;
import fr.univrouen.poste.domain.User;
import jakarta.annotation.Resource;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
public class DateClotureChecker {

	@Resource
	AppliConfigDao appliConfigDao;

	@Resource
	PosteCandidatureDao posteCandidatureDao;

	@Resource
	UserDao userDao;

	public boolean isCurrentTimeOk4ThisCandidat(User targetUser) {
		LocalDateTime currentTime = LocalDateTime.now();
		AppliConfig config = appliConfigDao.getAppliConfig();
		if(targetUser.getIsCandidat()) {
			// récupération candidatures candidat : auditionnable ?
			boolean auditionnable = false;
			Page<PosteCandidature> candidatures = posteCandidatureDao.findPosteCandidaturesByCandidat(targetUser);
			for(PosteCandidature candidature: candidatures) {
				auditionnable = auditionnable || candidature.getAuditionnable();
			}
			if(!auditionnable && !userDao.isCandidatActif(targetUser) && config != null && currentTime.isAfter(config.getDateEndCandidat()) ||
					!auditionnable && userDao.isCandidatActif(targetUser) && config != null && currentTime.isAfter(config.getDateEndCandidatActif())) {
				return false;		        }
			else if(auditionnable) {
				LocalDateTime dateEndCandidatAuditionnable = null;
				for(PosteCandidature candidature: candidatures) {
					LocalDateTime datePosteAuditionnable = candidature.getPoste().getDateEndCandidatAuditionnable();
					if(candidature.getAuditionnable() && (dateEndCandidatAuditionnable == null || datePosteAuditionnable.isAfter(dateEndCandidatAuditionnable) )) {
						dateEndCandidatAuditionnable = datePosteAuditionnable;
					}
				}
                return dateEndCandidatAuditionnable != null && !currentTime.isAfter(dateEndCandidatAuditionnable);
			}
			return true;
		}
		return false;
	}
	
	
	public boolean isCurrentTimeOk4ThisMembre(User targetUser) {
		LocalDateTime currentTime = LocalDateTime.now();
		AppliConfig config = appliConfigDao.getAppliConfig();
        return targetUser.getIsMembre() && (config == null || !currentTime.isAfter(config.getDateEndMembre()));
    }

}
