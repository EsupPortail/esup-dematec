package fr.univrouen.poste.services;

import java.util.Calendar;
import java.util.Date;
import java.util.List;

import javax.persistence.TypedQuery;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import fr.univrouen.poste.domain.AppliConfig;
import fr.univrouen.poste.domain.GalaxieEntry;
import fr.univrouen.poste.domain.PosteAPourvoir;
import fr.univrouen.poste.domain.PosteCandidature;
import fr.univrouen.poste.domain.User;
import fr.univrouen.poste.web.UserRegistrationForm;

@Service
public class GalaxieEntryService {

	@Autowired 
	private CreateUserService createUserService;
	
	@Autowired 
    private LogService logService;
	
	@Transactional
	public void generateCandidatPoste(GalaxieEntry galaxieEntry) {
		if(galaxieEntry.getCandidat() == null) {
			User candidat = null;
			TypedQuery<User> query = User.findUsersByNumCandidat(galaxieEntry.getNumCandidat(), null, null);
			if(query.getResultList().isEmpty()) {
				if(galaxieEntry.getEmail() == null || galaxieEntry.getEmail().isEmpty()) {
					String message = "Le candidat " + galaxieEntry.getNumCandidat() + " n'a pas de mail de renseigné";
					logService.logImportGalaxie(message, LogService.IMPORT_FAILED);
				} else {
					List<User> usersSameEmail = User.findUsersByEmailAddress(galaxieEntry.getEmail(), null, null).getResultList();
					if(!usersSameEmail.isEmpty()) {
						String message = "Le candidat " + galaxieEntry.getNumCandidat() + " a le même mail que le(s) utilisateur(s)";
						for(User u : usersSameEmail) 
							message = message + " " + u.getEmailAddress() + "(n° candidat : " + u.getNumCandidat() + ")";
						logService.logImportGalaxie(message, LogService.IMPORT_FAILED);
					} else {
		        		// new User 
		        		UserRegistrationForm userRegistration = new UserRegistrationForm();
		        		userRegistration.setEmailAddress(galaxieEntry.getEmail());
						candidat = createUserService.createCandidatUser(userRegistration);
							
						if(candidat != null) {
			        		// Candidat
			        		candidat.setNumCandidat(galaxieEntry.getNumCandidat());
			        		candidat.setCivilite(galaxieEntry.getCivilite());
			        		candidat.setEmailAddress(galaxieEntry.getEmail());
			        		candidat.setNom(galaxieEntry.getNom());
			        		candidat.setPrenom(galaxieEntry.getPrenom());
			        		candidat.persist();    
			        		
			        		logService.logImportGalaxie("Candidat " + candidat.getNumCandidat() + " créé.", LogService.IMPORT_SUCCESS);
		        		} else {
		        			String message = "Le mail n'a pu être envoyé pour le candidat " + galaxieEntry.getNumCandidat() + " [" + galaxieEntry.getEmail() + "]";
		        			logService.logImportGalaxie(message, LogService.IMPORT_FAILED);
						}		        			
		    		}
				}
				
			} else {
				candidat = query.getSingleResult();
			}
			
			if(candidat != null) {
				galaxieEntry.setCandidat(candidat);
			}
		}
		
		if(galaxieEntry.getCandidat() != null && galaxieEntry.getPoste() == null) {
			PosteAPourvoir poste = null;
			TypedQuery<PosteAPourvoir> query =  PosteAPourvoir.findPosteAPourvoirsByNumEmploi(galaxieEntry.getNumEmploi(), null, null);
			if(query.getResultList().isEmpty()) {
				
				// new Poste
				poste = new PosteAPourvoir();
				poste.setLocalisation(galaxieEntry.getLocalisation());
				poste.setNumEmploi(galaxieEntry.getNumEmploi());
				poste.setProfil(galaxieEntry.getProfil());
				poste.persist();
				
				logService.logImportGalaxie("Poste " + poste.getNumEmploi() + " créé.", LogService.IMPORT_SUCCESS);
				
			} else {
				poste = query.getSingleResult();
			}
			galaxieEntry.setPoste(poste);
		}
		
		if(galaxieEntry.getCandidat() != null && galaxieEntry.getPoste() != null && galaxieEntry.getCandidature() == null) {
			
			// new Candidature
			PosteCandidature candidature = new PosteCandidature();
			candidature.setCandidat(galaxieEntry.getCandidat());
			candidature.setPoste(galaxieEntry.getPoste());
			
		    Calendar cal = Calendar.getInstance();
		    Date currentTime = cal.getTime();
		    candidature.setCreation(currentTime);
		    
		    Boolean recevable = AppliConfig.getCacheCandidatureRecevableDefault();
		    candidature.setRecevable(recevable);
		    
			candidature.persist();
			galaxieEntry.setCandidature(candidature);   
			
			logService.logImportGalaxie("Candidature " + candidature.getPoste().getNumEmploi() + "/" + candidature.getCandidat().getNumCandidat() + " créé.", LogService.IMPORT_SUCCESS);
			
		}
	}
}
