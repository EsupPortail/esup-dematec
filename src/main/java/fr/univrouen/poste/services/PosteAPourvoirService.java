package fr.univrouen.poste.services;

import fr.univrouen.poste.dao.PosteAPourvoirDao;
import fr.univrouen.poste.dao.PosteCandidatureDao;
import fr.univrouen.poste.domain.PosteAPourvoir;
import fr.univrouen.poste.domain.PosteCandidature;
import fr.univrouen.poste.domain.PosteCandidature.RecevableEnum;
import fr.univrouen.poste.domain.User;
import jakarta.annotation.Resource;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.time.Clock;
import java.time.LocalDateTime;

@Service
@Transactional
public class PosteAPourvoirService {

    @Resource
    Clock clock;

	@Resource
	AppliConfigService appliConfigService;

	@Resource
	PosteCandidatureDao posteCandidatureDao;

	@Resource
	PosteAPourvoirDao posteAPourvoirDao;

	public List<PosteAPourvoirAvailableBean>  getPosteAPourvoirAvailables(User candidat) {
		
		List<PosteAPourvoir> postesAPourvoir = posteAPourvoirDao.findPosteAPourvoirsByDateEndSignupCandidatGreaterThan(LocalDateTime.now(clock)).getResultList();

		Page<PosteCandidature> candidatures = posteCandidatureDao.findPosteCandidaturesByCandidat(candidat);
		Set<PosteAPourvoir> postesAlreadyCandidated = new HashSet<PosteAPourvoir>();
		Set<PosteAPourvoir> postesAlreadyCandidatedWithNoModifications = new HashSet<PosteAPourvoir>();
		for(PosteCandidature candidature: candidatures) {
			postesAlreadyCandidated.add(candidature.getPoste());
			if(candidature.getModification() == null) {
				postesAlreadyCandidatedWithNoModifications.add(candidature.getPoste());
			}
		}

		List<PosteAPourvoirAvailableBean> posteAvailables = new ArrayList<PosteAPourvoirAvailableBean>();
		for(PosteAPourvoir poste : postesAPourvoir) {
			PosteAPourvoirAvailableBean posteAvailable = new PosteAPourvoirAvailableBean();
			posteAvailable.setPoste(poste);
			posteAvailable.setCandidat(postesAlreadyCandidated.contains(poste));
			posteAvailable.setCanBeUnsubscribed(postesAlreadyCandidatedWithNoModifications.contains(poste));
			posteAvailables.add(posteAvailable);
		}

		return posteAvailables;
	}

	public void updateCandidatures(User candidat, List<Long> posteIds) {

		Page<PosteCandidature> candidatures = posteCandidatureDao.findPosteCandidaturesByCandidat(candidat);
		Set<PosteAPourvoir> postesAlreadyCandidated = new HashSet<PosteAPourvoir>();
		for(PosteCandidature candidature: candidatures) {
			postesAlreadyCandidated.add(candidature.getPoste());
		}

        LocalDateTime now = LocalDateTime.now(clock);
        List<PosteAPourvoir> postesAPourvoir = posteAPourvoirDao
                .findPosteAPourvoirsByDateEndSignupCandidatGreaterThan(now).getResultList();
		
		for(Long posteId: posteIds) {
			PosteAPourvoir poste = posteAPourvoirDao.findPosteAPourvoir(posteId);
			if(!postesAlreadyCandidated.contains(poste) && postesAPourvoir.contains(poste)) {
				
			// new Candidature
			PosteCandidature candidature = new PosteCandidature();
			candidature.setCandidat(candidat);
			candidature.setPoste(poste);

			candidature.setCreation(now);

				RecevableEnum recevableEnum = appliConfigService.getCacheCandidatureRecevableEnumDefault();
				candidature.setRecevableEnum(recevableEnum);

				posteCandidatureDao.savePosteCandidature(candidature);
				postesAlreadyCandidated.add(poste);
			}
		} 

	}

}
