/**
 * Licensed to ESUP-Portail under one or more contributor license
 * agreements. See the NOTICE file distributed with this work for
 * additional information regarding copyright ownership.
 *
 * ESUP-Portail licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except in
 * compliance with the License. You may obtain a copy of the License at:
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package fr.univrouen.poste.services;

import java.sql.SQLException;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import javax.persistence.TypedQuery;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StopWatch;

import fr.univrouen.poste.domain.GalaxieEntry;
import fr.univrouen.poste.domain.GalaxieExcel;
import fr.univrouen.poste.domain.User;

@Service
public class GalaxieExcelParser {

	private final Logger logger = Logger.getLogger(getClass());

	@Autowired
	ExcelParser excelParser;

	@Autowired	
	GalaxieMappingService galaxieMappingService;

	public void process(GalaxieExcel galaxieExcel) throws SQLException {
		
		StopWatch chrono = new StopWatch();
        chrono.start();
        
		List<List<String>> cells = excelParser.getCells(galaxieExcel.getBigFile().getBinaryFile().getBinaryStream());

		Map<String, Long> cellsPosition = new HashMap<String, Long>();

		int p = 0;
		List<String> cellsHead = cells.remove(0);
		for (String cellName : cellsHead) {
			cellsPosition.put(cellName, new Long(p++));
		}
		galaxieMappingService.checkCellsHead(cellsPosition);
        
		Map<List<String>, GalaxieEntry>  dbGalaxyEntries = new HashMap<List<String>, GalaxieEntry>();
		for(GalaxieEntry galaxieEntry : GalaxieEntry.findAllGalaxieEntrys()) {
			dbGalaxyEntries.put(getList4Id(galaxieEntry), galaxieEntry);
		}
        
		for (List<String> row : cells) {
			
			// create a new galaxyEntry
			GalaxieEntry galaxieEntry = new GalaxieEntry();
			for (String cellName : cellsPosition.keySet()) {
				int position = cellsPosition.get(cellName).intValue();
				if (row.size() > position) {
					String cellValue = row.get(position);
					galaxieMappingService.setAttrFromCell(galaxieEntry, cellName, cellValue);
				} else {
					logger.debug("can't get " + cellName + " for this row in excel file ...");
				}
			}
			
			// Récupération d'un GalaxieEntry à chaque fois trop gourmand, même avec l'index ...
			//TypedQuery<GalaxieEntry> query = GalaxieEntry.findGalaxieEntrysByNumEmploiAndNumCandidat(galaxieEntry.getNumEmploi(), galaxieEntry.getNumCandidat(), null, null);
			GalaxieEntry dbGalaxyEntrie = dbGalaxyEntries.get(getList4Id(galaxieEntry));
			
			if (dbGalaxyEntrie == null) {
				galaxieEntry.persist();
				dbGalaxyEntries.put(getList4Id(galaxieEntry), galaxieEntry);
			} else {
				// This GalaxyEntry exists already, we merge it if needed
				if(!fieldsEquals(dbGalaxyEntrie, galaxieEntry)) {
					dbGalaxyEntrie.setCivilite(galaxieEntry.getCivilite());
					dbGalaxyEntrie.setNom(galaxieEntry.getNom());
					dbGalaxyEntrie.setPrenom(galaxieEntry.getPrenom());
					// si email différent et si le candidat n'a pas activé son compte - on réinitialise le compte == on le supprime et on le récréé avec cette nvelle adresse mail
					if(!dbGalaxyEntrie.getEmail().equals(galaxieEntry.getEmail())) {
						try {
							User user = User.findUsersByEmailAddress(dbGalaxyEntrie.getEmail()).getSingleResult();
							if(user.getActivationDate() == null && !user.isCandidatActif()) {
								logger.info("Le canndidat " + dbGalaxyEntrie.getNumCandidat() + " a changé d'email alors qu'il n'avait pas encore activé son compte - on relance la procédure de création de son compte/candidature.");
								user.remove();
								galaxieEntry.persist();
								dbGalaxyEntries.put(getList4Id(galaxieEntry), galaxieEntry);
								continue;
							}
							dbGalaxyEntrie.setEmail(galaxieEntry.getEmail());
						} catch(Exception e) {
							logger.warn("Pb avec le candidat " + dbGalaxyEntrie.getNumCandidat() + " qui a changé d'email ...", e);
						}
					}
					dbGalaxyEntrie.setLocalisation(galaxieEntry.getLocalisation());
					dbGalaxyEntrie.setProfil(galaxieEntry.getProfil());
					dbGalaxyEntrie.setEtatDossier(galaxieEntry.getEtatDossier());
					dbGalaxyEntrie.merge();
				}
			}		
		}
		
		chrono.stop();
		logger.info("Le traitement du fichier Excel Galaxie a été effectué en " + chrono.getTotalTimeMillis()/1000.0 + " sec.");

	}
	
	private boolean fieldsEquals(GalaxieEntry dbGalaxyEntrie,
			GalaxieEntry galaxieEntry) {
		return dbGalaxyEntrie.getCivilite().equals(galaxieEntry.getCivilite())
				&& dbGalaxyEntrie.getNom().equals(galaxieEntry.getNom())
				&& dbGalaxyEntrie.getPrenom().equals(galaxieEntry.getPrenom())
				&& dbGalaxyEntrie.getEmail().equals(galaxieEntry.getEmail())
				&& dbGalaxyEntrie.getLocalisation().equals(galaxieEntry.getLocalisation())
				&& dbGalaxyEntrie.getProfil().equals(galaxieEntry.getProfil())
				&& dbGalaxyEntrie.getEtatDossier().equals(galaxieEntry.getEtatDossier())
				;
	}
	
	private List<String> getList4Id(GalaxieEntry galaxieEntry) {
		return Arrays.asList(new String [] {galaxieEntry.getNumEmploi(), galaxieEntry.getNumCandidat()});
	}
	
	public Map<String, String> getCells4GalaxieEntry(GalaxieExcel galaxieExcel, GalaxieEntry galaxieEntry) throws SQLException {
		
		Map<String, String> cellsMap = null;
		List<List<String>> cells = excelParser.getCells(galaxieExcel.getBigFile().getBinaryFile().getBinaryStream());

		List<String> cellsHead = cells.remove(0);
		
		// dans le doute EsupDematEC considère la dernière ligne comme la plus à jour
		Collections.reverse(cells);
		
		for (List<String> row : cells) {		
			// create a new galaxyEntry
			GalaxieEntry ge = new GalaxieEntry();
			int position = 0;
			for (String cellName : cellsHead) {
				if(position<row.size()) {
					String cellValue = row.get(position);
					galaxieMappingService.setAttrFromCell(ge, cellName, cellValue);
				}
				position++;
			}
			
			if(getList4Id(galaxieEntry).equals(getList4Id(ge))) {		
				cellsMap = new HashMap<String, String>();
				position = 0;
				for (String cellName : cellsHead) {
					if(position<row.size()) {
						String cellValue = row.get(position);
						cellsMap.put(cellName, cellValue);
					}
					position++;
				}
				break;
			}
		}

		return cellsMap;
		
	}

}
