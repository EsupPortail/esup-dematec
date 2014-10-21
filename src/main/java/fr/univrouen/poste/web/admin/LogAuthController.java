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

import org.springframework.roo.addon.web.mvc.controller.scaffold.RooWebScaffold;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

import fr.univrouen.poste.domain.LogAuth;
import fr.univrouen.poste.web.searchcriteria.LogSearchCriteria;

@RequestMapping("/admin/logauths")
@Controller
@RooWebScaffold(path = "admin/logauths", formBackingObject = LogAuth.class, create=false, update=false)
public class LogAuthController {
	
    @ModelAttribute("command") 
    public LogSearchCriteria getLogSearchCriteria() {
    	return new LogSearchCriteria();
    }
    
    @RequestMapping(params = "find=ByActionEquals", method = RequestMethod.GET)
    public String findLogAuthsByActionEquals(@ModelAttribute("command") LogSearchCriteria searchCriteria, @RequestParam(value = "page", required = false) Integer page, @RequestParam(value = "size", required = false) Integer size, @RequestParam(value = "sortFieldName", required = false) String sortFieldName, @RequestParam(value = "sortOrder", required = false) String sortOrder, Model uiModel) {
    	if("".equals(searchCriteria.getStatus())) {
    		return this.list(page, size, sortFieldName, sortOrder, uiModel);
    	}
    	if (page != null || size != null) {
            int sizeNo = size == null ? 10 : size.intValue();
            final int firstResult = page == null ? 0 : (page.intValue() - 1) * sizeNo;
            uiModel.addAttribute("logauths", LogAuth.findLogAuthsByActionEquals(searchCriteria.getStatus(), sortFieldName, sortOrder).setFirstResult(firstResult).setMaxResults(sizeNo).getResultList());
            float nrOfPages = (float) LogAuth.countFindLogAuthsByActionEquals(searchCriteria.getStatus()) / sizeNo;
            uiModel.addAttribute("maxPages", (int) ((nrOfPages > (int) nrOfPages || nrOfPages == 0.0) ? nrOfPages + 1 : nrOfPages));
        } else {
            uiModel.addAttribute("logauths", LogAuth.findLogAuthsByActionEquals(searchCriteria.getStatus(), sortFieldName, sortOrder).getResultList());
        }    
        
        uiModel.addAttribute("command", searchCriteria);
        uiModel.addAttribute("finderview", true);
        
        addDateTimeFormatPatterns(uiModel);
        return "admin/logauths/list";
    }
    
}
