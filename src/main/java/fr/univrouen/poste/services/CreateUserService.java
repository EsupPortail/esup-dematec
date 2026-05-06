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

import fr.univrouen.poste.dao.AppliConfigDao;
import fr.univrouen.poste.dao.UserDao;
import fr.univrouen.poste.domain.User;
import fr.univrouen.poste.web.UserRegistrationForm;
import jakarta.annotation.Resource;
import org.apache.commons.lang3.text.WordUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class CreateUserService {
	
	final Logger logger = LoggerFactory.getLogger(getClass());
	
	@Resource
	PasswordEncoder passwordEncoder;
	
	@Resource
	EmailService emailService;

	@Resource
	AppliConfigDao appliConfigDao;

	@Resource
	UserDao userDao;

	@Resource
	PasswordService passwordService;

	public User createCandidatUser(UserRegistrationForm userRegistration) {
	    String mailSubject = appliConfigDao.getAppliConfig().getMailSubject();
	    String mailMessage = appliConfigDao.getAppliConfig().getTexteMailActivation();
	    User user = this.createUser(userRegistration, mailSubject, mailMessage);
	    // default numCandidat == email
	    user.setNumCandidat(user.getEmailAddress());
		userDao.saveUser(user);
	    return user;
    }

	public User createMembreUser(UserRegistrationForm userRegistration) {
	    String mailSubject = appliConfigDao.getAppliConfig().getMailSubjectMembre();
	    String mailMessage = appliConfigDao.getAppliConfig().getTexteMailActivationMembre();
	    return this.createUser(userRegistration, mailSubject, mailMessage);
    }

	User createUser(UserRegistrationForm userRegistration, String mailSubject, String mailMessage) {
	    String activationKey = passwordService.generateActivationKey();

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
	    userDao.saveUser(user);
	    
	    String mailTo = user.getEmailAddress();
	    String mailFrom = appliConfigDao.getAppliConfig().getMailFrom();

	    mailMessage = mailMessage.replaceAll("@@mailAddress@@", mailTo);
	    mailMessage = mailMessage.replaceAll("@@activationKey@@", activationKey);
	    mailMessage = mailMessage.replaceAll("@@nom@@", WordUtils.capitalizeFully(user.getNom()));
	    mailMessage = mailMessage.replaceAll("@@prenom@@", WordUtils.capitalizeFully(user.getPrenom()));
	    
	    if(emailService.sendMessage(mailFrom, mailTo, mailSubject, mailMessage)) {
	    	logger.warn("User with email " + user.getEmailAddress() + " is created and we sent him an email");
	    } else {
			userDao.deleteUser(user);
	    	return null;
	    }    	
	    
	    return user;
    }
}
