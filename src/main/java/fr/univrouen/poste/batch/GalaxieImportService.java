package fr.univrouen.poste.batch;

import fr.univrouen.poste.dao.BigFileDao;
import fr.univrouen.poste.dao.GalaxieExcelDao;
import fr.univrouen.poste.domain.GalaxieExcel;
import fr.univrouen.poste.services.GalaxieEntriesService;
import fr.univrouen.poste.services.GalaxieExcelParser;
import jakarta.annotation.Resource;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.orm.jpa.EntityManagerFactoryUtils;
import org.springframework.orm.jpa.EntityManagerHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import javax.sql.rowset.serial.SerialBlob;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.sql.SQLException;
import java.time.LocalDateTime;

@Service
public class GalaxieImportService {

	final Logger logger = LoggerFactory.getLogger(getClass());
	
	@Resource
	GalaxieExcelParser galaxieExcelParser;

	@Resource
	GalaxieEntriesService galaxieEntriesService;
	
	@Resource
	EntityManagerFactory entityManagerFactory;

    @Resource
    GalaxieExcelDao galaxieExcelDao;

	@Resource
	BigFileDao bigFileDao;

	@Transactional
	public void importGalaxie(String galaxieFilePath) throws IOException, SQLException {
		
		File file = new File(galaxieFilePath);
        String filename = file.getName();
        InputStream inputStream = new FileInputStream(file);
        byte[] bytes = IOUtils.toByteArray(inputStream);

        GalaxieExcel galaxieExcel = new GalaxieExcel();
        galaxieExcel.setFilename(filename);
        galaxieExcel.getBigFile().setBinaryFile(new SerialBlob(bytes));
		bigFileDao.saveBigFile(galaxieExcel.getBigFile());
        
        // set current date
        galaxieExcel.setCreation(LocalDateTime.now());

		galaxieExcel.setFileSize(Long.valueOf(bytes.length));
        
        // persist
        galaxieExcelDao.saveGalaxieExcel(galaxieExcel);
        
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
