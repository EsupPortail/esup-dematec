package fr.univrouen.poste.services;

import fr.univrouen.poste.dao.*;
import fr.univrouen.poste.domain.GalaxieEntry;
import fr.univrouen.poste.domain.PosteAPourvoir;
import fr.univrouen.poste.domain.PosteCandidature;
import fr.univrouen.poste.domain.PosteCandidature.RecevableEnum;
import fr.univrouen.poste.domain.User;
import fr.univrouen.poste.exceptions.EsupDematEcException;
import fr.univrouen.poste.exceptions.EsupDematEcWarnException;
import fr.univrouen.poste.web.UserRegistrationForm;
import jakarta.annotation.Resource;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.text.WordUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

@Service
public class GalaxieEntryService {

	@Resource
	CreateUserService createUserService;
	
	@Resource
    LogService logService;
	
	@Resource
	EmailService emailService;

	@Resource
	AppliConfigDao appliConfigDao;

	@Resource
	AppliConfigService appliConfigService;

	@Resource
	GalaxieEntryDao galaxieEntryDao;

	@Resource
	UserDao userDao;

	@Resource
	PosteCandidatureDao posteCandidatureDao;

	@Resource
	PosteAPourvoirDao posteAPourvoirDao;

	/**
	 * 	IMPORTANT : le galaxieEntry ayant été récupéré dans un autre contexte transactionnel, on doit faire un merge dessus ici (galaxieEntry.merge())
	 */
	@Transactional
	public void generateCandidat(GalaxieEntry galaxieEntry) {
		
		if(galaxieEntry.getCandidat() == null) {
			User candidat = userDao.findUserByNumCandidat(galaxieEntry.getNumCandidat());
			if(candidat == null) {
				if(galaxieEntry.getEmail() == null || galaxieEntry.getEmail().isEmpty()) {
					String message = "Le candidat " + galaxieEntry.getNumCandidat() + " n'a pas de mail de renseigné";
					throw new EsupDematEcWarnException(message);
				} else {
					User userSameEmail = userDao.findUserByEmailAddress(galaxieEntry.getEmail());
					if(userSameEmail != null) {
						String message = "Le candidat " + galaxieEntry.getNumCandidat() + " a le même mail que l'utilisateur suivant :";
						message = message + " " + userSameEmail.getEmailAddress() + "(n° candidat : " + userSameEmail.getNumCandidat() + ")";
						throw new EsupDematEcException(message);
					} else {
		        		// new User 
		        		UserRegistrationForm userRegistration = new UserRegistrationForm();
		        		userRegistration.setEmailAddress(galaxieEntry.getEmail());
		        		userRegistration.setLastName(galaxieEntry.getNom());
		        		userRegistration.setFirstName(galaxieEntry.getPrenom());
						candidat = createUserService.createCandidatUser(userRegistration);
							
						if(candidat != null) {
			        		// Candidat
			        		candidat.setNumCandidat(galaxieEntry.getNumCandidat());
			        		candidat.setCivilite(galaxieEntry.getCivilite());
			        		candidat.setEmailAddress(galaxieEntry.getEmail());
			        		candidat.setNom(galaxieEntry.getNom());
			        		candidat.setPrenom(galaxieEntry.getPrenom());   
			        		
			        		logService.logImportGalaxie("Candidat " + candidat.getNumCandidat() + " créé.", LogService.IMPORT_SUCCESS);
		        		} else {
		        			String message = "Le mail n'a pu être envoyé pour le candidat " + galaxieEntry.getNumCandidat() + " [" + galaxieEntry.getEmail() + "]";
		        			throw new EsupDematEcException(message);
						}		        			
		    		}
				}
				
			}

			galaxieEntry.setCandidat(candidat);

			galaxieEntryDao.saveGalaxieEntry(galaxieEntry);
		}
		
	}
	
	
	/**
	 * 	IMPORTANT : le galaxieEntry ayant été récupéré dans un autre contexte transactionnel, on doit faire un merge dessus ici (galaxieEntry.merge())
	 */
	@Transactional
	public void generatePoste(GalaxieEntry galaxieEntry) {
		
		if(galaxieEntry.getCandidat() != null && galaxieEntry.getPoste() == null) {
			PosteAPourvoir poste = null;
			List<PosteAPourvoir> postes = posteAPourvoirDao.findPosteAPourvoirsByNumEmplois(List.of(galaxieEntry.getNumEmploi()));
			if(postes.isEmpty()) {

				// new Poste
				poste = new PosteAPourvoir();
				poste.setLocalisation(galaxieEntry.getLocalisation());
				poste.setNumEmploi(galaxieEntry.getNumEmploi());
				poste.setProfil(galaxieEntry.getProfil());
				posteAPourvoirDao.savePosteAPourvoir(poste);

				logService.logImportGalaxie("Poste " + poste.getNumEmploi() + " créé.", LogService.IMPORT_SUCCESS);
				
			} else {
				poste = postes.get(0);
			}
			galaxieEntry.setPoste(poste);

			galaxieEntryDao.saveGalaxieEntry(galaxieEntry);
		}
		
	}
	
