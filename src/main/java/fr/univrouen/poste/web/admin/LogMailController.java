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

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.roo.addon.web.mvc.controller.finder.RooWebFinder;
import org.springframework.roo.addon.web.mvc.controller.scaffold.RooWebScaffold;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

import fr.univrouen.poste.domain.AppliConfig;
import fr.univrouen.poste.domain.LogMail;
import fr.univrouen.poste.domain.PosteCandidature;
import fr.univrouen.poste.services.EmailService;

@RequestMapping("/admin/logmails")
@Controller
@RooWebScaffold(path = "admin/logmails", formBackingObject = LogMail.class, create=false, update=false, delete=false)
@RooWebFinder
public class LogMailController {
	
	
	@Autowired
	EmailService emailService;
	
	@RequestMapping(value = "/{id}/resend")
	@PreAuthorize("hasRole('ROLE_ADMIN') or hasRole('ROLE_SUPER_MANAGER')")
	public String modifyRecevableCandidatureFile(@PathVariable("id") Long id) {
		
		LogMail logMail = LogMail.findLogMail(id);
		
		String mailTo = logMail.getMailTo();
		String mailMessage = logMail.getMessage();
		
		// TODO : get mailSubject from logMail ...
		//String mailSubject = logMail.getSubject();
		String mailSubject = AppliConfig.getCacheMailSubject();
		
	    String mailFrom = AppliConfig.getCacheMailFrom();
	    
		emailService.sendMessage(mailFrom, mailTo, mailSubject, mailMessage);

		return "redirect:/admin/logmails/" + id.toString();
	}
    
}
