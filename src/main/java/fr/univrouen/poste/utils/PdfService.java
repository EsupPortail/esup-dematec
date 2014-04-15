package fr.univrouen.poste.utils;

import java.io.IOException;

import javax.annotation.Resource;

import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Service
public class PdfService {

	@Resource
	TxPdfService txPdfService;
	
	@Async
	public void updateNbPages(Long pcFileId) throws IOException {		
		txPdfService.updateNbPages(pcFileId);
	}



}
