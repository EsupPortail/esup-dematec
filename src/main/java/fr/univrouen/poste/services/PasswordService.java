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
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Random;

@Service
public class PasswordService {
	
	private final Logger logger = Logger.getLogger(getClass());

	@Autowired
	private PasswordEncoder passwordEncoder;
	
	@Autowired
	EmailService emailService;

	@Autowired
	private LogService logService;

	private Random random = new Random(System.currentTimeMillis());

	public void sendPasswordActivationKeyMail(User user, String remoteAdress) {
		String activationKey = generateActivationKey();
		user.setActivationKey(activationKey);
		user.merge();

		String mailTo = user.getEmailAddress();
		String mailFrom = AppliConfig.getCacheMailFrom();
		String mailSubject = AppliConfig.getCacheMailSubject();

		String mailMessage = AppliConfig.getCacheTexteMailPasswordOublie();
		mailMessage = mailMessage.replaceAll("@@activationKey@@", activationKey);

		logService.logActionAuth(LogService.AUTH_PASSWORD_FORGOT_SENT, user.getEmailAddress(), remoteAdress);

		emailService.sendMessage(mailFrom, mailTo, mailSubject, mailMessage);
	}

	String generateActivationKey() {
		String activationKey = "activationKey" + Math.abs(this.random.nextInt());
		while(User.countFindUsersByActivationKey(activationKey) > 0) {
			activationKey = "activationKey" + Math.abs(this.random.nextInt());
		}
		return activationKey;
	}

}
