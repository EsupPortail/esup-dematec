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
package fr.univrouen.poste.web.admin;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Vector;

import javax.persistence.TypedQuery;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.roo.addon.web.mvc.controller.scaffold.RooWebScaffold;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import fr.univrouen.poste.domain.CommissionEntry;
import fr.univrouen.poste.domain.PosteAPourvoir;
import fr.univrouen.poste.domain.User;
import fr.univrouen.poste.exceptions.EsupMailException;
import fr.univrouen.poste.services.CreateUserService;
import fr.univrouen.poste.services.LogService;
import fr.univrouen.poste.web.UserRegistrationForm;

@RequestMapping("/admin/commissionentrys")
@Controller
@RooWebScaffold(path = "admin/commissionentrys", formBackingObject = CommissionEntry.class)
public class CommissionEntryController {
	
	@Autowired 
	private CreateUserService createUserService;
	
	@Autowired 
    private LogService logService;
	
    @RequestMapping(produces = "text/html")
    public String list(@RequestParam(value = "page", required = false) Integer page, @RequestParam(value = "size", required = false) Integer size, Model uiModel) {
        if (page != null || size != null) {
            int sizeNo = size == null ? 10 : size.intValue();
            final int firstResult = page == null ? 0 : (page.intValue() - 1) * sizeNo;
            uiModel.addAttribute("commissionentrys", CommissionEntry.findCommissionEntryEntries(firstResult, sizeNo));
            float nrOfPages = (float) CommissionEntry.countCommissionEntrys() / sizeNo;
            uiModel.addAttribute("maxPages", (int) ((nrOfPages > (int) nrOfPages || nrOfPages == 0.0) ? nrOfPages + 1 : nrOfPages));
        } else {
            uiModel.addAttribute("commissionentrys", CommissionEntry.findAllCommissionEntrys());
        }

        Map<String, String> unknowMembres = new HashMap<String, String>();
        Map<String, String> unknowPostes = new HashMap<String, String>();
        Map<List<String>, String> unknowCommission = new HashMap<List<String>, String>();
        
        List<CommissionEntry> commissionEntrys = CommissionEntry.findAllCommissionEntrys();
        for(CommissionEntry  commissionEntry : commissionEntrys) {
        	if(commissionEntry.getMembre()==null)
        		unknowMembres.put(commissionEntry.getEmail(), "dummy");
           	if(commissionEntry.getPoste() == null)
        		unknowPostes.put(commissionEntry.getNumPoste(), "dummy");
           	if(commissionEntry.getMembre() == null || commissionEntry.getPoste() == null) {
           		List<String> commissionKey = new Vector<String>();
           		commissionKey.add(commissionEntry.getEmail());
           		commissionKey.add(commissionEntry.getNumPoste());
           		unknowCommission.put(commissionKey, "dummy");
           	}
        }
        
        uiModel.addAttribute("unknowMembres", unknowMembres.keySet());
        uiModel.addAttribute("unknowPostes", unknowPostes.keySet());
        uiModel.addAttribute("unknowCommission", unknowCommission.keySet());
        
        return "admin/commissionentrys/list";
    }
    
    
    @RequestMapping("/generatecommissions")
    public String createCommission() {
 
    	List<CommissionEntry> commissionEntrys = CommissionEntry.findAllCommissionEntrys();
        for(CommissionEntry  commissionEntry : commissionEntrys) {     	
        	if(commissionEntry.getMembre()==null) {
        		User membre = null;
        		TypedQuery<User> query = User.findUsersByEmailAddress(commissionEntry.getEmail());
        		if(query.getResultList().isEmpty()) {
        			try {
	        			// new User 
	        			UserRegistrationForm userRegistration = new UserRegistrationForm();
	        			userRegistration.setEmailAddress(commissionEntry.getEmail());
	        			membre = createUserService.createMembreUser(userRegistration);
	        			
	        			// Membre
	        			membre.setNom(commissionEntry.getNom());
	        			membre.setPrenom(commissionEntry.getPrenom());
	        			membre.persist();     
	        			
	        			logService.logImportCommission("Membre " + membre.getEmailAddress() + " créé.", LogService.IMPORT_SUCCESS);
        			} catch (EsupMailException e) {
        				String message = "Le mail n'a pu être envoyé pour le membre " + commissionEntry.getEmail();
        				logService.logImportCommission(message, LogService.IMPORT_FAILED);
					}
        			
        		} else {
        			membre = query.getSingleResult();
        		}
        		if(membre.getIsCandidat()) {
        			logService.logImportCommission("L'utilisateur " + membre.getEmailAddress() + " est déjà candidat, il ne peut également être membre dans l'application (avec ce même email).", LogService.IMPORT_FAILED);
        		} else {
        			commissionEntry.setMembre(membre);
        		}
        		commissionEntry.persist();       		
        	}
        	
           	if(commissionEntry.getMembre() != null && commissionEntry.getPoste() == null) {
           		PosteAPourvoir poste = null;
           		TypedQuery<PosteAPourvoir> query =  PosteAPourvoir.findPosteAPourvoirsByNumEmploi(commissionEntry.getNumPoste());
           		if(query.getResultList().isEmpty()) {
           			
           			// new Poste
           			poste = new PosteAPourvoir();
           			poste.setNumEmploi(commissionEntry.getNumPoste());
           			poste.persist();
           			
           			logService.logImportCommission("Poste " + poste.getNumEmploi() + " créé.", LogService.IMPORT_SUCCESS);
           			
           		} else {
           			poste = query.getSingleResult();
        		}
           		commissionEntry.setPoste(poste);
           		commissionEntry.persist(); 
           	}
           	
           	if(commissionEntry.getMembre() != null && commissionEntry.getPoste() != null) {
	           	if(commissionEntry.getPoste().getMembres() == null || !commissionEntry.getPoste().getMembres().contains(commissionEntry.getMembre())) {           		
	           		PosteAPourvoir poste = commissionEntry.getPoste();
	           		if(poste.getMembres() == null) 
	           			poste.setMembres(new HashSet<User>());       			
	           			
	           		poste.getMembres().add(commissionEntry.getMembre());
	           		poste.persist();  
	           		logService.logImportCommission("Membre " + commissionEntry.getMembre().getEmailAddress() + " ajouté comme membre pour le poste " + poste.getNumEmploi() + ".", LogService.IMPORT_SUCCESS);   		
	           	}
           	}
        }
        

        return "redirect:/admin/logimportcommissions";
    }
    
    
 
    
}
