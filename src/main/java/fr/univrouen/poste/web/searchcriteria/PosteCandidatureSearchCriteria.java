package fr.univrouen.poste.web.searchcriteria;

import java.util.ArrayList;
import java.util.List;

import org.springframework.roo.addon.javabean.RooJavaBean;

import fr.univrouen.poste.domain.ManagerReview.ReviewStatusTypes;
import fr.univrouen.poste.domain.PosteAPourvoir;
import fr.univrouen.poste.domain.User;

@RooJavaBean
public class PosteCandidatureSearchCriteria {

	Boolean recevable;
	
	Boolean auditionnable;
	
	Boolean modification;
	
	List<String> numEmploiPostes;
	
	List<String> emailCandidats;
	
	List<ReviewStatusTypes> reviewStatus;

	public List<PosteAPourvoir> getPostes() {
		List<PosteAPourvoir> postes = null;
		if(numEmploiPostes!=null) {
			postes = PosteAPourvoir.findPosteAPourvoirsByNumEmplois(numEmploiPostes);
		}
		return postes;
	}

	public List<User> getCandidats() {
		List<User> candidats = null;
		if(emailCandidats!=null) {
			candidats = User.findUsersByEmailAddresses(emailCandidats);
		}
		return candidats;
	}

}
