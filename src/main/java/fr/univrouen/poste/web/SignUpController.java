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
package fr.univrouen.poste.web;

import java.util.Date;

import javax.persistence.TypedQuery;
import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.encoding.MessageDigestPasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

import fr.univrouen.poste.domain.AppliConfig;
import fr.univrouen.poste.domain.User;
import fr.univrouen.poste.services.CreateUserService;

@RequestMapping("/signup")
@Controller
public class SignUpController {

	private final Logger log = Logger.getLogger(getClass());

    @Autowired
    private SignUpValidator validator;
	
	@Autowired 
	private CreateUserService createUserService;

	@Autowired
	private MessageDigestPasswordEncoder messageDigestPasswordEncoder;
	
    @ModelAttribute("User")
    public UserRegistrationForm formBackingObject() {
        return new UserRegistrationForm();
    }

    
    @RequestMapping(method = RequestMethod.GET)
    public String createForm(Model model) {
    	if(AppliConfig.getCacheCandidatCanSignup()) {
	    	UserRegistrationForm form = new UserRegistrationForm();
	        model.addAttribute("User", form);
	        return "signup/index";
    	} else {
    		return "redirect:/";
    	}
    }
    
    
    @RequestMapping(params = "activate", method = RequestMethod.GET)
    public String activateUserComplexUrl(@RequestParam(value = "activate", required = true) String activationKey,@RequestParam(value = "emailAddress", required = true) String emailAddress,Model model) {
    	return activateUser(activationKey, emailAddress, model);
    }

    
    @RequestMapping(value = "/activate/{emailAddress}/{activationKey}")
    public String activateUserFriendlyUrl(@PathVariable("emailAddress") String emailAddress,  @PathVariable("activationKey") String activationKey, Model model) {
    	return activateUser(activationKey, emailAddress, model);
    }
    
	private String activateUser(String activationKey, String emailAddress, Model model) {
	    String textePremierePageAnonyme = AppliConfig.getCacheTextePremierePageAnonyme();
    	model.addAttribute("textePremierePageAnonyme", textePremierePageAnonyme);
    	TypedQuery<User> query = User.findUsersByActivationKeyAndEmailAddress(activationKey, emailAddress, null, null);
    	if(!query.getResultList().isEmpty()) {
        	User user = query.getSingleResult();
        	if(user.getPassword() != null) {
        		user.setActivationDate(new Date());
        		user.setEnabled(true);
        		user.merge();
        		return "login";
        	} else {
        		UserRegistrationForm form = new UserRegistrationForm();
        		form.setEmailAddress(emailAddress);
        		form.setActivationKey(activationKey);
                model.addAttribute("User", form);
        		return "signup/initpassword";
        	}
        }
        else{
        	return "signup/errorlink";
        }
    }
    
    @RequestMapping(value="/initpassword", method = RequestMethod.POST)  
    public String initPassword(@Valid UserRegistrationForm userRegistration, BindingResult result, Model model, HttpServletRequest request) {
    	String textePremierePageAnonyme = AppliConfig.getCacheTextePremierePageAnonyme();
    	model.addAttribute("textePremierePageAnonyme", textePremierePageAnonyme);
    	TypedQuery<User> query = User.findUsersByActivationKeyAndEmailAddress(userRegistration.getActivationKey(), userRegistration.getEmailAddress(), null, null);
        User User=query.getSingleResult();
        if(null!=User && userRegistration.getPassword().equals(userRegistration.getRepeatPassword()) && !userRegistration.getPassword().isEmpty()){
        	if(User.getPassword() == null) {
        		User.setActivationDate(new Date());
        		User.setEnabled(true);
        		User.setPassword(messageDigestPasswordEncoder.encodePassword(userRegistration.getPassword(), null));
        		User.merge();
        	} 
    		return "login";
        }
        else{
        	return "signup/error";
        }

    }

    @RequestMapping(method = RequestMethod.POST)
    public String create(UserRegistrationForm userRegistration, BindingResult result, Model model, HttpServletRequest request) {
    	if(AppliConfig.getCacheCandidatCanSignup()) {
	    	// be sure that there is no password sent by the web form
	    	userRegistration.setPassword(null);
	        if (result.hasErrors()) {
	        	log.error(result.toString());
	            return createForm(model);
	        } else {
	        	if(User.countFindUsersByEmailAddress(userRegistration.getEmailAddress())>0) {
	        		model.addAttribute("errorMessage", "Un compte avec cette même adresse mail est déjà présent dans cette application !");
	        		return createForm(model);
	        	} else {
	        		createUserService.createCandidatUser(userRegistration);
	        		return "signup/thanks";
	        	}
	        }
    	} else {
    		return "redirect:/";
    	}
    }

}
