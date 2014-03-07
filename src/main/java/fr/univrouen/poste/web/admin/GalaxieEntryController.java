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

import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
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

import fr.univrouen.poste.domain.AppliConfig;
import fr.univrouen.poste.domain.GalaxieEntry;
import fr.univrouen.poste.domain.PosteAPourvoir;
import fr.univrouen.poste.domain.PosteCandidature;
import fr.univrouen.poste.domain.User;
import fr.univrouen.poste.services.CreateUserService;
import fr.univrouen.poste.services.LogService;
import fr.univrouen.poste.web.UserRegistrationForm;

@RequestMapping("/admin/galaxieentrys")
@Controller
@RooWebScaffold(path = "admin/galaxieentrys", formBackingObject = GalaxieEntry.class)
public class GalaxieEntryController {
	
	@Autowired 
	private CreateUserService createUserService;
	
	@Autowired 
    private LogService logService;
	
    @RequestMapping(produces = "text/html")
    public String list(@RequestParam(value = "page", required = false) Integer page, @RequestParam(value = "size", required = false) Integer size, @RequestParam(value = "sortFieldName", required = false) String sortFieldName, @RequestParam(value = "sortOrder", required = false) String sortOrder, Model uiModel) {
    	if(sortFieldName==null)
        	sortFieldName = "numEmploi,numCandidat";
        if (page != null || size != null) {
            int sizeNo = size == null ? 10 : size.intValue();
            final int firstResult = page == null ? 0 : (page.intValue() - 1) * sizeNo;
            uiModel.addAttribute("galaxieentrys", GalaxieEntry.findGalaxieEntryEntries(firstResult, sizeNo, sortFieldName, sortOrder));
            float nrOfPages = (float) GalaxieEntry.countGalaxieEntrys() / sizeNo;
            uiModel.addAttribute("maxPages", (int) ((nrOfPages > (int) nrOfPages || nrOfPages == 0.0) ? nrOfPages + 1 : nrOfPages));
        } else {
            uiModel.addAttribute("galaxieentrys", GalaxieEntry.findAllGalaxieEntrys(sortFieldName, sortOrder));
        }
        
        Map<String, String> unknowCandidats = new HashMap<String, String>();
        Map<String, String> unknowPostes = new HashMap<String, String>();
        Map<List<String>, String> unknowCandidatures = new HashMap<List<String>, String>();
        
        List<GalaxieEntry> galaxieEntrysWithCandidatNull = GalaxieEntry.findAllGalaxieEntrysWithCandidatNull();
        for(GalaxieEntry  galaxieEntry : galaxieEntrysWithCandidatNull) {
        	unknowCandidats.put(galaxieEntry.getNumCandidat(), "dummy");
       		List<String> candidatureKey = new Vector<String>();
       		candidatureKey.add(galaxieEntry.getNumEmploi());
       		candidatureKey.add(galaxieEntry.getNumCandidat());
       		candidatureKey.add(galaxieEntry.getId().toString());
       		unknowCandidatures.put(candidatureKey, "dummy");
        }
        
        List<GalaxieEntry> galaxieEntrysWithPosteNull = GalaxieEntry.findAllGalaxieEntrysWithPosteNull();
        for(GalaxieEntry  galaxieEntry : galaxieEntrysWithPosteNull) {
        	unknowCandidats.put(galaxieEntry.getNumCandidat(), "dummy");
       		List<String> candidatureKey = new Vector<String>();
       		candidatureKey.add(galaxieEntry.getNumEmploi());
       		candidatureKey.add(galaxieEntry.getNumCandidat());
       		candidatureKey.add(galaxieEntry.getId().toString());
       		unknowCandidatures.put(candidatureKey, "dummy");
        }
        
        uiModel.addAttribute("unknowCandidats", unknowCandidats.keySet());
        uiModel.addAttribute("unknowPostes", unknowPostes.keySet());
        uiModel.addAttribute("unknowCandidatures", unknowCandidatures.keySet());
        
        return "admin/galaxieentrys/list";
    }
    
