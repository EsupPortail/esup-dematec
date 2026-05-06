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
import jakarta.annotation.Resource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.util.Base64;

@Service
public class PasswordService {
	
	final Logger logger = LoggerFactory.getLogger(getClass());

	@Resource
	PasswordEncoder passwordEncoder;
	
	@Resource
	EmailService emailService;

	@Resource
	LogService logService;

	@Resource
	AppliConfigDao appliConfigDao;

	@Resource
	UserDao userDao;

	private final SecureRandom secureRandom = new SecureRandom();

	public void sendPasswordActivationKeyMail(User user, String remoteAdress) {
		String activationKey = generateActivationKey();
		user.setActivationKey(activationKey);
		userDao.saveUser(user);

		String mailTo = user.getEmailAddress();
		String mailFrom = appliConfigDao.getAppliConfig().getMailFrom();
		String mailSubject = appliConfigDao.getAppliConfig().getMailSubject();

		String mailMessage = appliConfigDao.getAppliConfig().getTexteMailPasswordOublie();
		mailMessage = mailMessage.replaceAll("@@activationKey@@", activationKey);

		logService.logActionAuth(LogService.AUTH_PASSWORD_FORGOT_SENT, user.getEmailAddress(), remoteAdress);

		emailService.sendMessage(mailFrom, mailTo, mailSubject, mailMessage);
	}

	public String generateActivationKey() {
		String activationKey = generateSecureToken();
		while(userDao.countFindUsersByActivationKey(activationKey) > 0) {
			activationKey = generateSecureToken();
		}
		return activationKey;
	}

	private String generateSecureToken() {
		byte[] bytes = new byte[32];
		secureRandom.nextBytes(bytes);
		return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
	}

}
