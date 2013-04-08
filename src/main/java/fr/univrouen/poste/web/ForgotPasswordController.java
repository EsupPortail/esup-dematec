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

import java.util.Random;

import javax.annotation.Resource;
import javax.persistence.TypedQuery;
import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.encoding.MessageDigestPasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import fr.univrouen.poste.domain.AppliConfig;
import fr.univrouen.poste.domain.User;
import fr.univrouen.poste.services.EmailService;
import fr.univrouen.poste.services.LogService;

@RequestMapping("/forgotpassword/**")
@Controller
public class ForgotPasswordController {

	@Autowired
	private LogService logService;
	
    @Resource
    private transient EmailService emailService;

	@Autowired
	private MessageDigestPasswordEncoder messageDigestPasswordEncoder;

    @ModelAttribute("forgotpasswordForm")
    public ForgotPasswordForm formBackingObject() {
        return new ForgotPasswordForm();
    }

    @RequestMapping
    public String index() {
        return "forgotpassword/index";
    }

    @RequestMapping(value = "/forgotpassword/update", method = RequestMethod.POST)
    public String update(@ModelAttribute("forgotpasswordForm") ForgotPasswordForm form, BindingResult result, HttpServletRequest request) {
        if (result.hasErrors()) {
        	return "forgotpassword/index";
        } else {
        	TypedQuery<User> userQuery=User.findUsersByEmailAddress(form.getEmailAddress());
        	if(null!=userQuery && !userQuery.getResultList().isEmpty()){
        		User User = userQuery.getSingleResult();
        		Random random = new Random(System.currentTimeMillis());
        		String newPassword = "pass"+random.nextLong();
        		User.setPassword(messageDigestPasswordEncoder.encodePassword(newPassword, null));
        		User.merge();

        		String mailTo = form.getEmailAddress();
        	    String mailFrom = AppliConfig.getCacheMailFrom();
        	    String mailSubject = AppliConfig.getCacheMailSubject();
        	    
        	    String mailMessage = AppliConfig.getCacheTexteMailPasswordOublie();
        	    mailMessage = mailMessage.replaceAll("@@newPassword@@", newPassword);        
        		
				logService.logActionAuth(LogService.AUTH_PASSWORD_FORGOT_SENT, User.getEmailAddress(), request.getRemoteAddr());
        		
        		emailService.sendMessage(mailFrom, mailTo, mailSubject, mailMessage);
        	} else {
        		logService.logActionAuth(LogService.AUTH_PASSWORD_FORGOT_FAILED, form.getEmailAddress(), request.getRemoteAddr());
        	}

            return "forgotpassword/thanks";
        }
    }

}
