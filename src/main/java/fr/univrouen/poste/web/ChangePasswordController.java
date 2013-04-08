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

import javax.persistence.Query;
import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.encoding.MessageDigestPasswordEncoder;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import fr.univrouen.poste.domain.User;
import fr.univrouen.poste.services.LogService;

@RequestMapping("/changepassword/**")
@Controller
public class ChangePasswordController {

	@Autowired
	private LogService logService;
	
	@Autowired
	private ChangePasswordValidator validator;

	@Autowired
	private MessageDigestPasswordEncoder messageDigestPasswordEncoder;

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
				Query query = User
						.findUsersByEmailAddress(userDetails.getUsername());
				User person = (User) query.getSingleResult();
				person.setPassword(messageDigestPasswordEncoder.encodePassword(newPassword, null));
				person.merge();
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
