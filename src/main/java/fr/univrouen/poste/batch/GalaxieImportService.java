package fr.univrouen.poste.batch;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.sql.SQLException;
import java.util.Calendar;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.sql.rowset.serial.SerialBlob;
import javax.sql.rowset.serial.SerialException;

import org.apache.commons.io.IOUtils;
import org.apache.log4j.Logger;
import org.hibernate.Session;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.orm.hibernate3.SessionFactoryUtils;
import org.springframework.orm.hibernate3.SessionHolder;
import org.springframework.orm.jpa.EntityManagerFactoryUtils;
import org.springframework.orm.jpa.EntityManagerHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import fr.univrouen.poste.domain.GalaxieExcel;
import fr.univrouen.poste.services.ExcelParser;
import fr.univrouen.poste.services.GalaxieEntriesService;
import fr.univrouen.poste.services.GalaxieExcelParser;

@Service
public class GalaxieImportService {

	private final Logger logger = Logger.getLogger(getClass());
	
	@Autowired 
	GalaxieExcelParser galaxieExcelParser;
	
	@Autowired 	
	ExcelParser excelParser;
	
	@Autowired
	GalaxieEntriesService galaxieEntriesService;
	
	@Autowired
	EntityManagerFactory entityManagerFactory;

	@Transactional
	public void importGalaxie(String galaxieFilePath) throws IOException, SerialException, SQLException {
		
		File file = new File(galaxieFilePath);
        String filename = file.getName();
        InputStream inputStream = new FileInputStream(file);
        byte[] bytes = IOUtils.toByteArray(inputStream);

        GalaxieExcel galaxieExcel = new GalaxieExcel();
        galaxieExcel.setFilename(filename);
        galaxieExcel.getBigFile().setBinaryFile(new SerialBlob(bytes)); 
        galaxieExcel.getBigFile().persist();
        
        // set current date 
        Calendar cal = Calendar.getInstance();
        galaxieExcel.setCreation(cal.getTime());    
        
        // persist
        galaxieExcel.persist();
        
        // process : generate GalaxieEntries
    	galaxieExcelParser.process(galaxieExcel);
    	
	}
	
	/**
	 * Reprise du code de OpenEntityManagerInViewFilter pour appel de generateCandidatsPostes
	 * avec gestion similaire de  la session + transactions
	 */
	public void generateCandidatsPostes() {
		
		EntityManager em =  entityManagerFactory.createEntityManager();
		EntityManagerHolder emHolder = new EntityManagerHolder(em);
		TransactionSynchronizationManager.bindResource(entityManagerFactory, emHolder);
		
		try {
			galaxieEntriesService.generateCandidatsPostes();             
		} finally {
			emHolder = (EntityManagerHolder) TransactionSynchronizationManager.unbindResource(entityManagerFactory);
			logger.debug("Closing JPA EntityManager in OpenEntityManagerInViewFilter");
			EntityManagerFactoryUtils.closeEntityManager(emHolder.getEntityManager());
		}
		
	}

}
