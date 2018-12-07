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

import javax.servlet.http.HttpServletRequest;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

import fr.univrouen.poste.domain.PosteCandidature.RecevableEnum;
import fr.univrouen.poste.web.candidat.MyPosteCandidatureController;

@RequestMapping("admin/multipostecandidatures")
@Controller
public class PosteCandidatureMultiManageController {

	private final Logger log = Logger.getLogger(getClass());

	@Autowired
	MyPosteCandidatureController myPosteCandidatureController;

	
	@RequestMapping(value = "/manage", params = "non_recevable", method = RequestMethod.POST, produces = "text/html")
	public String candidaturesNonRecevables(@RequestParam("candidatureId") List<Long> pcIds, HttpServletRequest request) {
		for(Long id: pcIds) {
			myPosteCandidatureController.modifyRecevableCandidature(id, RecevableEnum.NON_RECEVABLE);
		}
	    String referer = request.getHeader("Referer");
	    return "redirect:" + referer;
	}
	
	@RequestMapping(value = "/manage", params = "recevable", method = RequestMethod.POST, produces = "text/html")
	public String candidaturesRecevables(@RequestParam("candidatureId") List<Long> pcIds, HttpServletRequest request) {
		for(Long id: pcIds) {
			myPosteCandidatureController.modifyRecevableCandidature(id, RecevableEnum.RECEVABLE);
		}
	    String referer = request.getHeader("Referer");
	    return "redirect:" + referer;
	}
	
	@RequestMapping(value = "/manage", params = "non_auditionnable", method = RequestMethod.POST, produces = "text/html")
	public String candidaturesNonAuditionnables(@RequestParam("candidatureId") List<Long> pcIds, @RequestParam(required=false) String mailCorps, HttpServletRequest request) {
		for(Long id: pcIds) {
			myPosteCandidatureController.modifyAuditionnableCandidatureFile(id, false, mailCorps);
		}
	    String referer = request.getHeader("Referer");
	    return "redirect:" + referer;
	}
	
	
	@RequestMapping(value = "/manage", params = "auditionnable", method = RequestMethod.POST, produces = "text/html")
	public String candidaturesAuditionnables(@RequestParam("candidatureId") List<Long> pcIds, @RequestParam(required=true) String mailCorps, HttpServletRequest request) {
		for(Long id: pcIds) {
			myPosteCandidatureController.modifyAuditionnableCandidatureFile(id, true, mailCorps);
		}
	    String referer = request.getHeader("Referer");
	    return "redirect:" + referer;
	}
	
	@RequestMapping(value = "/manage", params = "reviewstatus_vue", method = RequestMethod.POST, produces = "text/html")
	public String candidaturesVues(@RequestParam("candidatureId") List<Long> pcIds, HttpServletRequest request) {
		for(Long id: pcIds) {
			myPosteCandidatureController.modifyReviewCandidature(id, "Vue");
		}
	    String referer = request.getHeader("Referer");
	    return "redirect:" + referer;
	}
	
	@RequestMapping(value = "/manage", params = "reviewstatus_incomplet", method = RequestMethod.POST, produces = "text/html")
	public String candidaturesIncompletes(@RequestParam("candidatureId") List<Long> pcIds, HttpServletRequest request) {
		for(Long id: pcIds) {
			myPosteCandidatureController.modifyReviewCandidature(id, "Vue_incomplet");
		}
	    String referer = request.getHeader("Referer");
	    return "redirect:" + referer;
	}
	
	@RequestMapping(value = "/manage", params = "laureat", method = RequestMethod.POST, produces = "text/html")
	public String candidaturesLaureates(@RequestParam("candidatureId") List<Long> pcIds, @RequestParam(required=true) String mailCorps, HttpServletRequest request) {
		for(Long id: pcIds) {
			myPosteCandidatureController.modifyLaureatCandidatureFile(id, true, mailCorps);
		}
	    String referer = request.getHeader("Referer");
	    return "redirect:" + referer;
	}
	
}
