package fr.univrouen.poste.services;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import fr.univrouen.poste.domain.AppliConfig;
import fr.univrouen.poste.domain.PosteAPourvoir;
import fr.univrouen.poste.domain.PosteCandidature;
import fr.univrouen.poste.domain.User;

@Service
@Transactional
public class PosteAPourvoirService {

	public List<PosteAPourvoirAvailableBean>  getPosteAPourvoirAvailables(User candidat) {
		
		List<PosteAPourvoir> postesAPourvoir = PosteAPourvoir.findPosteAPourvoirsByDateEndCandidatGreaterThan(new Date()).getResultList();

		List<PosteCandidature> candidatures = PosteCandidature.findPosteCandidaturesByCandidat(candidat).getResultList();
		Set<PosteAPourvoir> postesAlreadyCandidated = new HashSet<PosteAPourvoir>();
		for(PosteCandidature candidature: candidatures) {
			postesAlreadyCandidated.add(candidature.getPoste());
		}

		List<PosteAPourvoirAvailableBean> posteAvailables = new ArrayList<PosteAPourvoirAvailableBean>();
		for(PosteAPourvoir poste : postesAPourvoir) {
			PosteAPourvoirAvailableBean posteAvailable = new PosteAPourvoirAvailableBean();
			posteAvailable.setPoste(poste);
			posteAvailable.setCandidat(postesAlreadyCandidated.contains(poste));
			posteAvailables.add(posteAvailable);
		}

		return posteAvailables;
	}

	public void updateCandidatures(User candidat, List<Long> posteIds) {

		List<PosteCandidature> candidatures = PosteCandidature.findPosteCandidaturesByCandidat(candidat).getResultList();
		Set<PosteAPourvoir> postesAlreadyCandidated = new HashSet<PosteAPourvoir>();
		for(PosteCandidature candidature: candidatures) {
			postesAlreadyCandidated.add(candidature.getPoste());
		}

		List<PosteAPourvoir> postesAPourvoir = PosteAPourvoir.findPosteAPourvoirsByDateEndCandidatGreaterThan(new Date()).getResultList();
		
		for(Long posteId: posteIds) {
			PosteAPourvoir poste = PosteAPourvoir.findPosteAPourvoir(posteId);
			if(!postesAlreadyCandidated.contains(poste) && postesAPourvoir.contains(poste)) {
				
				// new Candidature
				PosteCandidature candidature = new PosteCandidature();
				candidature.setCandidat(candidat);
				candidature.setPoste(poste);

				Calendar cal = Calendar.getInstance();
				Date currentTime = cal.getTime();
				candidature.setCreation(currentTime);

				Boolean recevable = AppliConfig.getCacheCandidatureRecevableDefault();
				candidature.setRecevable(recevable);

				candidature.persist();
			}
		} 

	}

}
