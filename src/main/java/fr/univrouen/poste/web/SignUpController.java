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

import fr.univrouen.poste.dao.AppliConfigDao;
import fr.univrouen.poste.dao.UserDao;
import fr.univrouen.poste.domain.AppliConfig;
import fr.univrouen.poste.domain.User;
import fr.univrouen.poste.services.CreateUserService;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;
import java.util.Date;
import java.util.List;

@RequestMapping("/signup")
@Controller
public class SignUpController {

	final Logger log = LoggerFactory.getLogger(getClass());
	
	@Resource
	CreateUserService createUserService;

	@Resource
	PasswordEncoder passwordEncoder;

	@Resource
	AppliConfigDao appliConfigDao;

	@Resource
	UserDao userDao;
	
    @ModelAttribute("User")
    public UserRegistrationForm formBackingObject() {
        return new UserRegistrationForm();
    }

	@ModelAttribute("civilitesEnum")
	public List<String> civilitesEnum() {
		return Arrays.asList("", "Mme", "M.");
	}
    
    @RequestMapping(method = RequestMethod.GET)
    public String createForm(Model model) {
    	AppliConfig config = appliConfigDao.getAppliConfig();
    	Boolean candidatCanSignup = config != null ? config.getCandidatCanSignup() : false;
    	if(candidatCanSignup) {
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
    public String activateUserFriendlyUrl(@PathVariable String emailAddress, @PathVariable String activationKey, Model model) {
    	return activateUser(activationKey, emailAddress, model);
    }
    
	String activateUser(String activationKey, String emailAddress, Model model) {
	    AppliConfig config = appliConfigDao.getAppliConfig();
	    String textePremierePageAnonyme = config != null ? config.getTextePremierePageAnonyme() : "";
    	model.addAttribute("textePremierePageAnonyme", textePremierePageAnonyme);
    	List<User> users = userDao.findUsersByActivationKeyAndEmailAddress(activationKey, emailAddress);
    	if(users != null && !users.isEmpty()) {
        	User user = users.get(0);
        	if(user.getPassword() != null) {
        		user.setActivationDate(new Date());
        		user.setEnabled(true);
        		userDao.saveUser(user);
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
    	AppliConfig config = appliConfigDao.getAppliConfig();
    	String textePremierePageAnonyme = config != null ? config.getTextePremierePageAnonyme() : "";
    	model.addAttribute("textePremierePageAnonyme", textePremierePageAnonyme);
    	List<User> users = userDao.findUsersByActivationKeyAndEmailAddress(userRegistration.getActivationKey(), userRegistration.getEmailAddress());
        User userEntity = (users != null && !users.isEmpty()) ? users.get(0) : null;
        if(userEntity != null && userRegistration.getPassword().equals(userRegistration.getRepeatPassword()) && !userRegistration.getPassword().isEmpty()){
        	if(userEntity.getPassword() == null) {
        		userEntity.setActivationDate(new Date());
        		userEntity.setEnabled(true);
        		userEntity.setPassword(passwordEncoder.encode(userRegistration.getPassword()));
        		userDao.saveUser(userEntity);
        	} 
    		return "login";
        }
        else{
        	return "signup/error";
        }

    }

    @RequestMapping(method = RequestMethod.POST)
    public String create(@ModelAttribute("User") @Valid UserRegistrationForm userRegistration, BindingResult result, Model model, HttpServletRequest request) {
    	AppliConfig config = appliConfigDao.getAppliConfig();
    	Boolean candidatCanSignup = config != null ? config.getCandidatCanSignup() : false;
    	Date currentTime = new Date();
    	if (candidatCanSignup && config != null) {
    		candidatCanSignup = currentTime.compareTo(config.getDateEndCandidat()) < 0;
    	}
    	if(candidatCanSignup) {
	    	// be sure that there is no password sent by the web form
	    	userRegistration.setPassword(null);
	        if (result.hasErrors()) {
	        	log.warn(result.toString());
	            return "signup/index";
	        } else {
	        	if(userDao.countFindUsersByEmailAddress(userRegistration.getEmailAddress())>0) {
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
