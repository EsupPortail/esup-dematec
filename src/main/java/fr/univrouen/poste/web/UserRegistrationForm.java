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

import org.springframework.roo.addon.javabean.RooJavaBean;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

@RooJavaBean
public class UserRegistrationForm {
	@NotNull
	@Size(min = 1, message="Merci de saisir votre civilité")
	private String civilite;

	@NotNull
	@Size(min = 1, message="Merci de saisir votre prénom")
	private String firstName;
	
	@NotNull
	@Size(min = 1, message="Merci de saisir votre nom de famille")
	private String lastName;
	
	@NotNull
	@Size(min = 1, message="Merci de saisir votre adresse email")
	private String emailAddress;
	
	private String password;
	
	private String repeatPassword;

	private String activationKey;

    // don't care of upper/lower case for authentication with email ...
	public void setEmailAddress(String emailAddress) {
    	this.emailAddress = emailAddress.toLowerCase().trim();
    }
	
    public void setFirstName(String firstName) {
        this.firstName = firstName.trim();
    }
    
    public void setLastName(String lastName) {
        this.lastName = lastName.trim();
    }

	public String getCivilite() {
		return civilite;
	}

	public void setCivilite(String civilite) {
		this.civilite = civilite;
	}
}
