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

import fr.univrouen.poste.dao.GalaxieMappingDao;
import fr.univrouen.poste.domain.GalaxieEntry;
import fr.univrouen.poste.domain.GalaxieMapping;
import jakarta.annotation.Resource;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
public class GalaxieMappingService {

	final Logger logger = LoggerFactory.getLogger(getClass());

	@Resource
	GalaxieMappingDao galaxieMappingDao;

	GalaxieMapping getGalaxieMapping() {
		return galaxieMappingDao.findAllGalaxieMappings().get(0);
	}

	public void setAttrFromCell(GalaxieEntry galaxieEntry, String cellName, String cellValue) {

		GalaxieMapping galaxieMapping = getGalaxieMapping();

		String id_numemploi = galaxieMapping.getId_numemploi();
		String id_numCandidat = galaxieMapping.getId_numCandidat();
		String id_email = galaxieMapping.getId_email();
		
		String id_civilite = galaxieMapping.getId_civilite();
		String id_nom = galaxieMapping.getId_nom();
		String id_prenom = galaxieMapping.getId_prenom();
		String id_localisation = galaxieMapping.getId_localisation();
		String id_profil = galaxieMapping.getId_profil();
		String id_etat_dossier = galaxieMapping.getId_etat_dossier();
		
		
        if (id_numemploi.equals(cellName)) galaxieEntry.setNumEmploi(cellValue.trim());
        if (id_numCandidat.equals(cellName)) galaxieEntry.setNumCandidat(cellValue.trim());
        if (id_civilite.equals(cellName)) galaxieEntry.setCivilite(cellValue.trim());
        if (id_nom.equals(cellName)) galaxieEntry.setNom(cellValue.trim());
        if (id_prenom.equals(cellName)) galaxieEntry.setPrenom(cellValue.trim());
        if (id_email.equals(cellName)) galaxieEntry.setEmail(cellValue.trim());
        if (id_localisation.equals(cellName)) galaxieEntry.setLocalisation(cellValue.trim());
        if (id_profil.equals(cellName)) galaxieEntry.setProfil(cellValue.trim());
        if (id_etat_dossier.equals(cellName)) galaxieEntry.setEtatDossier(cellValue.trim());
	}

	public void checkCellsHead(Map<String, Long> cellsPosition) {

		GalaxieMapping galaxieMapping = getGalaxieMapping();

		String id_numemploi = galaxieMapping.getId_numemploi();
		String id_numCandidat = galaxieMapping.getId_numCandidat();
		String id_email = galaxieMapping.getId_email();

		List<String> columnsNotFound = new ArrayList<String>();
		String[] columnNamesRequired = {id_numemploi, id_numCandidat, id_email};
		
		for(String columnName: columnNamesRequired) {
			if(!cellsPosition.containsKey(columnName)) {
				columnsNotFound.add(columnName);
			}
		}
		
		if(!columnsNotFound.isEmpty()) {
			String errorMsg = "La (les) colonne(s) " + StringUtils.join(columnsNotFound, ", ") + 
					" est (sont) manquante(s) dans le fichier Excel fourni. Les colonnes " + StringUtils.join(columnNamesRequired, ", ") + 
					" sont obligatoires ; ces libellés étant à configurer pour chaque campagne via l'IHM - menu 'Mapping Galaxie' " +
					" , ce en fonction de la structure des fichiers Excel Galaxie.";
			logger.error(errorMsg);
			throw new RuntimeException(errorMsg);
		}
	}
	
}
