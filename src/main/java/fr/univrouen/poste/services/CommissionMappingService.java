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

import org.springframework.stereotype.Service;

import fr.univrouen.poste.domain.CommissionEntry;

@Service
public class CommissionMappingService {	

    private static final String id_numposte = "Poste";

    private static final String id_email = "Email";

    private static final String id_nom = "Nom";

    private static final String id_prenom = "Prénom";
    
    private static final String id_president= "Président";

	public void setAttrFromCell(CommissionEntry commissionEntry, String cellName, String cellValue) {
        if (id_numposte.equals(cellName)) commissionEntry.setNumPoste(cellValue.trim());
        if (id_nom.equals(cellName)) commissionEntry.setNom(cellValue.trim());
        if (id_prenom.equals(cellName)) commissionEntry.setPrenom(cellValue.trim());
        if (id_email.equals(cellName)) commissionEntry.setEmail(cellValue.trim());
        if (id_president.equals(cellName)) commissionEntry.setPresident("oui".equalsIgnoreCase(cellValue.trim()));
	}
	
}
