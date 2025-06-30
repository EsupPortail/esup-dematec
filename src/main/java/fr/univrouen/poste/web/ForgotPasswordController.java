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

import fr.univrouen.poste.dao.UserDao;
import fr.univrouen.poste.domain.User;
import fr.univrouen.poste.services.EmailService;
import fr.univrouen.poste.services.LogService;
import fr.univrouen.poste.services.PasswordService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import java.util.List;

@RequestMapping("/forgotpassword/**")
@Controller
@Transactional
public class ForgotPasswordController {

	@Resource
	LogService logService;
	
    @Resource
    transient EmailService emailService;

	@Resource
	PasswordEncoder passwordEncoder;
	
	@Resource
	ForgotChangePasswordValidator validator;
	
	@Resource
	PasswordService passwordService;

	@Resource
	UserDao userDao;

    @ModelAttribute("forgotpasswordForm")
    public ForgotPasswordForm formBackingObject() {
        return new ForgotPasswordForm();
    }

    @RequestMapping
    public String index() {
        return "forgotpassword/index";
    }

    @RequestMapping(value = "/forgotpassword/update", method = RequestMethod.POST)
    public String update(@ModelAttribute("forgotpasswordForm") @Valid ForgotPasswordForm form, BindingResult result, HttpServletRequest request) {
        if (result.hasErrors()) {
        	return "forgotpassword/index";
        } else {
        	List<User> users = userDao.findUsersByEmailAddressAndActivationDateIsNotNull(form.getEmailAddress().trim());
        	if(users != null && !users.isEmpty()){
        		User user = users.get(0);
				passwordService.sendPasswordActivationKeyMail(user, request.getRemoteAddr());
			} else {
        		logService.logActionAuth(LogService.AUTH_PASSWORD_FORGOT_FAILED, form.getEmailAddress(), request.getRemoteAddr());
        	}

            return "forgotpassword/thanks";
        }
    }

    @RequestMapping(value = "/forgotpassword/formChange", method = RequestMethod.GET)
    public String modifyPasswordFormWithActivationKey(@RequestParam String activationKey, Model model) {
        List<User> users = userDao.findUsersByActivationKey(activationKey);
        if(users != null && !users.isEmpty()){
        	ForgotChangePasswordForm changePasswordForm = new ForgotChangePasswordForm();
        	changePasswordForm.setActivationKey(activationKey);
        	model.addAttribute("changePasswordForm", changePasswordForm);
        	return "forgotpassword/formChange";
        }
        return "redirect:/";
    }
    
    @RequestMapping(value = "/forgotpassword/change", method = RequestMethod.POST)
    public String modifyPasswordWithActivationKey(@ModelAttribute("changePasswordForm") ForgotChangePasswordForm form,
			BindingResult result, HttpServletRequest request) {
		validator.validate(form, result);
		if (result.hasErrors()) {
			return "changepassword/index";
		}
        List<User> users = userDao.findUsersByActivationKey(form.getActivationKey());
        if(users != null && !users.isEmpty()){
        	User user = users.get(0);
        	user.setPassword(passwordEncoder.encode(form.getNewPassword()));
        	user.setActivationKey(null);
        	userDao.saveUser(user);
        }
        return "redirect:/";
    }


}