    @RequestMapping("/generatecandidatspostes")
    public String createCandidatPosteUrl(Model uiModel) {
    	
    	List<GalaxieEntry> galaxieEntrys = GalaxieEntry.findAllGalaxieEntrys();
        for(GalaxieEntry  galaxieEntry : galaxieEntrys) {			
        	if(galaxieEntry.getCandidat() == null) {
        		User candidat = null;
        		TypedQuery<User> query = User.findUsersByNumCandidat(galaxieEntry.getNumCandidat(), null, null);
        		if(query.getResultList().isEmpty()) {
        			if(galaxieEntry.getEmail() == null || galaxieEntry.getEmail().isEmpty()) {
        				String message = "Le candidat " + galaxieEntry.getNumCandidat() + " n'a pas de mail de renseigné";
        				logService.logImportGalaxie(message, LogService.IMPORT_FAILED);
        			} else {
	        			List<User> usersSameEmail = User.findUsersByEmailAddress(galaxieEntry.getEmail(), null, null).getResultList();
	        			if(!usersSameEmail.isEmpty()) {
	        				String message = "Le candidat " + galaxieEntry.getNumCandidat() + " a le même mail que le(s) utilisateur(s)";
	        				for(User u : usersSameEmail) 
	        					message = message + " " + u.getEmailAddress() + "(n° candidat : " + u.getNumCandidat() + ")";
	        				logService.logImportGalaxie(message, LogService.IMPORT_FAILED);
	        			} else {
			        		// new User 
			        		UserRegistrationForm userRegistration = new UserRegistrationForm();
			        		userRegistration.setEmailAddress(galaxieEntry.getEmail());
							candidat = createUserService.createCandidatUser(userRegistration);
								
							if(candidat != null) {
				        		// Candidat
				        		candidat.setNumCandidat(galaxieEntry.getNumCandidat());
				        		candidat.setCivilite(galaxieEntry.getCivilite());
				        		candidat.setEmailAddress(galaxieEntry.getEmail());
				        		candidat.setNom(galaxieEntry.getNom());
				        		candidat.setPrenom(galaxieEntry.getPrenom());
				        		candidat.persist();    
				        		
				        		logService.logImportGalaxie("Candidat " + candidat.getNumCandidat() + " créé.", LogService.IMPORT_SUCCESS);
			        		} else {
			        			String message = "Le mail n'a pu être envoyé pour le candidat " + galaxieEntry.getNumCandidat() + " [" + galaxieEntry.getEmail() + "]";
			        			logService.logImportGalaxie(message, LogService.IMPORT_FAILED);
							}		        			
		        		}
        			}
        			
        		} else {
        			candidat = query.getSingleResult();
        		}
        		 			
            	// re-attach galaxieEntry : galaxieEntry can be detached here because of a transaction rollback
            	// @see CreateUserService.createCandidatUser/createMembreUser
    			galaxieEntry.merge();
    			
        		if(candidat != null) {
	        		galaxieEntry.setCandidat(candidat);
	        		galaxieEntry.persist();
        		}
        	}
        	
           	if(galaxieEntry.getCandidat() != null && galaxieEntry.getPoste() == null) {
           		PosteAPourvoir poste = null;
           		TypedQuery<PosteAPourvoir> query =  PosteAPourvoir.findPosteAPourvoirsByNumEmploi(galaxieEntry.getNumEmploi(), null, null);
           		if(query.getResultList().isEmpty()) {
           			
           			// new Poste
           			poste = new PosteAPourvoir();
           			poste.setLocalisation(galaxieEntry.getLocalisation());
           			poste.setNumEmploi(galaxieEntry.getNumEmploi());
           			poste.setProfil(galaxieEntry.getProfil());
           			poste.persist();
           			
           			logService.logImportGalaxie("Poste " + poste.getNumEmploi() + " créé.", LogService.IMPORT_SUCCESS);
           			
           		} else {
           			poste = query.getSingleResult();
        		}
        		galaxieEntry.setPoste(poste);
        		galaxieEntry.persist(); 
           	}
           	
           	if(galaxieEntry.getCandidat() != null && galaxieEntry.getPoste() != null && galaxieEntry.getCandidature() == null) {
           		
           		// new Candidature
           		PosteCandidature candidature = new PosteCandidature();
           		candidature.setCandidat(galaxieEntry.getCandidat());
           		candidature.setPoste(galaxieEntry.getPoste());
           		
                Calendar cal = Calendar.getInstance();
                Date currentTime = cal.getTime();
                candidature.setCreation(currentTime);
                
                Boolean recevable = AppliConfig.getCacheCandidatureRecevableDefault();
                candidature.setRecevable(recevable);
                
           		candidature.persist();
           		galaxieEntry.setCandidature(candidature);
           		galaxieEntry.persist();    
           		
       			logService.logImportGalaxie("Candidature " + candidature.getPoste().getNumEmploi() + "/" + candidature.getCandidat().getNumCandidat() + " créé.", LogService.IMPORT_SUCCESS);
           		
           	}
           	
        }
        

        return "redirect:/admin/logimportgalaxies?sortFieldName=actionDate&sortOrder=desc&page=1&size=40";
    }
    
    
}
