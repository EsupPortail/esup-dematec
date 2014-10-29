package fr.univrouen.poste.services;

import java.io.InputStream;
import java.io.OutputStream;
import java.sql.SQLException;
import java.util.Map;

import javax.annotation.Resource;

import org.springframework.stereotype.Service;

import fr.univrouen.poste.domain.GalaxieEntry;
import fr.univrouen.poste.domain.GalaxieExcel;
import fr.univrouen.poste.domain.PosteCandidature;

@Service
public class TemplateService {

    
    @Resource
    GalaxieExcelParser galaxieExcelParser;
    
    @Resource
    WordParser wordParser;

    public void generateTemplateFile(InputStream templateDocx, PosteCandidature candidature, OutputStream out) throws SQLException {
    	
    	Map<String, String> textMap = null;
    	
    	GalaxieEntry galaxieEntry = GalaxieEntry.findGalaxieEntrysByCandidature(candidature).getSingleResult();
    	for(GalaxieExcel galaxieExcel: GalaxieExcel.findAllGalaxieExcels("creation", "desc")) {
    		textMap = galaxieExcelParser.getCells4GalaxieEntry(galaxieExcel, galaxieEntry);
    		if(textMap != null) {
    			break;
    		}
    	}
    	
    	wordParser.modifyWord(templateDocx, textMap, out);	
    }

}
