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

import java.io.Serializable;

import org.apache.log4j.Logger;
import org.springframework.mail.MailSender;
import org.springframework.mail.SimpleMailMessage;

public class EmailService implements Serializable {
	
	private static final long serialVersionUID = 1L;

	private final Logger logger = Logger.getLogger(getClass());
	
    private transient MailSender mailSender; 
    
    private transient SimpleMailMessage mail = new SimpleMailMessage();  
    
    private LogService logService;
    
    private Boolean isEnabled = false;
  
    	
    public void setMailSender(MailSender mailSender) {
    	this.mailSender = mailSender;
    }
	public void setLogService(LogService logService) {
    	this.logService = logService;
    }
	public void setIsEnabled(Boolean isEnabled) {
    	this.isEnabled = isEnabled;
    }
	
	public boolean sendMessage(String mailFrom, String mailTo, String subject, String mailMessage) {
		if(this.isEnabled) {
	    	try {
	    		mail.setFrom(mailFrom);
			    mail.setTo(mailTo);
			    mail.setSubject(subject);
			    mail.setText(mailMessage);
		        mailSender.send(mail);
		        logger.debug("Email sent : " + mail.toString());
		        logService.logMail(mailTo, mailMessage, LogService.MAIL_SENT);
	    	} catch(Exception e) {   		
		        logger.error("Email failed : " + mail.toString(), e);
		        logService.logMail(mailTo, mailMessage, LogService.MAIL_FAILED);
		        return false;
	    	}
		} else {
			logger.warn("sendMessage called but email is not enabled ...");
			logger.info("\tmethod call was :  sendMessage(" + mailFrom + ", " + mailTo + ", " + subject + ", " + mailMessage + ")");
		}
		return true;
    }
	
}
