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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import fr.univrouen.poste.domain.CommissionEntry;
import fr.univrouen.poste.domain.CommissionExcel;

@Service
public class CommissionExcelParser {

	private final Logger logger = Logger.getLogger(getClass());

	@Autowired
	ExcelParser excelParser;
	
	@Autowired	
	CommissionMappingService commissionMappingService;

	public void process(CommissionExcel commissionExcel) throws SQLException {

		List<List<String>> cells = excelParser.getCells(commissionExcel.getBigFile().getBinaryFile().getBinaryStream());

		Map<String, Long> cellsPosition = new HashMap<String, Long>();

		int p = 0;
		List<String> cellsHead = cells.remove(0);
		for (String cellName : cellsHead) {
			cellsPosition.put(cellName, new Long(p++));
		}

		Map<List<String>, CommissionEntry>  dbcommissionEntries = new HashMap<List<String>, CommissionEntry>();
		for(CommissionEntry commissionEntry : CommissionEntry.findAllCommissionEntrys()) {
			dbcommissionEntries.put(getList4Id(commissionEntry), commissionEntry);
		}
        
		
		for (List<String> row : cells) {

			// create a new commissionEntry
			CommissionEntry commissionEntry = new CommissionEntry();
			for (String cellName : cellsPosition.keySet()) {
				int position = cellsPosition.get(cellName).intValue();
				if (row.size() > position) {
					String cellValue = row.get(position);
					commissionMappingService.setAttrFromCell(commissionEntry, cellName, cellValue);
				} else {
					logger.debug("can't get " + cellName + " for this row in excel file ...");
				}
			}

			if(commissionEntry.getNumPoste() != null && !commissionEntry.getNumPoste().isEmpty()
					&& commissionEntry.getEmail() != null && !commissionEntry.getEmail().isEmpty()) {
				
				// Récupération d'un CommissionEntry à chaque fois trop gourmand, même avec l'index ...
				//TypedQuery<CommissionEntry> query = CommissionEntry.findCommissionEntrysByNumPosteAndEmail(commissionEntry.getNumPoste(), commissionEntry.getEmail(), null, null);
				CommissionEntry dbCommissionEntry = dbcommissionEntries.get(getList4Id(commissionEntry));
				
				if (dbCommissionEntry == null) {
					commissionEntry.persist();
				} else {
					// This GalaxyEntry exists already, we merge it if needed
					if(!fieldsEquals(dbCommissionEntry, commissionEntry)) {
						dbCommissionEntry.setNom(commissionEntry.getNom());
						dbCommissionEntry.setPrenom(commissionEntry.getPrenom());
						dbCommissionEntry.merge();
					}
				}
			}

		}

	}
	
	private boolean fieldsEquals(CommissionEntry dbCommissionEntry,
			CommissionEntry commissionEntry) {
		return dbCommissionEntry.getNom().equals(commissionEntry.getNom()) 
				&& dbCommissionEntry.getPrenom().equals(commissionEntry.getPrenom());
	}

	private List<String> getList4Id(CommissionEntry commissionEntry) {
		return Arrays.asList(new String [] {commissionEntry.getNumPoste(), commissionEntry.getEmail()});
	}


}
