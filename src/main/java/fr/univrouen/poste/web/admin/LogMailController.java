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

import fr.univrouen.poste.dao.AppliConfigDao;
import fr.univrouen.poste.dao.LogMailDao;
import fr.univrouen.poste.domain.AppliConfig;
import fr.univrouen.poste.domain.LogMail;
import fr.univrouen.poste.services.EmailService;
import fr.univrouen.poste.web.searchcriteria.LogSearchCriteria;
import jakarta.annotation.Resource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.ArrayList;
import java.util.List;

@RequestMapping("/admin/logmails")
@Controller
public class LogMailController {
	
	@Resource
	LogMailDao logMailDao;
	
	@Resource
	EmailService emailService;

	@Resource
	AppliConfigDao appliConfigDao;
	
    @ModelAttribute("command") 
    public LogSearchCriteria getLogSearchCriteria() {
    	return new LogSearchCriteria();
    }
    
	@ModelAttribute("users")
	public List<String> getUserIds() {
		List<String> userIds = logMailDao.getAllMailTo();
		userIds.add(0, "");
		return userIds;
	}
    
    @RequestMapping(params = "find=ByStatusEqualsAndUserIdEquals", method = RequestMethod.GET)
    public String findLogMailsByStatusEqualsAndUserIdEquals(@ModelAttribute("command") LogSearchCriteria searchCriteria, @PageableDefault(size = 10) Pageable pageable, @RequestParam(value = "sortFieldName", required = false) String sortFieldName, @RequestParam(value = "sortOrder", required = false) String sortOrder, Model uiModel) {
    	if("".equals(searchCriteria.getStatus()) && "".equals(searchCriteria.getUserId())) {
    		return this.list(pageable, sortFieldName, sortOrder, uiModel);
    	}
        List<LogMail> all = logMailDao.findLogMails(searchCriteria.getStatus(), searchCriteria.getUserId(), sortFieldName, sortOrder);
        if (pageable.isPaged()) {
            int start = (int) pageable.getOffset();
            int end = Math.min(start + pageable.getPageSize(), all.size());
            List<LogMail> sub = start <= end ? all.subList(start, end) : new ArrayList<>();
            uiModel.addAttribute("logmails", new PageImpl<>(sub, pageable, all.size()));
        } else {
            uiModel.addAttribute("logmails", all);
        }
        uiModel.addAttribute("command", searchCriteria);
        uiModel.addAttribute("finderview", true);
        addDateTimeFormatPatterns(uiModel);
        return "admin/logmails/list";
    }
    
	@RequestMapping(value = "/{id}/resend", method = RequestMethod.POST)
	@PreAuthorize("hasRole('ROLE_ADMIN') or hasRole('ROLE_SUPER_MANAGER')")
	public String resendEmail(@PathVariable Long id, RedirectAttributes ra) {
		// With 'RedirectAttributes ra' we disable here adding model attributes in request header when redirecting ...
		
		LogMail logMail = logMailDao.findLogMail(id);
		
		String mailTo = logMail.getMailTo();
		String mailMessage = logMail.getMessage();
		
		AppliConfig config = appliConfigDao.getAppliConfig();
		String mailSubject = config != null ? config.getMailSubject() : "";
		String mailFrom = config != null ? config.getMailFrom() : "";
	    
		Boolean emailSent = emailService.sendMessage(mailFrom, mailTo, mailSubject, mailMessage);
		ra.addAttribute("emailSent", emailSent);
		
		return "redirect:/admin/logmails/" + id.toString();
	}
    

	@RequestMapping(method = RequestMethod.GET, value = "/{id}", produces = "text/html")
    public String show(@PathVariable Long id, Model uiModel) {
        addDateTimeFormatPatterns(uiModel);
        uiModel.addAttribute("logmail", logMailDao.findLogMail(id));
        uiModel.addAttribute("itemId", id);
        return "admin/logmails/show";
    }

	@RequestMapping(produces = "text/html")
    public String list(@PageableDefault(size = 10) Pageable pageable, @RequestParam(value = "sortFieldName", required = false) String sortFieldName, @RequestParam(value = "sortOrder", required = false) String sortOrder, Model uiModel) {
        if (pageable.isPaged()) {
            Page<LogMail> result = logMailDao.findLogMailEntries(pageable, sortFieldName, sortOrder);
            uiModel.addAttribute("logmails", result);
        } else {
            uiModel.addAttribute("logmails", logMailDao.findAllLogMails(sortFieldName, sortOrder));
        }
        addDateTimeFormatPatterns(uiModel);
        return "admin/logmails/list";
    }

	void addDateTimeFormatPatterns(Model uiModel) {
        uiModel.addAttribute("logMail_actiondate_date_format", "dd/MM/yyyy HH:mm");
    }
}
