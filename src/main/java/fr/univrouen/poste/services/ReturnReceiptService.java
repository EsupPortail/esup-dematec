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

import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.datetime.DateFormatter;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import fr.univrouen.poste.domain.AppliConfig;
import fr.univrouen.poste.domain.AppliConfig.MailReturnReceiptModeTypes;
import fr.univrouen.poste.domain.PosteCandidature;
import fr.univrouen.poste.domain.PosteCandidatureFile;
import fr.univrouen.poste.domain.User;

@Service
public class ReturnReceiptService {

	private static final long serialVersionUID = 1L;

	private final Logger logger = Logger.getLogger(getClass());

	@Autowired
	EmailService emailService;
	

	DateFormatter dateFormatter = new DateFormatter("dd/MM/yyyy HH:mm");

	public void logActionFile(String action, PosteCandidature postecandidature, PosteCandidatureFile postecandidatureFile, HttpServletRequest request, Date currentTime) {

		MailReturnReceiptModeTypes mailReturnReceiptMode = AppliConfig.getCacheMailReturnReceiptModeType();
		
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

	private void sendEmail(String fileName, String numEmploi) {	
		String mailTo = SecurityContextHolder.getContext().getAuthentication().getName();
		String mailFrom = AppliConfig.getCacheMailFrom();
		String mailSubject = AppliConfig.getCacheMailSubject();

		String mailMessage = AppliConfig.getCacheTexteMailCandidatReturnReceipt();
		mailMessage = mailMessage.replaceAll("@@fileName@@", fileName);       
		mailMessage = mailMessage.replaceAll("@@numEmploi@@", numEmploi);      
		emailService.sendMessage(mailFrom, mailTo, mailSubject, mailMessage);
	}

	
	public final void sendDepotStatusIfRequired(Authentication auth) {
		
		String emailAddress = auth.getName();
		Set<String> roles = AuthorityUtils.authorityListToSet(auth.getAuthorities());
				
		boolean isCandidat = roles.contains("ROLE_CANDIDAT");
		
		if(isCandidat) {
			MailReturnReceiptModeTypes mailReturnReceiptMode = AppliConfig.getCacheMailReturnReceiptModeType();
			if(MailReturnReceiptModeTypes.EACH_SESSION.equals(mailReturnReceiptMode)) {
				String messageBody = "";
				User candidat = User.findUsersByEmailAddress(emailAddress, null, null).getSingleResult();
				List<PosteCandidature> candidatures = PosteCandidature.findPosteCandidaturesByCandidat(candidat).getResultList();
				for(PosteCandidature candidature: candidatures) {
					messageBody = messageBody + "\n*Poste n°" + candidature.getPoste().getNumEmploi() + "*";
					for(PosteCandidatureFile candidatureFile : candidature.getCandidatureFiles()) {
						String filename = candidatureFile.getFilename();
						String fileSize =  candidatureFile.getFileSizeFormatted();
						String sentDate =  dateFormatter.print(candidatureFile.getSendTime(), Locale.getDefault());
						messageBody = messageBody + "\n - " + filename + " - " + fileSize + " [" + sentDate + "]";
					}
				}
				String mailFrom = AppliConfig.getCacheMailFrom();
				String mailSubject = AppliConfig.getCacheMailSubject();

				String mailMessage = AppliConfig.getCacheTexteMailCandidatReturnReceipt();
				mailMessage = mailMessage.replaceAll("@@messageBody@@", messageBody);           
				emailService.sendMessage(mailFrom, emailAddress, mailSubject, mailMessage);
			}
		}
		
		
	}

	
}
