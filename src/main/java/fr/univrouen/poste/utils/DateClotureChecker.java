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

import java.util.Date;

@Service
public class DateClotureChecker {

	@Resource
	AppliConfigDao appliConfigDao;

	@Resource
	PosteCandidatureDao posteCandidatureDao;

	@Resource
	UserDao userDao;

	public boolean isCurrentTimeOk4ThisCandidat(User targetUser) {
		Date currentTime = new Date();
		AppliConfig config = appliConfigDao.getAppliConfig();
		if(targetUser.getIsCandidat()) {
			// récupération candidatures candidat : auditionnable ?
			boolean auditionnable = false;
			Page<PosteCandidature> candidatures = posteCandidatureDao.findPosteCandidaturesByCandidat(targetUser);
			for(PosteCandidature candidature: candidatures) {
				auditionnable = auditionnable || candidature.getAuditionnable();
			}
			if(!auditionnable && !userDao.isCandidatActif(targetUser) && config != null && currentTime.compareTo(config.getDateEndCandidat()) > 0 ||
					!auditionnable && userDao.isCandidatActif(targetUser) && config != null && currentTime.compareTo(config.getDateEndCandidatActif()) > 0) {
				return false;		        }   
			else if(auditionnable) {
				Date dateEndCandidatAuditionnable = null;
				for(PosteCandidature candidature: candidatures) {
					Date datePosteAuditionnable = candidature.getPoste().getDateEndCandidatAuditionnable();
					if(candidature.getAuditionnable() && (dateEndCandidatAuditionnable == null || datePosteAuditionnable.compareTo(dateEndCandidatAuditionnable) > 0 )) {
						dateEndCandidatAuditionnable = datePosteAuditionnable;
					}
				}
                return dateEndCandidatAuditionnable != null && currentTime.compareTo(dateEndCandidatAuditionnable) <= 0;
			}
			return true;
		}
		return false;
	}
	
	
	public boolean isCurrentTimeOk4ThisMembre(User targetUser) {
		Date currentTime = new Date();
		AppliConfig config = appliConfigDao.getAppliConfig();
        return targetUser.getIsMembre() && (config == null || currentTime.compareTo(config.getDateEndMembre()) <= 0);
    }

}
