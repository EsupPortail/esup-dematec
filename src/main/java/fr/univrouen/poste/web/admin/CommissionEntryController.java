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

import fr.univrouen.poste.dao.CommissionEntryDao;
import fr.univrouen.poste.dao.PosteAPourvoirDao;
import fr.univrouen.poste.dao.UserDao;
import fr.univrouen.poste.domain.CommissionEntry;
import fr.univrouen.poste.domain.User;
import fr.univrouen.poste.services.CommissionEntryService;
import fr.univrouen.poste.services.LogService;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.web.util.UriUtils;
import org.springframework.web.util.WebUtils;

import java.util.*;

@RequestMapping("/admin/commissionentrys")
@Controller
public class CommissionEntryController {
	
	final Logger logger = LoggerFactory.getLogger(getClass());
	
	@Resource
	CommissionEntryService commissionEntryService;
	
	@Resource
    LogService logService;

	@Resource
	CommissionEntryDao commissionEntryDao;

	@Resource
	UserDao userDao;

	@Resource
	PosteAPourvoirDao posteAPourvoirDao;
	
    @RequestMapping(produces = "text/html")
    public String list(@PageableDefault(size = 10) Pageable pageable, @RequestParam(value = "sortFieldName", required = false) String sortFieldName, @RequestParam(value = "sortOrder", required = false) String sortOrder, Model uiModel) {
        if(sortFieldName==null)
            sortFieldName = "numPoste,email";
        if (pageable.isPaged()) {
            Page<CommissionEntry> page = commissionEntryDao.findCommissionEntryEntries(pageable, sortFieldName, sortOrder);
            uiModel.addAttribute("commissionentrys", page);
        } else {
            uiModel.addAttribute("commissionentrys", commissionEntryDao.findAllCommissionEntrys(sortFieldName, sortOrder));
        }

        Map<String, String> unknowMembres = new HashMap<String, String>();
        Map<String, String> unknowPostes = new HashMap<String, String>();
        Map<List<String>, String> unknowCommission = new HashMap<List<String>, String>();
        
        List<CommissionEntry> commissionEntrysWithMembreNull = commissionEntryDao.findCommissionEntrysByMembreIsNull();
        for(CommissionEntry  commissionEntry : commissionEntrysWithMembreNull) {
        	unknowMembres.put(commissionEntry.getEmail(), "dummy");
       		List<String> candidatureKey = new Vector<String>();
       		candidatureKey.add(commissionEntry.getEmail());
       		candidatureKey.add(commissionEntry.getNumPoste());
       		candidatureKey.add(commissionEntry.getId().toString());
       		unknowCommission.put(candidatureKey, "dummy");
        }
        
        List<CommissionEntry> commissionEntrysWithPosteNull = commissionEntryDao.findCommissionEntrysByPosteIsNull();
        for(CommissionEntry  commissionEntry : commissionEntrysWithPosteNull) {
        	unknowPostes.put(commissionEntry.getNumPoste(), "dummy");
       		List<String> candidatureKey = new Vector<String>();
       		candidatureKey.add(commissionEntry.getEmail());
       		candidatureKey.add(commissionEntry.getNumPoste());
       		candidatureKey.add(commissionEntry.getId().toString());
       		unknowCommission.put(candidatureKey, "dummy");
        }
        
        uiModel.addAttribute("unknowMembres", unknowMembres.keySet());
        uiModel.addAttribute("unknowPostes", unknowPostes.keySet());
        uiModel.addAttribute("unknowCommission", unknowCommission.keySet());
        
        return "admin/commissionentrys/list";
    }
    
    
    @RequestMapping("/generatecommissions")
    public String generateCommissions() {
 
    	List<CommissionEntry> commissionEntrys = commissionEntryDao.findCommissionEntrysByMembreIsNull();
        for(CommissionEntry  commissionEntry : commissionEntrys) {
        	String commissionEntryStr = commissionEntry.toString();
        	try{
        		commissionEntryService.generateMembre(commissionEntry);
        	} catch(Exception e) {
        		logService.logImportCommission(e.getMessage(), LogService.IMPORT_FAILED);
				logger.error("Import of " + commissionEntryStr + " failed", e);
        	}
        }
        
    	commissionEntrys = commissionEntryDao.findCommissionEntrysByPosteIsNull();
        for(CommissionEntry  commissionEntry : commissionEntrys) {     	
        	String commissionEntryStr = commissionEntry.toString();
        	try{
        		commissionEntryService.generatePoste(commissionEntry);
        	} catch(Exception e) {
        		logService.logImportCommission(e.getMessage(), LogService.IMPORT_FAILED);
				logger.error("Import of " + commissionEntryStr + " failed", e);
        	}
        }
        
    	commissionEntrys = commissionEntryDao.findAllCommissionEntrys();
    	Set<User> membres = new HashSet<User>();
        for(CommissionEntry  commissionEntry : commissionEntrys) {  
        	membres.add(commissionEntry.getMembre());
        }
        for(User membre: membres) {
        	if(membre!=null) {
	        	String membreStr = membre.toString();
	        	try{
	        		commissionEntryService.generateCommission(membre);
	        	} catch(Exception e) {
	        		logService.logImportCommission(e.getMessage(), LogService.IMPORT_FAILED);
					logger.error("Import of commission for " + membreStr + " failed", e);
	        	}
        	}
        }
        
        
        return "redirect:/admin/logimportcommissions";
    }
    

