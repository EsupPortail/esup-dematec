package fr.univrouen.poste.services;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.springframework.stereotype.Service;

import fr.univrouen.poste.domain.PosteAPourvoir;
import fr.univrouen.poste.domain.PosteCandidature;
import fr.univrouen.poste.domain.User;

@Service
public class PosteAPourvoirService {

	public List<PosteAPourvoirAvailableBean>  getPosteAPourvoirAvailables(User candidat) {
		// TODO dates début et fin de postes à pourvoir
		List<PosteAPourvoir> postes = PosteAPourvoir.findAllPosteAPourvoirs();
		
		List<PosteCandidature> candidatures = PosteCandidature.findPosteCandidaturesByCandidat(candidat).getResultList();
		Set<PosteAPourvoir> postesAlreadyCandidated = new HashSet<PosteAPourvoir>();
		for(PosteCandidature candidature: candidatures) {
			postesAlreadyCandidated.add(candidature.getPoste());
		}
		
		List<PosteAPourvoirAvailableBean> posteAvailables = new ArrayList<PosteAPourvoirAvailableBean>();
		for(PosteAPourvoir poste : postes) {
			PosteAPourvoirAvailableBean posteAvailable = new PosteAPourvoirAvailableBean();
			posteAvailable.setPoste(poste);
			posteAvailable.setCandidat(postesAlreadyCandidated.contains(poste));
			posteAvailables.add(posteAvailable);
		}
		
		return posteAvailables;
	}

}
