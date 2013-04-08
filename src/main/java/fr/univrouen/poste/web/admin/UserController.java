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
    		@RequestParam(value = "status", required=false) String status, Model uiModel) {
    	
    	if("Admin".equals(status))
    		uiModel.addAttribute("users", User.findUsersByIsAdmin(true).getResultList());
    	
    	else if("SuperManager".equals(status))
    		uiModel.addAttribute("users", User.findUsersByIsSuperManager(true).getResultList());
    	
    	else if("Manager".equals(status))
    		uiModel.addAttribute("users", User.findUsersByIsManager(true).getResultList());
    	
    	else if("Membre".equals(status))
    		uiModel.addAttribute("users", User.findAllMembres());
    	
    	else if("Candidat".equals(status))
    		uiModel.addAttribute("users", User.findAllCandidats());
    	
    	else
    		uiModel.addAttribute("users", User.findAllUsers());
    	  	
        return "admin/users/list";
    }


}

