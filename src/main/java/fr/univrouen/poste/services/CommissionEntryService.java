package fr.univrouen.poste.services;

import java.util.HashSet;

import javax.persistence.TypedQuery;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import fr.univrouen.poste.domain.CommissionEntry;
import fr.univrouen.poste.domain.PosteAPourvoir;
import fr.univrouen.poste.domain.User;
import fr.univrouen.poste.exceptions.EsupDematEcException;
import fr.univrouen.poste.web.UserRegistrationForm;

@Service
public class CommissionEntryService {

	@Autowired 
	private CreateUserService createUserService;
	
	@Autowired 
    private LogService logService;
	
	/**
	 * 	IMPORTANT : le commissionEntry ayant été récupéré dans un autre contexte transactionnel, on doit faire un merge dessus ici (commissionEntry.merge())
		on le fait qu'en cas de modification cependant, pour des raisons de perf (update sql).
	 */
	@Transactional
	public void generateCommission(CommissionEntry commissionEntry) {

		boolean commissionEntryModified = false;
		
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
					
					logService.logImportCommission("Membre " + membre.getEmailAddress() + " créé.", LogService.IMPORT_SUCCESS);
				} else {
					String message = "Le mail n'a pu être envoyé pour le membre " + commissionEntry.getEmail();
					throw new EsupDematEcException(message);
				}
				
			} else {
				membre = query.getSingleResult();
			}
			if(membre.getIsCandidat()) {
				throw new EsupDematEcException("L'utilisateur " + membre.getEmailAddress() + " est déjà candidat, il ne peut également être membre dans l'application (avec ce même email).");
			} else {
				commissionEntry.setMembre(membre);
			}    	
			commissionEntryModified = true;
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
			
			commissionEntryModified = true;
		}
		
		if(commissionEntry.getMembre() != null && commissionEntry.getPoste() != null) {
		   	if(commissionEntry.getPoste().getMembres() == null || !commissionEntry.getPoste().getMembres().contains(commissionEntry.getMembre())) {           		
		   		PosteAPourvoir poste = commissionEntry.getPoste();
		   		if(poste.getMembres() == null) 
		   			poste.setMembres(new HashSet<User>());       			
		   			
		   		poste.getMembres().add(commissionEntry.getMembre());
		   		logService.logImportCommission("Membre " + commissionEntry.getMembre().getEmailAddress() + " ajouté comme membre pour le poste " + poste.getNumEmploi() + ".", LogService.IMPORT_SUCCESS);   		
		   	}
		   	commissionEntryModified = true;
		}
		
		if(commissionEntryModified) {
			commissionEntry.merge();
		}

	}
}
