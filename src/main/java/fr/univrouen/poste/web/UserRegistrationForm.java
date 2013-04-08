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

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

import org.springframework.roo.addon.javabean.RooJavaBean;

@RooJavaBean
public class UserRegistrationForm {

	@NotNull
	@Size(min = 1)
	private String firstName;
	
	@NotNull
	@Size(min = 1)
	private String lastName;
	
	@NotNull
	@Size(min = 1)
	private String emailAddress;
	
	@NotNull
	@Size(min = 1)
	private String password;
	
	@NotNull
	@Size(min = 1)
	private String repeatPassword;

	@NotNull
	@Size(min = 1)
	private String activationKey;

    // don't care of upper/lower case for authentication with email ...
	public void setEmailAddress(String emailAddress) {
    	this.emailAddress = emailAddress.toLowerCase();
    }

}
