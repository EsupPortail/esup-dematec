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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.persistence.TypedQuery;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import fr.univrouen.poste.domain.GalaxieEntry;
import fr.univrouen.poste.domain.GalaxieExcel;

@Service
public class GalaxieExcelParser {

	private final Logger logger = Logger.getLogger(getClass());

	@Autowired
	ExcelParser excelParser;

	@Autowired	
	GalaxieMappingService galaxieMappingService;

	public void process(GalaxieExcel galaxieExcel) throws SQLException {

		List<List<String>> cells = excelParser.getCells(galaxieExcel.getBigFile().getBinaryFile().getBinaryStream());

		Map<String, Long> cellsPosition = new HashMap<String, Long>();

		int p = 0;
		List<String> cellsHead = cells.remove(0);
		for (String cellName : cellsHead) {
			cellsPosition.put(cellName, new Long(p++));
		}
		galaxieMappingService.checkCellsHead(cellsPosition);

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
			
			TypedQuery<GalaxieEntry> query = GalaxieEntry.findGalaxieEntrysByNumEmploiAndNumCandidat(galaxieEntry.getNumEmploi(), galaxieEntry.getNumCandidat(), null, null);
			if (query.getResultList().isEmpty()) {
				galaxieEntry.persist();
			} else {
				// This GalaxyEntry exists already, we merge it ...
				GalaxieEntry galaxyEntryOld = query.getSingleResult();
				galaxyEntryOld.setNumEmploi(galaxieEntry.getNumEmploi());
				galaxyEntryOld.setNumCandidat(galaxieEntry.getNumCandidat());
				galaxyEntryOld.setCivilite(galaxieEntry.getCivilite());
				galaxyEntryOld.setNom(galaxieEntry.getNom());
				galaxyEntryOld.setPrenom(galaxieEntry.getPrenom());
				galaxyEntryOld.setEmail(galaxieEntry.getEmail());
				galaxyEntryOld.setLocalisation(galaxieEntry.getLocalisation());
				galaxyEntryOld.setProfil(galaxieEntry.getProfil());
				galaxyEntryOld.merge();
			}
		}

	}

}