	/**
	 * 	IMPORTANT : le galaxieEntry ayant été récupéré dans un autre contexte transactionnel, on doit faire un merge dessus ici (galaxieEntry.merge())
	 */
	@Transactional
	public void generateCandidatures(User candidat) {

		List<String> postes = new ArrayList<String>();
		
		List<GalaxieEntry> galaxieEntries = galaxieEntryDao.findGalaxieEntrysByCandidat(candidat);
		
		String nom = "";
		String prenom = "";
		String civilite = "";
		
		for(GalaxieEntry galaxieEntry : galaxieEntries) {
			
			if(galaxieEntry.getCandidat() != null && galaxieEntry.getPoste() != null && galaxieEntry.getCandidature() == null) {
				
				// new Candidature
				PosteCandidature candidature = new PosteCandidature();
				candidature.setCandidat(galaxieEntry.getCandidat());
				candidature.setPoste(galaxieEntry.getPoste());
				
			    Calendar cal = Calendar.getInstance();
			    Date currentTime = cal.getTime();
			    candidature.setCreation(currentTime);
			    
			    RecevableEnum recevableEnum = appliConfigService.getCacheCandidatureRecevableEnumDefault();
			    candidature.setRecevableEnum(recevableEnum);

				posteCandidatureDao.savePosteCandidature(candidature);
				galaxieEntry.setCandidature(candidature);   
				
				logService.logImportGalaxie("Candidature " + candidature.getPoste().getNumEmploi() + "/" + candidature.getCandidat().getNumCandidat() + " créé.", LogService.IMPORT_SUCCESS);

				galaxieEntryDao.saveGalaxieEntry(galaxieEntry);
				
				postes.add(candidature.getPoste().getNumEmploi());
				
				nom = galaxieEntry.getNom();
				prenom = galaxieEntry.getPrenom();
				civilite = galaxieEntry.getCivilite();
			}
		}
		
		// send email notification    
	    String mailTo = candidat.getEmailAddress();
	    String mailFrom = appliConfigDao.getAppliConfig().getMailFrom();
	    String mailSubject = appliConfigDao.getAppliConfig().getMailSubject();	    
	    String mailMessage = appliConfigDao.getAppliConfig().getTexteMailNewCandidatures();
	    
	    mailMessage = mailMessage.replaceAll("@@postes@@", StringUtils.join(postes, ","));
	    mailMessage = mailMessage.replaceAll("@@nom@@",  WordUtils.capitalizeFully(nom));
	    mailMessage = mailMessage.replaceAll("@@prenom@@", WordUtils.capitalizeFully(prenom));
	    mailMessage = mailMessage.replaceAll("@@civilite@@", civilite);
	    
	    emailService.sendMessage(mailFrom, mailTo, mailSubject, mailMessage);
		
	}
}
