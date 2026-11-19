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
import java.sql.SQLException;
import java.util.List;

@Service
public class TxPdfService {

	final Logger log = LoggerFactory.getLogger(TxPdfService.class);

	@Resource
	PosteCandidatureFileDao posteCandidatureFileDao;

    @Transactional
    public void updateNbPages(Long pcFileId) {
        PosteCandidatureFile file = posteCandidatureFileDao.findPosteCandidatureFile(pcFileId);
        if (file == null) {
            return;
        }

        try (InputStream input = file.getBigFile().getBinaryFile().getBinaryStream();
             PDDocument document = PDDocument.load(input)) {
            long pages = document.getNumberOfPages();
            file.setNbPages(pages);
            log.info("{} contient {} pages.", file.getFilename(), pages);
        } catch (IOException | SQLException e) {
            log.info("Exception reading {} like a pdf file for counting pages.", file.getFilename(), e);
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
