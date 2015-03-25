package fr.univrouen.poste.services;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StopWatch;

import fr.univrouen.poste.domain.GalaxieEntry;
import fr.univrouen.poste.domain.User;
import fr.univrouen.poste.exceptions.EsupDematEcWarnException;

@Service
public class GalaxieEntriesService {
	
	private final Logger logger = Logger.getLogger(getClass());
	
	@Autowired 
	GalaxieEntryService galaxieEntryService;
	
	@Autowired 
    private LogService logService;

    public synchronized void generateCandidatsPostes() {
    	
		StopWatch chrono = new StopWatch();
        chrono.start();

		generateCandidats();      
        
    	generatePostes();  
        
    	generateCandidatures();  

        chrono.stop();
		logger.info("La génération des candidats/postes/candidature a été effectuée en " + chrono.getTotalTimeMillis()/1000.0 + " sec.");
    }


    private void generateCandidatures() {
		List<GalaxieEntry> galaxieEntrys;
		galaxieEntrys = GalaxieEntry.findGalaxieEntrysByCandidatureIsNull().getResultList();
    	Set<User> candidatureUsers = new HashSet<User>();
        for(GalaxieEntry  galaxieEntry : galaxieEntrys) {	
        	if(galaxieEntry.getCandidat() != null) {
        		candidatureUsers.add(galaxieEntry.getCandidat());
        	}
        }
        for(User user: candidatureUsers) {
        	String userEmail = user.getEmailAddress();
        	try{
        		galaxieEntryService.generateCandidatures(user);
        	} catch(Exception e) {
				logService.logImportGalaxie(e.getMessage(), LogService.IMPORT_FAILED);
				logger.error("Import of " + userEmail + " candidatures failed", e);
        	}
        }
	}


    private void generatePostes() {
		List<GalaxieEntry> galaxieEntrys;
		galaxieEntrys = GalaxieEntry.findGalaxieEntrysByPosteIsNull().getResultList();
        for(GalaxieEntry  galaxieEntry : galaxieEntrys) {	
        	String galaxyEntryStr = galaxieEntry.toString();
        	try{
        		galaxieEntryService.generatePoste(galaxieEntry);
        	} catch(Exception e) {
				logService.logImportGalaxie(e.getMessage(), LogService.IMPORT_FAILED);
				logger.error("Import of " + galaxyEntryStr + " failed", e);
        	}
        }
	}


    private void generateCandidats() {
		List<GalaxieEntry> galaxieEntrys = GalaxieEntry.findGalaxieEntrysByCandidatIsNull().getResultList();
        for(GalaxieEntry  galaxieEntry : galaxieEntrys) {	
        	String galaxyEntryStr = galaxieEntry.toString();
        	try{
        		galaxieEntryService.generateCandidat(galaxieEntry);
        	} catch(EsupDematEcWarnException ew) {
				logService.logImportGalaxie(ew.getMessage(), LogService.IMPORT_FAILED);
				logger.warn("Import of " + galaxyEntryStr + " failed", ew);
        	} catch(Exception e) {
				logService.logImportGalaxie(e.getMessage(), LogService.IMPORT_FAILED);
				logger.error("Import of " + galaxyEntryStr + " failed", e);
        	}
        }
	}
}
