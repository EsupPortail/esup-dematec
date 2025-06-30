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

import fr.univrouen.poste.dao.AppliConfigDao;
import fr.univrouen.poste.dao.PosteAPourvoirDao;
import fr.univrouen.poste.dao.PosteCandidatureDao;
import fr.univrouen.poste.dao.UserDao;
import fr.univrouen.poste.domain.AppliConfig;
import fr.univrouen.poste.domain.PosteAPourvoir;
import fr.univrouen.poste.domain.PosteCandidature;
import fr.univrouen.poste.domain.User;
import fr.univrouen.poste.services.PosteAPourvoirAvailableBean;
import fr.univrouen.poste.services.PosteAPourvoirService;
import jakarta.annotation.Resource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@RequestMapping("addpostecandidatures")
@Controller
@Transactional
public class CandidatAddPosteCandidatureController {

	final Logger log = LoggerFactory.getLogger(getClass());

	@Resource
	UserDao userDao;

	@Resource
	PosteAPourvoirDao posteAPourvoirDao;

	@Resource
	PosteCandidatureDao posteCandidatureDao;

	@Resource
	PosteAPourvoirService posteAPourvoirService;

	@Resource
	AppliConfigDao appliConfigDao;

    @RequestMapping(method = RequestMethod.GET, produces = "text/html")
	@PreAuthorize("hasRole('ROLE_CANDIDAT')")
    public String postesForm(Model uiModel) {   	
		
    	AppliConfig config = appliConfigDao.getAppliConfig();
    	Boolean candidatCanSignup = config != null ? config.getCandidatCanSignup() : false;
    	if(!candidatCanSignup) {
    		return "redirect:/postecandidatures";
    	}
    	
    	Authentication auth = SecurityContextHolder.getContext().getAuthentication();
    	String emailAddress = auth.getName();
		User candidat = userDao.findUserByEmailAddress(emailAddress);
		
		if(candidat != null) {
			List<PosteAPourvoirAvailableBean> posteapourvoirs = posteAPourvoirService.getPosteAPourvoirAvailables(candidat);
			uiModel.addAttribute("posteapourvoirs", posteapourvoirs);
		}
        return "addpostecandidatures/index";
    }
    
    @RequestMapping(method = RequestMethod.POST, produces = "text/html")
	@PreAuthorize("hasRole('ROLE_CANDIDAT')")
    public String addCandidature(@RequestParam(required=false) List<Long> posteIds, Model uiModel) {   	
    			
    	AppliConfig config = appliConfigDao.getAppliConfig();
    	Boolean candidatCanSignup = config != null ? config.getCandidatCanSignup() : false;
    	if(!candidatCanSignup) {
    		return "redirect:/postecandidatures";
    	}
    	
    	if(posteIds != null) {
    	
	    	Authentication auth = SecurityContextHolder.getContext().getAuthentication();
	    	String emailAddress = auth.getName();
			User candidat = userDao.findUserByEmailAddress(emailAddress);
			
			if(candidat != null) {
				log.info("Candidatures sur les postes : " + posteIds + " pour le candidat " + candidat);
				posteAPourvoirService.updateCandidatures(candidat, posteIds);
			}
    	}
    	
    	return "redirect:/addpostecandidatures";
    }
    
    @RequestMapping(method = RequestMethod.DELETE, produces = "text/html")
	@PreAuthorize("hasRole('ROLE_CANDIDAT')")
    public String delCandidature(@RequestParam(required=false) List<Long> posteIds, Model uiModel) {   	
    			
    	AppliConfig config = appliConfigDao.getAppliConfig();
    	Boolean candidatCanSignup = config != null ? config.getCandidatCanSignup() : false;
    	if(!candidatCanSignup) {
    		return "redirect:/postecandidatures";
    	}
    	
    	if(posteIds != null) {
    	
	    	Authentication auth = SecurityContextHolder.getContext().getAuthentication();
	    	String emailAddress = auth.getName();
			User candidat = userDao.findUserByEmailAddress(emailAddress);
			
			if(candidat != null) {
				for(Long posteId : posteIds) {   			
					PosteAPourvoir poste = posteAPourvoirDao.findPosteAPourvoir(posteId);
					Page<PosteCandidature> candidatures = posteCandidatureDao.findPosteCandidaturesByCandidatAndPoste(candidat, poste);
					if(!candidatures.isEmpty()) {
						PosteCandidature candidature = candidatures.getContent().get(0);
						if(candidature.getModification() == null) {
							posteCandidatureDao.deletePosteCandidature(candidature);
							log.info("Candidatures annulées sur les postes : " + posteIds + " pour le candidat " + candidat);
						}
					}
				}
			}
    	}
    	
    	return "redirect:/addpostecandidatures";
    }
}
