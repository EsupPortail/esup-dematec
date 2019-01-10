package fr.univrouen.poste.web.searchcriteria;

import java.util.List;
import java.util.Map;

import org.springframework.roo.addon.javabean.RooJavaBean;

import fr.univrouen.poste.domain.ManagerReview.ReviewStatusTypes;
import fr.univrouen.poste.domain.PosteAPourvoir;
import fr.univrouen.poste.domain.PosteCandidature.RecevableEnum;
import fr.univrouen.poste.domain.PosteCandidatureTag;
import fr.univrouen.poste.domain.PosteCandidatureTagValue;
import fr.univrouen.poste.domain.TemplateFile;
import fr.univrouen.poste.domain.User;

@RooJavaBean
public class PosteCandidatureSearchCriteria {

	RecevableEnum recevable;
	
	Boolean auditionnable;
	
	Boolean modification;
	
	List<String> numEmploiPostes;
	
	List<String> emailCandidats;
	
	List<ReviewStatusTypes> reviewStatus;
	
	String searchText;
	
	TemplateFile templateFile;
	
    Map<PosteCandidatureTag, PosteCandidatureTagValue> tags;

	public List<PosteAPourvoir> getPostes() {
		List<PosteAPourvoir> postes = null;
		if(numEmploiPostes!=null && !numEmploiPostes.isEmpty()) {
			postes = PosteAPourvoir.findPosteAPourvoirsByNumEmplois(numEmploiPostes);
		}
		return postes;
	}

	public List<User> getCandidats() {
		List<User> candidats = null;
		if(emailCandidats!=null && !emailCandidats.isEmpty()) {
			candidats = User.findUsersByEmailAddresses(emailCandidats);
		}
		return candidats;
	}

}
