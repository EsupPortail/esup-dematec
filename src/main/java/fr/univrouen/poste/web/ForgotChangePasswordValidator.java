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

import fr.univrouen.poste.dao.UserDao;
import fr.univrouen.poste.domain.User;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;

import java.util.List;

@Service("forgotChangePasswordValidator")
public class ForgotChangePasswordValidator implements Validator {

	@Resource
	UserDao userDao;

	@Override
	public boolean supports(Class<?> clazz) {
		return ForgotChangePasswordForm.class.equals(clazz);
	}


	@Override
	public void validate(Object target, Errors errors) {
		ForgotChangePasswordForm form = (ForgotChangePasswordForm) target;

		try {
			List<User> users = userDao.findUsersByActivationKey(form.getActivationKey());
			if (users != null && !users.isEmpty()) {
				if (users.size() > 1) {
					errors.rejectValue("emailAddress",
							"changepassword.duplicateemailaddress");
				} else {
					String newPassword = form.getNewPassword();
					String newPasswordAgain = form.getNewPasswordAgain();
					if (!newPassword.equals(newPasswordAgain)) {
						errors.reject("changepassword.passwordsnomatch");
					}
				}
			} else {
				errors.rejectValue("emailAddress",
						"changepassword.invalidemailaddress");
			}
		} catch (Exception e) {
			errors.rejectValue("emailAddress",
					"changepassword.invalidemailaddress");
		}
	}

}
