package fr.univrouen.poste.utils;

import fr.univrouen.poste.dao.PosteCandidatureFileDao;
import fr.univrouen.poste.domain.PosteCandidatureFile;
import fr.univrouen.poste.exceptions.EsupDematEcException;
import jakarta.annotation.Resource;
import org.apache.pdfbox.io.MemoryUsageSetting;
import org.apache.pdfbox.multipdf.PDFMergerUtility;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;

@Service
public class TxPdfService {

	final Logger log = LoggerFactory.getLogger(TxPdfService.class);

	@Resource
	PosteCandidatureFileDao posteCandidatureFileDao;

	@Transactional
	public void updateNbPages(Long pcFileId) throws IOException, InterruptedException {
		
		PosteCandidatureFile pcFile = posteCandidatureFileDao.findPosteCandidatureFile(pcFileId);
		
		long sleepTime = 0;
		while(pcFile == null && sleepTime<60000) {
			sleepTime += 5000;
			Thread.sleep(sleepTime);
			pcFile = posteCandidatureFileDao.findPosteCandidatureFile(pcFileId);
		}
		
		PDDocument doc = null;
		try {
			doc = PDDocument.load(pcFile.getBigFile().getBinaryFile().getBinaryStream());
			long nbPages = doc.getNumberOfPages();
			pcFile.setNbPages(nbPages);
			log.info(pcFile.getFilename() + " contient " + nbPages + " pages.");
		} catch (Exception e) {
			log.info("Exception reading " + pcFile.getFilename() + " like a pdf file for counting pages.", e);
		} finally {
			if(doc != null) {
				doc.close();
			}
		}

	}

	
	public void mergePdfs(List<InputStream> pdfFiles, String filename, OutputStream destStream) {
		PDFMergerUtility ut = new PDFMergerUtility();
		ut.setDestinationFileName(filename);
		ut.setDestinationStream(destStream);
		for(InputStream pdfFile: pdfFiles) {
			ut.addSource(pdfFile);
		}
		try {
			ut.mergeDocuments(MemoryUsageSetting.setupMainMemoryOnly());
		} catch (IOException e) {
			throw new EsupDematEcException("Error merging pdf files- " + filename, e);
		}	
	}
	
}
