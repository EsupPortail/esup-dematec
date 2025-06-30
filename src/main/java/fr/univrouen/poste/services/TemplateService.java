package fr.univrouen.poste.services;

import fr.univrouen.poste.dao.GalaxieEntryDao;
import fr.univrouen.poste.dao.GalaxieExcelDao;
import fr.univrouen.poste.domain.GalaxieEntry;
import fr.univrouen.poste.domain.GalaxieExcel;
import fr.univrouen.poste.domain.PosteCandidature;
import fr.univrouen.poste.domain.TemplateFile;
import jakarta.annotation.Resource;
import jakarta.servlet.ServletOutputStream;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
public class TemplateService {

	final Logger log = LoggerFactory.getLogger(getClass());
	
    @Resource
    GalaxieExcelParser galaxieExcelParser;
    
    @Resource
    WordParser wordParser;

	@Resource
	GalaxieExcelDao galaxieExcelDao;

	@Resource
	GalaxieEntryDao galaxieEntryDao;

    public void generateTemplateFile(TemplateFile templateFile, PosteCandidature candidature, OutputStream out) throws SQLException, IOException {
    	
    	Map<String, String> textMap = null;
    	
    	GalaxieEntry galaxieEntry = galaxieEntryDao.findGalaxieEntrysByCandidature(candidature);
    	for(GalaxieExcel galaxieExcel: galaxieExcelDao.findAllGalaxieExcels("creation", "desc")) {
    		textMap = galaxieExcelParser.getCells4GalaxieEntry(galaxieExcel, galaxieEntry);
    		if(textMap != null) {
    			break;
    		}
    	}
    	textMap.putAll(candidature.getMapFields());
    	
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
    		GalaxieEntry galaxieEntry = galaxieEntryDao.findGalaxieEntrysByCandidature(candidature);
        	for(GalaxieExcel galaxieExcel: galaxieExcelDao.findAllGalaxieExcels("creation", "desc")) {
        		textMap = galaxieExcelParser.getCells4GalaxieEntry(galaxieExcel, galaxieEntry);
        		if(textMap != null) {
        			break;
        		}
        	}
        	textMap.putAll(candidature.getMapFields());
        	textMaps.add(textMap);
    	}
    	
    	InputStream docxInputStream = templateFile.getBigFile().getBinaryFile().getBinaryStream();
		byte[] docxBytes = IOUtils.toByteArray(docxInputStream);
    	ByteArrayInputStream docx = new ByteArrayInputStream(docxBytes);
    	wordParser.modifyWord(docx, textMaps.get(0), textMaps, out);
		
	}

	@Transactional
	public List<String> getGalaxieKeys() {
		List<String> galaxieKeys = new ArrayList<String>();
		List<GalaxieExcel> galaxieExcels  = galaxieExcelDao.findAllGalaxieExcels("creation", "desc");
		List<GalaxieEntry> galaxieEntries = galaxieEntryDao.findAllGalaxieEntrys("id", "desc");
		if(!galaxieExcels.isEmpty() && !galaxieEntries.isEmpty()) {
			try {
				Map<String, String> textMap = galaxieExcelParser.getCells4GalaxieEntry(galaxieExcels.get(0), galaxieEntries.get(0));
				if(textMap != null) {
					for(String key : textMap.keySet()) {
						String cleanKey = StringUtils.stripAccents(key);
						cleanKey = cleanKey.replaceAll(" ", "_");
						cleanKey = cleanKey.replaceAll( "\\W", "");
						galaxieKeys.add(cleanKey);
					}
				}
			} catch (SQLException | IOException e) {
				log.debug("Exception getting galaxie keys", e);
			}
		}
		return galaxieKeys;
	}

}
