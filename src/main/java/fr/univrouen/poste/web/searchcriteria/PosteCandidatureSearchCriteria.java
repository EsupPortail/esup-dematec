package fr.univrouen.poste.web.searchcriteria;

import fr.univrouen.poste.domain.ManagerReview.ReviewStatusTypes;
import fr.univrouen.poste.domain.PosteCandidature.RecevableEnum;
import fr.univrouen.poste.domain.PosteCandidatureTag;
import fr.univrouen.poste.domain.PosteCandidatureTagValue;
import fr.univrouen.poste.domain.PosteAPourvoir;
import fr.univrouen.poste.domain.TemplateFile;
import fr.univrouen.poste.domain.User;
import lombok.Getter;
import lombok.Setter;

import java.util.List;
import java.util.Map;

@Getter
@Setter
public class PosteCandidatureSearchCriteria {

	RecevableEnum recevable;
	
	Boolean auditionnable;
	
	Boolean modification;
	
	List<String> numEmploiPostes;
	
	List<String> emailCandidats;

	List<PosteAPourvoir> postes;

	List<User> candidats;

	List<ReviewStatusTypes> reviewStatus;
	
	String searchText;
	
	TemplateFile templateFile;
	
    Map<PosteCandidatureTag, PosteCandidatureTagValue> tags;

}
