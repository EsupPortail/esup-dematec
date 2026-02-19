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
import fr.univrouen.poste.dao.PosteCandidatureDao;
import fr.univrouen.poste.dao.UserDao;
import fr.univrouen.poste.domain.AppliConfig.MailReturnReceiptModeTypes;
import fr.univrouen.poste.domain.PosteCandidature;
import fr.univrouen.poste.domain.PosteCandidatureFile;
import fr.univrouen.poste.domain.User;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Set;

@Service
public class ReturnReceiptService {

	static final long serialVersionUID = 1L;

	final Logger logger = LoggerFactory.getLogger(getClass());

	@Resource
	EmailService emailService;

	@Resource
	AppliConfigDao appliConfigDao;

	@Resource
	UserDao userDao;

	@Resource
	PosteCandidatureDao posteCandidatureDao;
	

	DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

	public void logActionFile(String action, PosteCandidature postecandidature, PosteCandidatureFile postecandidatureFile, HttpServletRequest request, LocalDateTime currentTime) {

		MailReturnReceiptModeTypes mailReturnReceiptMode = appliConfigDao.getAppliConfig().getMailReturnReceiptModeType();
		
		synchronized (this) {

			switch(mailReturnReceiptMode) {
				case EACH_UPLOAD:
					if(LogService.UPLOAD_ACTION.equals(action)) {
						sendEmail(postecandidatureFile.getFilename(), postecandidature.getPoste().getNumEmploi());
					}
					break;
				case NEVER:
					break;
				default:
					break;
			}

		}

	}

	void sendEmail(String fileName, String numEmploi) {	
		String mailTo = SecurityContextHolder.getContext().getAuthentication().getName();
		String mailFrom = appliConfigDao.getAppliConfig().getMailFrom();
		String mailSubject = appliConfigDao.getAppliConfig().getMailSubject();

		String mailMessage = appliConfigDao.getAppliConfig().getTexteMailCandidatReturnReceipt();
		mailMessage = mailMessage.replaceAll("@@fileName@@", fileName);       
		mailMessage = mailMessage.replaceAll("@@numEmploi@@", numEmploi);      
		emailService.sendMessage(mailFrom, mailTo, mailSubject, mailMessage);
	}

	
	public final void sendDepotStatusIfRequired(Authentication auth) {
		
		String emailAddress = auth.getName();
		Set<String> roles = AuthorityUtils.authorityListToSet(auth.getAuthorities());
				
		boolean isCandidat = roles.contains("ROLE_CANDIDAT");
		
		if(isCandidat) {
			MailReturnReceiptModeTypes mailReturnReceiptMode = appliConfigDao.getAppliConfig().getMailReturnReceiptModeType();
			if(MailReturnReceiptModeTypes.EACH_SESSION.equals(mailReturnReceiptMode)) {
				String messageBody = "";
				User candidat = userDao.findUsersByEmailAddress(emailAddress);
				Page<PosteCandidature> candidatures = posteCandidatureDao.findPosteCandidaturesByCandidat(candidat);
				for(PosteCandidature candidature: candidatures) {
					messageBody = messageBody + "\n*Poste n°" + candidature.getPoste().getNumEmploi() + "*";
					for(PosteCandidatureFile candidatureFile : candidature.getCandidatureFiles()) {
						String filename = candidatureFile.getFilename();
						String fileSize =  candidatureFile.getFileSizeFormatted();
						String sentDate =  candidatureFile.getSendTime().format(dateFormatter);
						messageBody = messageBody + "\n - " + filename + " - " + fileSize + " [" + sentDate + "]";
					}
				}
				String mailFrom = appliConfigDao.getAppliConfig().getMailFrom();
				String mailSubject = appliConfigDao.getAppliConfig().getMailSubject();

				String mailMessage = appliConfigDao.getAppliConfig().getTexteMailCandidatReturnReceipt();
				mailMessage = mailMessage.replaceAll("@@messageBody@@", messageBody);           
				emailService.sendMessage(mailFrom, emailAddress, mailSubject, mailMessage);
			}
		}
		
		
	}

	
}
