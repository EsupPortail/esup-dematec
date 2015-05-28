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

import javax.persistence.EntityNotFoundException;
import javax.persistence.NonUniqueResultException;
import javax.persistence.Query;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.encoding.MessageDigestPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;

import fr.univrouen.poste.domain.User;

@Service("forgotChangePasswordValidator")
public class ForgotChangePasswordValidator implements Validator {

	@Autowired
	private MessageDigestPasswordEncoder messageDigestPasswordEncoder;


	@Override
	public boolean supports(Class<?> clazz) {
		return ForgotChangePasswordForm.class.equals(clazz);
	}


	@Override
	public void validate(Object target, Errors errors) {
		ForgotChangePasswordForm form = (ForgotChangePasswordForm) target;

		try {
			Query query = User.findUsersByActivationKey(form.getActivationKey());
			if(null!=query) {
				query.getSingleResult(); // result not used but assert that there is one (and only) result for the query 
				String newPassword = form.getNewPassword();
				String newPasswordAgain = form.getNewPasswordAgain();
				if (!newPassword.equals(newPasswordAgain)) {
					errors.reject("changepassword.passwordsnomatch");
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
