package fr.univrouen.poste.services;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import fr.univrouen.poste.dao.GalaxieEntryDao;
import jakarta.annotation.Resource;
import org.slf4j.Logger; import org.slf4j.LoggerFactory;
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
	
	final Logger logger = LoggerFactory.getLogger(getClass());
	
	@Resource
	GalaxieEntryService galaxieEntryService;
	
	@Resource
    LogService logService;

	@Resource
	GalaxieEntryDao galaxieEntryDao;

    public synchronized void generateCandidatsPostes() {
    	
		StopWatch chrono = new StopWatch();
        chrono.start();

		generateCandidats();      
        
    	generatePostes();  
        
    	generateCandidatures();  

        chrono.stop();
		logger.info("La génération des candidats/postes/candidature a été effectuée en " + chrono.getTotalTimeMillis()/1000.0 + " sec.");
    }


    void generateCandidatures() {
		List<GalaxieEntry> galaxieEntrys;
		galaxieEntrys = galaxieEntryDao.findGalaxieEntrysByCandidatureIsNull();
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


    void generatePostes() {
		List<GalaxieEntry> galaxieEntrys = galaxieEntryDao.findGalaxieEntrysByPosteIsNull();
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


    void generateCandidats() {
		List<GalaxieEntry> galaxieEntrys = galaxieEntryDao.findGalaxieEntrysByCandidatIsNull();
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
