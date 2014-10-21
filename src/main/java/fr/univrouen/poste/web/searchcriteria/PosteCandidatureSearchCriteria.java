package fr.univrouen.poste.web.searchcriteria;

import java.util.List;

import org.springframework.roo.addon.javabean.RooJavaBean;

import fr.univrouen.poste.domain.PosteAPourvoir;
import fr.univrouen.poste.domain.User;

@RooJavaBean
public class PosteCandidatureSearchCriteria {

	Boolean recevable;
	
	Boolean auditionnable;
	
	List<PosteAPourvoir> postes;
	
	List<User> candidats;
}
