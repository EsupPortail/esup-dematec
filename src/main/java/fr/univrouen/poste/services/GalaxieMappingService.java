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

import fr.univrouen.poste.domain.GalaxieEntry;

public class GalaxieMappingService {


    private String id_numemploi = "dummy";
    private String id_numCandidat = "dummy";
    private String id_civilite = "dummy";
    private String id_nom = "dummy";
    private String id_prenom = "dummy";
    private String id_email = "dummy";
    private String id_localisation = "dummy";
    private String id_profil = "dummy";

	public void setId_numemploi(String id_numemploi) {
    	this.id_numemploi = id_numemploi;
    }

	public void setId_numCandidat(String id_numCandidat) {
    	this.id_numCandidat = id_numCandidat;
    }

	public void setId_civilite(String id_civilite) {
    	this.id_civilite = id_civilite;
    }

	public void setId_nom(String id_nom) {
    	this.id_nom = id_nom;
    }

	public void setId_prenom(String id_prenom) {
    	this.id_prenom = id_prenom;
    }

	public void setId_email(String id_email) {
    	this.id_email = id_email;
    }

	public void setId_localisation(String id_localisation) {
    	this.id_localisation = id_localisation;
    }

	public void setId_profil(String id_profil) {
    	this.id_profil = id_profil;
    }

	public void setAttrFromCell(GalaxieEntry galaxieEntry, String cellName, String cellValue) {
        if (id_numemploi.equals(cellName)) galaxieEntry.setNumEmploi(cellValue);
        if (id_numCandidat.equals(cellName)) galaxieEntry.setNumCandidat(cellValue);
        if (id_civilite.equals(cellName)) galaxieEntry.setCivilite(cellValue);
        if (id_nom.equals(cellName)) galaxieEntry.setNom(cellValue);
        if (id_prenom.equals(cellName)) galaxieEntry.setPrenom(cellValue);
        if (id_email.equals(cellName)) galaxieEntry.setEmail(cellValue);
        if (id_localisation.equals(cellName)) galaxieEntry.setLocalisation(cellValue);
        if (id_profil.equals(cellName)) galaxieEntry.setProfil(cellValue);
	}
	
}
