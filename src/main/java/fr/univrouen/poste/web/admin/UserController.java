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

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.roo.addon.web.mvc.controller.finder.RooWebFinder;
import org.springframework.roo.addon.web.mvc.controller.scaffold.RooWebScaffold;
import org.springframework.security.authentication.encoding.MessageDigestPasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

import fr.univrouen.poste.domain.User;

@RooWebScaffold(path = "admin/users", formBackingObject = User.class)
@RequestMapping("/admin/users")
@Controller
@RooWebFinder
public class UserController {

	private final Logger logger = Logger.getLogger(getClass());
	
    @Autowired
    private MessageDigestPasswordEncoder messageDigestPasswordEncoder;

    @RequestMapping(method = RequestMethod.POST, produces = "text/html")
    public String create(@Valid User user, BindingResult result, Model model, HttpServletRequest request) {
        if (result.hasErrors()) {
            model.addAttribute("user", user);
            logger.error("Error when creating an user : " + result.getGlobalError());
            return "admin/users/create";
        }
        if (user.getId() != null) {
            User savedUser = User.findUser(user.getId());
            if (!savedUser.getPassword().equals(user.getPassword())) {
                user.setPassword(messageDigestPasswordEncoder.encodePassword(user.getPassword(), null));
            }
        } else {
            user.setPassword(messageDigestPasswordEncoder.encodePassword(user.getPassword(), null));
        }
        user.persist();
        return "redirect:/admin/users/" + user.getId().toString();
    }
    
    
    @RequestMapping(method = RequestMethod.PUT, produces = "text/html")
    public String update(@Valid User user, BindingResult bindingResult, Model uiModel, HttpServletRequest httpServletRequest) {
        if (bindingResult.hasErrors()) {
            populateEditForm(uiModel, user);
            return "admin/users/update";
        }
        uiModel.asMap().clear();
        if (user.getId() != null) {
            User savedUser = User.findUser(user.getId());
            if (!user.getPassword().equals(savedUser.getPassword())) {
                user.setPassword(messageDigestPasswordEncoder.encodePassword(user.getPassword(), null));
            }
        } else {
            user.setPassword(messageDigestPasswordEncoder.encodePassword(user.getPassword(), null));
        }
        user.merge();
        return "redirect:/admin/users/" + encodeUrlPathSegment(user.getId().toString(), httpServletRequest);
    }
    
    @RequestMapping(params = "find=ByStatus", method = RequestMethod.GET)
    public String findUsersByStatus(
    		@RequestParam(value = "status", required=false) String status, @RequestParam(value = "page", required = false) Integer page, @RequestParam(value = "size", required = false) Integer size, Model uiModel) {
    	
    	if("Admin".equals(status))
    		return this.findUsersByIsAdmin(true, page, size, uiModel);
    	
    	else if("SuperManager".equals(status))
    		return this.findUsersByIsSuperManager(true, page, size, uiModel);
    	
    	else if("Manager".equals(status))
    		return this.findUsersByIsManager(true, page, size, uiModel);
    	
    	else if("Membre".equals(status))
    		return this.findUsersByMembre(true, page, size, uiModel);
    	
    	else if("Candidat".equals(status))
    		return this.findUsersByCandidat(true, page, size, uiModel);
    	
    	else
    		return this.list(page, size, uiModel);
    }


	private String findUsersByCandidat(boolean b, Integer page, Integer size,
			Model uiModel) {
        if (page != null || size != null) {
            int sizeNo = size == null ? 10 : size.intValue();
            final int firstResult = page == null ? 0 : (page.intValue() - 1) * sizeNo;
            uiModel.addAttribute("users", User.findAllCandidats().setFirstResult(firstResult).setMaxResults(sizeNo).getResultList());
            float nrOfPages = (float) User.countCandidats() / sizeNo;
            uiModel.addAttribute("maxPages", (int) ((nrOfPages > (int) nrOfPages || nrOfPages == 0.0) ? nrOfPages + 1 : nrOfPages));
        } else {
            uiModel.addAttribute("users", User.findAllCandidats().getResultList());
        }
        addDateTimeFormatPatterns(uiModel);
        return "admin/users/list";
	}


	private String findUsersByMembre(boolean b, Integer page, Integer size,
			Model uiModel) {
        if (page != null || size != null) {
            int sizeNo = size == null ? 10 : size.intValue();
            final int firstResult = page == null ? 0 : (page.intValue() - 1) * sizeNo;
            uiModel.addAttribute("users", User.findAllMembres().setFirstResult(firstResult).setMaxResults(sizeNo).getResultList());
            float nrOfPages = (float) User.countMembres() / sizeNo;
            uiModel.addAttribute("maxPages", (int) ((nrOfPages > (int) nrOfPages || nrOfPages == 0.0) ? nrOfPages + 1 : nrOfPages));
        } else {
            uiModel.addAttribute("users", User.findAllMembres().getResultList());
        }
        addDateTimeFormatPatterns(uiModel);
        return "admin/users/list";
	}


}

