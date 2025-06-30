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
import fr.univrouen.poste.services.LogService;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

@RequestMapping("/changepassword/**")
@Controller
public class ChangePasswordController {

	@Resource
	LogService logService;
	
	@Resource
	ChangePasswordValidator validator;

	@Resource
	PasswordEncoder passwordEncoder;
    @Resource
    UserDao userDao;

	@ModelAttribute("changePasswordForm")
	public ChangePasswordForm formBackingObject() {
		return new ChangePasswordForm();
	}

	@RequestMapping(value = "/changepassword/index")
	public String index() {
		if (SecurityContextHolder.getContext().getAuthentication()
				.isAuthenticated()) {
			return "changepassword/index";
		} else {
			return "login";
		}
	}

	@RequestMapping(value = "/changepassword/update", method = RequestMethod.POST)
	public String update(
			@ModelAttribute("changePasswordForm") ChangePasswordForm form,
			BindingResult result, HttpServletRequest request) {
		validator.validate(form, result);
		if (result.hasErrors()) {
			return "changepassword/index"; // back to form
		} else {
			if (SecurityContextHolder.getContext().getAuthentication()
					.isAuthenticated()) {
				UserDetails userDetails = (UserDetails) SecurityContextHolder
						.getContext().getAuthentication().getPrincipal();
				String newPassword = form.getNewPassword();
				User person =  userDao.findUsersByEmailAddress(userDetails.getUsername());
				person.setPassword(passwordEncoder.encode(newPassword));
				userDao.saveUser(person);
				logService.logActionAuth(LogService.AUTH_PASSWORD_CHANGED, userDetails.getUsername(), request.getRemoteAddr());
				return "changepassword/thanks";
			} else {
				return "login";
			}
		}
	}

	@RequestMapping(value = "/changepassword/thanks")
	public String thanks() {
		return "changepassword/thanks";
	}

}
