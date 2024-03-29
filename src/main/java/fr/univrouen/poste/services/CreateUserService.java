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
package fr.univrouen.poste.services;

import fr.univrouen.poste.domain.AppliConfig;
import fr.univrouen.poste.domain.User;
import fr.univrouen.poste.web.UserRegistrationForm;
import org.apache.commons.lang3.text.WordUtils;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Random;

@Service
public class CreateUserService {
	
	private final Logger logger = Logger.getLogger(getClass());
	
	@Autowired
	private PasswordEncoder passwordEncoder;
	
	@Autowired
	EmailService emailService;

	public User createCandidatUser(UserRegistrationForm userRegistration) {
	    String mailSubject = AppliConfig.getCacheMailSubject();
	    String mailMessage = AppliConfig.getCacheTexteMailActivation();
	    User user = this.createUser(userRegistration, mailSubject, mailMessage);
	    // default numCandidat == email
	    user.setNumCandidat(user.getEmailAddress());
	    user.merge();
	    return user;
    }

	public User createMembreUser(UserRegistrationForm userRegistration) {
	    String mailSubject = AppliConfig.getCacheMailSubjectMembre();
	    String mailMessage = AppliConfig.getCacheTexteMailActivationMembre();
	    return this.createUser(userRegistration, mailSubject, mailMessage);
    }

	private User createUser(UserRegistrationForm userRegistration, String mailSubject, String mailMessage) {
	    Random random = new Random(System.currentTimeMillis());
	    String activationKey = "activationKey" + Math.abs(random.nextInt());

	    User user = new User();
	    user.setActivationDate(null);
	    user.setEmailAddress(userRegistration.getEmailAddress());
	    if(userRegistration.getPassword() != null) {
	    	user.setPassword(passwordEncoder.encode(userRegistration.getPassword()));
	    }
	    if(userRegistration.getLastName() != null) {
	    	user.setNom(userRegistration.getLastName());
	    }
		if(userRegistration.getFirstName() != null) {
		    user.setPrenom(userRegistration.getFirstName());
		}
		if(userRegistration.getCivilite() != null) {
			user.setCivilite(userRegistration.getCivilite());
		}
	    user.setActivationKey(activationKey);
	    user.setEnabled(true);
	    user.persist();
	    
	    String mailTo = user.getEmailAddress();
	    String mailFrom = AppliConfig.getCacheMailFrom();

	    mailMessage = mailMessage.replaceAll("@@mailAddress@@", mailTo);
	    mailMessage = mailMessage.replaceAll("@@activationKey@@", activationKey);
	    mailMessage = mailMessage.replaceAll("@@nom@@", WordUtils.capitalizeFully(user.getNom()));
	    mailMessage = mailMessage.replaceAll("@@prenom@@", WordUtils.capitalizeFully(user.getPrenom()));
	    
	    if(emailService.sendMessage(mailFrom, mailTo, mailSubject, mailMessage)) {
	    	logger.warn("User with email " + user.getEmailAddress() + " is created and we sent him an email");
	    } else {
	    	user.remove();
	    	return null;
	    }    	
	    
	    return user;
    }
}
