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
import java.util.List;
import java.util.Map;
import java.util.Vector;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.roo.addon.web.mvc.controller.scaffold.RooWebScaffold;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import fr.univrouen.poste.domain.CommissionEntry;
import fr.univrouen.poste.services.CommissionEntryService;

@RequestMapping("/admin/commissionentrys")
@Controller
@RooWebScaffold(path = "admin/commissionentrys", formBackingObject = CommissionEntry.class)
public class CommissionEntryController {

	@Autowired 
	CommissionEntryService commissionEntryService;
	
    @RequestMapping(produces = "text/html")
    public String list(@RequestParam(value = "page", required = false) Integer page, @RequestParam(value = "size", required = false) Integer size, @RequestParam(value = "sortFieldName", required = false) String sortFieldName, @RequestParam(value = "sortOrder", required = false) String sortOrder, Model uiModel) {
        if(sortFieldName==null)
        	sortFieldName = "numPoste,email";
    	if (page != null || size != null) {
            int sizeNo = size == null ? 10 : size.intValue();
            final int firstResult = page == null ? 0 : (page.intValue() - 1) * sizeNo;
            uiModel.addAttribute("commissionentrys", CommissionEntry.findCommissionEntryEntries(firstResult, sizeNo, sortFieldName, sortOrder));
            float nrOfPages = (float) CommissionEntry.countCommissionEntrys() / sizeNo;
            uiModel.addAttribute("maxPages", (int) ((nrOfPages > (int) nrOfPages || nrOfPages == 0.0) ? nrOfPages + 1 : nrOfPages));
        } else {
            uiModel.addAttribute("commissionentrys", CommissionEntry.findAllCommissionEntrys(sortFieldName, sortOrder));
        }

        Map<String, String> unknowMembres = new HashMap<String, String>();
        Map<String, String> unknowPostes = new HashMap<String, String>();
        Map<List<String>, String> unknowCommission = new HashMap<List<String>, String>();
        
        List<CommissionEntry> commissionEntrysWithMembreNull = CommissionEntry.findAllCommissionEntrysWithMembreNull();
        for(CommissionEntry  commissionEntry : commissionEntrysWithMembreNull) {
        	unknowMembres.put(commissionEntry.getEmail(), "dummy");
       		List<String> candidatureKey = new Vector<String>();
       		candidatureKey.add(commissionEntry.getEmail());
       		candidatureKey.add(commissionEntry.getNumPoste());
       		candidatureKey.add(commissionEntry.getId().toString());
       		unknowCommission.put(candidatureKey, "dummy");
        }
        
        List<CommissionEntry> commissionEntrysWithPosteNull = CommissionEntry.findAllCommissionEntrysWithPosteNull();
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
 
    	List<CommissionEntry> commissionEntrys = CommissionEntry.findAllCommissionEntrys();
        for(CommissionEntry  commissionEntry : commissionEntrys) {     	
        	commissionEntryService.generateCommission(commissionEntry);
        }
        
        return "redirect:/admin/logimportcommissions?sortFieldName=actionDate&sortOrder=desc&page=1&size=40";
    }
    
}
