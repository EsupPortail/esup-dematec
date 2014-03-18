package fr.univrouen.poste.services;

import java.util.HashSet;

import javax.persistence.TypedQuery;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import fr.univrouen.poste.domain.CommissionEntry;
import fr.univrouen.poste.domain.PosteAPourvoir;
import fr.univrouen.poste.domain.User;
import fr.univrouen.poste.web.UserRegistrationForm;

@Service
public class CommissionEntryService {

	@Autowired 
	private CreateUserService createUserService;
	
	@Autowired 
    private LogService logService;
	

	public void generateCommission(CommissionEntry commissionEntry) {
		if(commissionEntry.getMembre()==null) {
			User membre = null;
			TypedQuery<User> query = User.findUsersByEmailAddress(commissionEntry.getEmail(), null, null);
			if(query.getResultList().isEmpty()) {

				// new User 
				UserRegistrationForm userRegistration = new UserRegistrationForm();
				userRegistration.setEmailAddress(commissionEntry.getEmail());
				membre = createUserService.createMembreUser(userRegistration);
					
				if(membre != null) {
					// Membre
					membre.setNom(commissionEntry.getNom());
					membre.setPrenom(commissionEntry.getPrenom());
					membre.persist();     
					
					logService.logImportCommission("Membre " + membre.getEmailAddress() + " créé.", LogService.IMPORT_SUCCESS);
				} else {
					String message = "Le mail n'a pu être envoyé pour le membre " + commissionEntry.getEmail();
					logService.logImportCommission(message, LogService.IMPORT_FAILED);
				}
				
			} else {
				membre = query.getSingleResult();
			}
			if(membre.getIsCandidat()) {
				logService.logImportCommission("L'utilisateur " + membre.getEmailAddress() + " est déjà candidat, il ne peut également être membre dans l'application (avec ce même email).", LogService.IMPORT_FAILED);
			} else {
				commissionEntry.setMembre(membre);
			}
			commissionEntry.persist();       		
		}
		
		if(commissionEntry.getMembre() != null && commissionEntry.getPoste() == null) {
			PosteAPourvoir poste = null;
			TypedQuery<PosteAPourvoir> query =  PosteAPourvoir.findPosteAPourvoirsByNumEmploi(commissionEntry.getNumPoste(), null, null);
			if(query.getResultList().isEmpty()) {
				
				// new Poste
				poste = new PosteAPourvoir();
				poste.setNumEmploi(commissionEntry.getNumPoste());
				poste.persist();
				
				logService.logImportCommission("Poste " + poste.getNumEmploi() + " créé.", LogService.IMPORT_SUCCESS);
				
			} else {
				poste = query.getSingleResult();
			}
			commissionEntry.setPoste(poste);
			commissionEntry.persist(); 
		}
		
		if(commissionEntry.getMembre() != null && commissionEntry.getPoste() != null) {
		   	if(commissionEntry.getPoste().getMembres() == null || !commissionEntry.getPoste().getMembres().contains(commissionEntry.getMembre())) {           		
		   		PosteAPourvoir poste = commissionEntry.getPoste();
		   		if(poste.getMembres() == null) 
		   			poste.setMembres(new HashSet<User>());       			
		   			
		   		poste.getMembres().add(commissionEntry.getMembre());
		   		poste.persist();  
		   		logService.logImportCommission("Membre " + commissionEntry.getMembre().getEmailAddress() + " ajouté comme membre pour le poste " + poste.getNumEmploi() + ".", LogService.IMPORT_SUCCESS);   		
		   	}
		}
	}
}