	@RequestMapping(method = RequestMethod.POST, produces = "text/html")
    public String create(@Valid CommissionEntry commissionEntry, BindingResult bindingResult, Model uiModel, RedirectAttributes redirectAttributes, HttpServletRequest httpServletRequest) {
        if (bindingResult.hasErrors()) {
            populateEditForm(uiModel, commissionEntry);
            return "admin/commissionentrys/create";
        }
        commissionEntryDao.saveCommissionEntry(commissionEntry);
        return "redirect:/admin/commissionentrys/" + encodeUrlPathSegment(commissionEntry.getId().toString(), httpServletRequest);
    }

	@RequestMapping(params = "form", produces = "text/html")
    public String createForm(Model uiModel) {
        populateEditForm(uiModel, new CommissionEntry());
        return "admin/commissionentrys/create";
    }

	@RequestMapping(method = RequestMethod.GET, value = "/{id}", produces = "text/html")
    public String show(@PathVariable Long id, Model uiModel) {
        uiModel.addAttribute("commissionentry", commissionEntryDao.findCommissionEntry(id));
        uiModel.addAttribute("itemId", id);
        return "admin/commissionentrys/show";
    }

	@RequestMapping(method = RequestMethod.PUT, produces = "text/html")
    public String update(@Valid CommissionEntry commissionEntry, BindingResult bindingResult, Model uiModel, RedirectAttributes redirectAttributes, HttpServletRequest httpServletRequest) {
        if (bindingResult.hasErrors()) {
            populateEditForm(uiModel, commissionEntry);
            return "admin/commissionentrys/update";
        }
        commissionEntryDao.saveCommissionEntry(commissionEntry);
        return "redirect:/admin/commissionentrys/" + encodeUrlPathSegment(commissionEntry.getId().toString(), httpServletRequest);
    }

	@RequestMapping(value = "/{id}", params = "form", produces = "text/html")
    public String updateForm(@PathVariable Long id, Model uiModel) {
        populateEditForm(uiModel, commissionEntryDao.findCommissionEntry(id));
        return "admin/commissionentrys/update";
    }

	@RequestMapping(value = "/{id}", method = RequestMethod.DELETE, produces = "text/html")
    public String delete(@PathVariable Long id, @RequestParam(value = "page", required = false) Integer page, @RequestParam(value = "size", required = false) Integer size, RedirectAttributes redirectAttributes) {
        commissionEntryDao.deleteCommissionEntry(id);
        redirectAttributes.addFlashAttribute("page", (page == null) ? "1" : page.toString());
        redirectAttributes.addFlashAttribute("size", (size == null) ? "10" : size.toString());
        return "redirect:/admin/commissionentrys";
    }

	void populateEditForm(Model uiModel, CommissionEntry commissionEntry) {
        uiModel.addAttribute("commissionEntry", commissionEntry);
        uiModel.addAttribute("posteapourvoirs", posteAPourvoirDao.findAllPosteAPourvoirs());
        uiModel.addAttribute("users", userDao.findAllUsers());
    }

	String encodeUrlPathSegment(String pathSegment, HttpServletRequest httpServletRequest) {
        String enc = httpServletRequest.getCharacterEncoding();
        if (enc == null) {
            enc = WebUtils.DEFAULT_CHARACTER_ENCODING;
        }
        pathSegment = UriUtils.encodePathSegment(pathSegment, enc);
        return pathSegment;
    }
}
