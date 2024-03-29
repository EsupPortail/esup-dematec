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
/**
 * 
 */
package fr.univrouen.poste.web;

import fr.univrouen.poste.domain.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;

import javax.persistence.EntityNotFoundException;
import javax.persistence.NonUniqueResultException;
import javax.persistence.Query;

@Service("changePasswordValidator")
public class ChangePasswordValidator implements Validator {

	@Autowired
	private PasswordEncoder passwordEncoder;

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.springframework.validation.Validator#supports(java.lang.Class)
	 */
	@Override
	public boolean supports(Class<?> clazz) {
		return ChangePasswordForm.class.equals(clazz);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.springframework.validation.Validator#validate(java.lang.Object,
	 * org.springframework.validation.Errors)
	 */
	@Override
	public void validate(Object target, Errors errors) {
		ChangePasswordForm form = (ChangePasswordForm) target;

		try {
			if (SecurityContextHolder.getContext().getAuthentication()
					.isAuthenticated()) {
				UserDetails userDetails = (UserDetails) SecurityContextHolder
						.getContext().getAuthentication().getPrincipal();
				Query query = User
						.findUsersByEmailAddress(userDetails.getUsername(), null, null);
				if(null!=query){
					User person = (User) query.getSingleResult();
					String storedPassword = person.getPassword();
					String currentPassword = form.getOldPassword();
					if (!passwordEncoder.matches(currentPassword, storedPassword)) {
						errors.rejectValue("oldPassword",
								"changepassword.invalidpassword");
					}
					String newPassword = form.getNewPassword();
					String newPasswordAgain = form.getNewPasswordAgain();
					if (!newPassword.equals(newPasswordAgain)) {
						errors.reject("changepassword.passwordsnomatch");
					}
				}
			}
		} catch (EntityNotFoundException e) {
			errors.rejectValue("emailAddress",
					"changepassword.invalidemailaddress");
		} catch (NonUniqueResultException e) {
			errors.rejectValue("emailAddress",
					"changepassword.duplicateemailaddress");
		}
	}

}
