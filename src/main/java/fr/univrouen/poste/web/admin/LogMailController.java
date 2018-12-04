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

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.roo.addon.web.mvc.controller.scaffold.RooWebScaffold;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import fr.univrouen.poste.domain.AppliConfig;
import fr.univrouen.poste.domain.LogMail;
import fr.univrouen.poste.services.EmailService;
import fr.univrouen.poste.web.searchcriteria.LogSearchCriteria;

@RequestMapping("/admin/logmails")
@Controller
@RooWebScaffold(path = "admin/logmails", formBackingObject = LogMail.class, create=false, update=false, delete=false)
public class LogMailController {
	
	
	@Autowired
	EmailService emailService;
	
    @ModelAttribute("command") 
    public LogSearchCriteria getLogSearchCriteria() {
    	return new LogSearchCriteria();
    }
    
	@ModelAttribute("users")
	public List<String> getUserIds() {
		List<String> userIds = LogMail.getAllMailTo();
		userIds.add(0, "");
		return userIds;
	}
    
    @RequestMapping(params = "find=ByStatusEqualsAndUserIdEquals", method = RequestMethod.GET)
    public String findLogMailsByStatusEqualsAndUserIdEquals(@ModelAttribute("command") LogSearchCriteria searchCriteria, @RequestParam(value = "page", required = false) Integer page, @RequestParam(value = "size", required = false) Integer size, @RequestParam(value = "sortFieldName", required = false) String sortFieldName, @RequestParam(value = "sortOrder", required = false) String sortOrder, Model uiModel) {
    	if("".equals(searchCriteria.getStatus()) && "".equals(searchCriteria.getUserId())) {
    		return this.list(page, size, sortFieldName, sortOrder, uiModel);
    	}
    	if (page != null || size != null) {
            int sizeNo = size == null ? 10 : size.intValue();
            final int firstResult = page == null ? 0 : (page.intValue() - 1) * sizeNo;
            uiModel.addAttribute("logmails", LogMail.findLogMails(searchCriteria.getStatus(), searchCriteria.getUserId(), sortFieldName, sortOrder).setFirstResult(firstResult).setMaxResults(sizeNo).getResultList());
            float nrOfPages = (float) LogMail.countFindLogMails(searchCriteria.getStatus(), searchCriteria.getUserId()) / sizeNo;
            uiModel.addAttribute("maxPages", (int) ((nrOfPages > (int) nrOfPages || nrOfPages == 0.0) ? nrOfPages + 1 : nrOfPages));
        } else {
            uiModel.addAttribute("logmails", LogMail.findLogMails(searchCriteria.getStatus(), searchCriteria.getUserId(), sortFieldName, sortOrder).getResultList());
        }    	
    	
        uiModel.addAttribute("command", searchCriteria);
        uiModel.addAttribute("finderview", true);

        addDateTimeFormatPatterns(uiModel);
        return "admin/logmails/list";
    }
    
	@RequestMapping(value = "/{id}/resend", method = RequestMethod.POST)
	@PreAuthorize("hasRole('ROLE_ADMIN') or hasRole('ROLE_SUPER_MANAGER')")
	public String resendEmail(@PathVariable("id") Long id, RedirectAttributes ra) {
		// With 'RedirectAttributes ra' we disable here adding model attributes in request header when redirecting ...
		
		LogMail logMail = LogMail.findLogMail(id);
		
		String mailTo = logMail.getMailTo();
		String mailMessage = logMail.getMessage();
		
		// TODO : get mailSubject from logMail ...
		//String mailSubject = logMail.getSubject();
		String mailSubject = AppliConfig.getCacheMailSubject();
		
	    String mailFrom = AppliConfig.getCacheMailFrom();
	    
		Boolean emailSent = emailService.sendMessage(mailFrom, mailTo, mailSubject, mailMessage);
		ra.addAttribute("emailSent", emailSent);
		
		return "redirect:/admin/logmails/" + id.toString();
	}
    
}
