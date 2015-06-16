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
package fr.univrouen.poste.web.candidat;

import java.util.List;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

import fr.univrouen.poste.domain.AppliConfig;
import fr.univrouen.poste.domain.User;
import fr.univrouen.poste.services.LogService;
import fr.univrouen.poste.services.PosteAPourvoirAvailableBean;
import fr.univrouen.poste.services.PosteAPourvoirService;

@RequestMapping("addpostecandidatures")
@Controller
@Transactional
public class CandidatAddPosteCandidatureController {

	private final Logger log = Logger.getLogger(getClass());

	@Autowired
	LogService logService;

	@Autowired
	PosteAPourvoirService posteAPourvoirService;

    @RequestMapping(method = RequestMethod.GET, produces = "text/html")
	@PreAuthorize("hasRole('ROLE_CANDIDAT')")
    public String postesForm(Model uiModel) {   	
		
    	if(!AppliConfig.getCacheCandidatCanSignup()) {
    		return "redirect:/postecandidatures";
    	}
    	
    	Authentication auth = SecurityContextHolder.getContext().getAuthentication();
    	String emailAddress = auth.getName();
		User candidat = User.findUsersByEmailAddress(emailAddress, null, null).getSingleResult();
		
    	List<PosteAPourvoirAvailableBean> posteapourvoirs = posteAPourvoirService.getPosteAPourvoirAvailables(candidat);
    	uiModel.addAttribute("posteapourvoirs", posteapourvoirs);
        return "addpostecandidatures/index";
    }
    
    @RequestMapping(method = RequestMethod.POST, produces = "text/html")
	@PreAuthorize("hasRole('ROLE_CANDIDAT')")
    public String addCandidature(@RequestParam(required=false) List<Long> posteIds, Model uiModel) {   	
    			
    	if(!AppliConfig.getCacheCandidatCanSignup()) {
    		return "redirect:/postecandidatures";
    	}
    	
    	if(posteIds != null) {
    	
	    	Authentication auth = SecurityContextHolder.getContext().getAuthentication();
	    	String emailAddress = auth.getName();
			User candidat = User.findUsersByEmailAddress(emailAddress, null, null).getSingleResult();
			
	    	log.info("Candidatures sur les postes : " + posteIds + " pour le candidat " + candidat);
	    	posteAPourvoirService.updateCandidatures(candidat, posteIds);
    	}
    	
    	return "redirect:/addpostecandidatures";
    }
    
    
}
