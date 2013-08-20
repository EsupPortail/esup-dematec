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
				TypedQuery<CommissionEntry> query = CommissionEntry.findCommissionEntrysByNumPosteAndEmail(commissionEntry.getNumPoste(), commissionEntry.getEmail(), null, null);
				if (query.getResultList().isEmpty()) {
					commissionEntry.persist();
				} else {
					// This GalaxyEntry exists already, we merge it ...
					CommissionEntry commissionEntryOld = query.getSingleResult();
					commissionEntryOld.setNumPoste(commissionEntry.getNumPoste());
					commissionEntryOld.setNom(commissionEntry.getNom());
					commissionEntryOld.setPrenom(commissionEntry.getPrenom());
					commissionEntryOld.setEmail(commissionEntry.getEmail());
	
				}
			}

		}

	}

}
