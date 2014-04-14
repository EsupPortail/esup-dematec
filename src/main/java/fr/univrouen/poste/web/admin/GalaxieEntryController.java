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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Vector;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.roo.addon.web.mvc.controller.scaffold.RooWebScaffold;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import fr.univrouen.poste.domain.GalaxieEntry;
import fr.univrouen.poste.domain.User;
import fr.univrouen.poste.services.GalaxieEntryService;
import fr.univrouen.poste.services.LogService;

@RequestMapping("/admin/galaxieentrys")
@Controller
@RooWebScaffold(path = "admin/galaxieentrys", formBackingObject = GalaxieEntry.class)
public class GalaxieEntryController {
	
	private final Logger logger = Logger.getLogger(getClass());
	
	@Autowired 
	GalaxieEntryService galaxieEntryService;
	
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
    public String generateCandidatsPostes (Model uiModel) {
    	
    	List<GalaxieEntry> galaxieEntrys = GalaxieEntry.findGalaxieEntrysByCandidatIsNull().getResultList();
        for(GalaxieEntry  galaxieEntry : galaxieEntrys) {	
        	try{
        		galaxieEntryService.generateCandidat(galaxieEntry);
        	} catch(Exception e) {
				logService.logImportGalaxie(e.getMessage(), LogService.IMPORT_FAILED);
				logger.error("Import of " + galaxieEntry + " failed", e);
        	}
        }      
        
    	galaxieEntrys = GalaxieEntry.findGalaxieEntrysByPosteIsNull().getResultList();
        for(GalaxieEntry  galaxieEntry : galaxieEntrys) {	
        	try{
        		galaxieEntryService.generatePoste(galaxieEntry);
        	} catch(Exception e) {
				logService.logImportGalaxie(e.getMessage(), LogService.IMPORT_FAILED);
				logger.error("Import of " + galaxieEntry + " failed", e);
        	}
        }  
        
    	galaxieEntrys = GalaxieEntry.findGalaxieEntrysByCandidatureIsNull().getResultList();
    	Set<User> candidatureUsers = new HashSet<User>();
        for(GalaxieEntry  galaxieEntry : galaxieEntrys) {	
        	if(galaxieEntry.getCandidat() != null) {
        		candidatureUsers.add(galaxieEntry.getCandidat());
        	}
        }
        for(User user: candidatureUsers) {
        	try{
        		galaxieEntryService.generateCandidatures(user);
        	} catch(Exception e) {
				logService.logImportGalaxie(e.getMessage(), LogService.IMPORT_FAILED);
				logger.error("Import of " + user.getEmailAddress() + " candidatures failed", e);
        	}
        }  

        return "redirect:/admin/logimportgalaxies?sortFieldName=actionDate&sortOrder=desc&page=1&size=40";
    }
    
}
