package fr.univrouen.poste.utils;

import java.util.Date;
import java.util.List;

import org.springframework.stereotype.Service;

import fr.univrouen.poste.domain.AppliConfig;
import fr.univrouen.poste.domain.PosteCandidature;
import fr.univrouen.poste.domain.User;

@Service
public class DateClotureChecker {

	public boolean isCurrentTimeOk4ThisCandidat(User targetUser) {
		Date currentTime = new Date();     	        
		if(targetUser.getIsCandidat()) {
			// récupération candidatures candidat : auditionnable ?
			boolean auditionnable = false;
			List<PosteCandidature> candidatures = PosteCandidature.findPosteCandidaturesByCandidat(targetUser).getResultList();
			for(PosteCandidature candidature: candidatures) {
				auditionnable = auditionnable || candidature.getAuditionnable();
			}
			if(!auditionnable && !targetUser.isCandidatActif() && currentTime.compareTo(AppliConfig.getCacheDateEndCandidat()) > 0 || 
					!auditionnable && targetUser.isCandidatActif() && currentTime.compareTo(AppliConfig.getCacheDateEndCandidatActif()) > 0) {
				return false;		        }   
			else if(auditionnable) {
				Date dateEndCandidatAuditionnable = null;
				for(PosteCandidature candidature: candidatures) {
					Date datePosteAuditionnable = candidature.getPoste().getDateEndCandidatAuditionnable();
					if(candidature.getAuditionnable() && (dateEndCandidatAuditionnable == null || datePosteAuditionnable.compareTo(dateEndCandidatAuditionnable) > 0 )) {
						dateEndCandidatAuditionnable = datePosteAuditionnable;
					}
				}
				if(dateEndCandidatAuditionnable == null || currentTime.compareTo(dateEndCandidatAuditionnable) > 0) {
					return false;		        	
				}
			}
			return true;
		}
		return false;
	}
	
	
	public boolean isCurrentTimeOk4ThisMembre(User targetUser) {
		Date currentTime = new Date();     	        
		if(targetUser.getIsMembre() && currentTime.compareTo(AppliConfig.getCacheDateEndMembre()) > 0) {
			return false;
		}    
		return true;
	}

}
