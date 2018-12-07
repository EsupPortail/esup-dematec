package fr.univrouen.poste.services;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.annotation.Resource;
import javax.servlet.ServletOutputStream;

import org.apache.commons.io.IOUtils;
import org.springframework.stereotype.Service;

import fr.univrouen.poste.domain.GalaxieEntry;
import fr.univrouen.poste.domain.GalaxieExcel;
import fr.univrouen.poste.domain.PosteAPourvoir;
import fr.univrouen.poste.domain.PosteCandidature;
import fr.univrouen.poste.domain.TemplateFile;

@Service
public class TemplateService {

    
    @Resource
    GalaxieExcelParser galaxieExcelParser;
    
    @Resource
    WordParser wordParser;

    public void generateTemplateFile(TemplateFile templateFile, PosteCandidature candidature, OutputStream out) throws SQLException, IOException {
    	
    	Map<String, String> textMap = null;
    	
    	GalaxieEntry galaxieEntry = GalaxieEntry.findGalaxieEntrysByCandidature(candidature).getSingleResult();
    	for(GalaxieExcel galaxieExcel: GalaxieExcel.findAllGalaxieExcels("creation", "desc")) {
    		textMap = galaxieExcelParser.getCells4GalaxieEntry(galaxieExcel, galaxieEntry);
    		if(textMap != null) {
    			break;
    		}
    	}
    	
    	InputStream docxInputStream = templateFile.getBigFile().getBinaryFile().getBinaryStream();
		byte[] docxBytes = IOUtils.toByteArray(docxInputStream);
    	ByteArrayInputStream docx = new ByteArrayInputStream(docxBytes);
    	wordParser.modifyWord(docx, textMap, null, out);	
    }

	public void generateTemplateFile(TemplateFile templateFile, List<PosteCandidature> candidatures,
			ServletOutputStream out) throws SQLException, IOException {
		
    	List<Map<String, String>> textMaps = new ArrayList<Map<String, String>>();
    	
    	for(PosteCandidature candidature : candidatures) {
    		Map<String, String> textMap = null;
    		GalaxieEntry galaxieEntry = GalaxieEntry.findGalaxieEntrysByCandidature(candidature).getSingleResult();
        	for(GalaxieExcel galaxieExcel: GalaxieExcel.findAllGalaxieExcels("creation", "desc")) {
        		textMap = galaxieExcelParser.getCells4GalaxieEntry(galaxieExcel, galaxieEntry);
        		if(textMap != null) {
        			break;
        		}
        	}
        	textMaps.add(textMap);
    	}
    	
    	InputStream docxInputStream = templateFile.getBigFile().getBinaryFile().getBinaryStream();
		byte[] docxBytes = IOUtils.toByteArray(docxInputStream);
    	ByteArrayInputStream docx = new ByteArrayInputStream(docxBytes);
    	wordParser.modifyWord(docx, textMaps.get(0), textMaps, out);
		
	}

}
