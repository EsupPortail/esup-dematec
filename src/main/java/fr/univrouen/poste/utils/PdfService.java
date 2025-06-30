package fr.univrouen.poste.utils;

import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;

import jakarta.annotation.Resource;

import org.slf4j.Logger; import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Service
public class PdfService {
	
	final Logger log = LoggerFactory.getLogger(PdfService.class);
	
	@Resource
	TxPdfService txPdfService;
	
	@Async
	public void updateNbPages(Long pcFileId) {		
		try {
			txPdfService.updateNbPages(pcFileId);
		} catch (Exception e) {
			log.warn("updateNbPages failed on PosteCandidatureFile " + pcFileId);
		}
	}

	public void mergePdfs(List<InputStream> pdfFiles, String filename, OutputStream destStream) {
		txPdfService.mergePdfs(pdfFiles, filename, destStream);
	}

}
