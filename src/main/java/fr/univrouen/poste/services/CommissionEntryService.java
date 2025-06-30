package fr.univrouen.poste.services;

import fr.univrouen.poste.dao.AppliConfigDao;
import fr.univrouen.poste.dao.CommissionEntryDao;
import fr.univrouen.poste.dao.PosteAPourvoirDao;
import fr.univrouen.poste.dao.UserDao;
import fr.univrouen.poste.domain.CommissionEntry;
import fr.univrouen.poste.domain.PosteAPourvoir;
import fr.univrouen.poste.domain.User;
import fr.univrouen.poste.exceptions.EsupDematEcException;
import fr.univrouen.poste.web.UserRegistrationForm;
import jakarta.annotation.Resource;
import jakarta.persistence.TypedQuery;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

@Service
public class CommissionEntryService {

	@Resource
	CreateUserService createUserService;
	
	@Resource
    LogService logService;
	
	@Resource
	EmailService emailService;

	@Resource
	AppliConfigDao appliConfigDao;

	@Resource
	UserDao userDao;

	@Resource
	CommissionEntryDao commissionEntryDao;

	@Resource
	PosteAPourvoirDao posteAPourvoirDao;
	
	/**
	 * 	IMPORTANT : le commissionEntry ayant été récupéré dans un autre contexte transactionnel, on doit faire un merge dessus ici (commissionEntry.merge())
	 */
	@Transactional
	public void generateMembre(CommissionEntry commissionEntry) {

		if(commissionEntry.getMembre()==null) {
			User membre = userDao.findUsersByEmailAddress(commissionEntry.getEmail());
			if(membre == null) {

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
				
			}
			commissionEntry.setMembre(membre); 	
			commissionEntryDao.saveCommissionEntry(commissionEntry);
		}

	}
	
	
	
	/**
	 * 	IMPORTANT : le commissionEntry ayant été récupéré dans un autre contexte transactionnel, on doit faire un merge dessus ici (commissionEntry.merge())
	 */
	@Transactional
	public void generatePoste(CommissionEntry commissionEntry) {
		
		if(commissionEntry.getMembre() != null && commissionEntry.getPoste() == null) {
			PosteAPourvoir poste = null;
			TypedQuery<PosteAPourvoir> query =  posteAPourvoirDao.findPosteAPourvoirsByNumEmploi(commissionEntry.getNumPoste(), null, null);
			if(query.getResultList().isEmpty()) {
				
				// new Poste
				poste = new PosteAPourvoir();
				poste.setNumEmploi(commissionEntry.getNumPoste());
				posteAPourvoirDao.savePosteAPourvoir(poste);
				
				logService.logImportCommission("Poste " + poste.getNumEmploi() + " créé.", LogService.IMPORT_SUCCESS);
				
			} else {
				poste = query.getSingleResult();
			}
			commissionEntry.setPoste(poste);
			
			commissionEntryDao.saveCommissionEntry(commissionEntry);
		}

	}
	
	
	/**
	 * 	IMPORTANT : le commissionEntry ayant été récupéré dans un autre contexte transactionnel, on doit faire un merge dessus ici (commissionEntry.merge())
		on le fait qu'en cas de modification cependant, pour des raisons de perf (update sql).
	 */
	@Transactional
	public void generateCommission(User membre) {

		List<String> postes = new ArrayList<String>();
		
		List<CommissionEntry> commissionEntries = commissionEntryDao.findCommissionEntrysByMembre(membre);
		
		for(CommissionEntry commissionEntry: commissionEntries) {
			if(commissionEntry.getMembre() != null && commissionEntry.getPoste() != null) {
			   	if(commissionEntry.getPoste().getMembres() == null || !commissionEntry.getPoste().getMembres().contains(commissionEntry.getMembre())) {           		
			   		PosteAPourvoir poste = commissionEntry.getPoste();
			   		if(poste.getMembres() == null) {
			   			poste.setMembres(new HashSet<User>());
			   		}
			   			
			   		poste.getMembres().add(commissionEntry.getMembre());
			   		logService.logImportCommission("Membre " + commissionEntry.getMembre().getEmailAddress() + " ajouté comme membre pour le poste " + poste.getNumEmploi() + ".", LogService.IMPORT_SUCCESS); 
			   		postes.add(poste.getNumEmploi());
			   	}
			   	if(commissionEntry.getPresident() && (commissionEntry.getPoste().getPresidents() == null || !commissionEntry.getPoste().getPresidents().contains(commissionEntry.getMembre()))) {           		
			   		PosteAPourvoir poste = commissionEntry.getPoste();
			   		if(poste.getPresidents() == null) {
			   			poste.setPresidents(new HashSet<User>());
			   		}
			   			
			   		poste.getPresidents().add(commissionEntry.getMembre());
			   		logService.logImportCommission("Membre " + commissionEntry.getMembre().getEmailAddress() + " ajouté comme président pour le poste " + poste.getNumEmploi() + ".", LogService.IMPORT_SUCCESS); 
			   	}
			}
		}

		if(!postes.isEmpty()) {
			// send email notification    
		    String mailTo = membre.getEmailAddress();
		    String mailFrom = appliConfigDao.getAppliConfig().getMailFrom();
		    String mailSubject = appliConfigDao.getAppliConfig().getMailSubject();	    
		    String mailMessage = appliConfigDao.getAppliConfig().getTexteMailNewCommissions();
	
		    mailMessage = mailMessage.replaceAll("@@postes@@", StringUtils.join(postes, ","));
		    
		    emailService.sendMessage(mailFrom, mailTo, mailSubject, mailMessage);
		}
	    
	}
}
