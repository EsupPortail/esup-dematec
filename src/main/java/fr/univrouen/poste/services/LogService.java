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

import java.util.Calendar;
import java.util.Date;

import javax.servlet.http.HttpServletRequest;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.switchuser.SwitchUserGrantedAuthority;
import org.springframework.stereotype.Service;

import fr.univrouen.poste.domain.LogAuth;
import fr.univrouen.poste.domain.LogFile;
import fr.univrouen.poste.domain.LogImportCommission;
import fr.univrouen.poste.domain.LogImportGalaxie;
import fr.univrouen.poste.domain.LogMail;
import fr.univrouen.poste.domain.PosteAPourvoir;
import fr.univrouen.poste.domain.PosteCandidature;
import fr.univrouen.poste.domain.PosteCandidatureFile;
import fr.univrouen.poste.domain.User;

@Service
public class LogService {
	
	public final static String UPLOAD_ACTION = "UPLOAD";
	
	public final static String DELETE_ACTION = "DELETE";

	public static final String DOWNLOAD_ACTION = "DOWNLOAD";

	public static final String AUTH_SUCCESS = "AUTH SUCCESS";

	public static final String AUTH_FAILED = "AUTH FAILED";

	public static final String AUTH_PASSWORD_CHANGED = "AUTH PASSWORD CHANGED";

	public static final String AUTH_PASSWORD_FORGOT_SENT = "AUTH PASSWORD FORGOT SENT";

	public static final String AUTH_PASSWORD_FORGOT_FAILED = "AUTH PASSWORD FORGOT FAILED";
	
	public static final String MAIL_SENT = "MAIL SENT";
	
	public static final String MAIL_FAILED = "MAIL FAILED";
	
	public static final String IMPORT_SUCCESS = "IMPORT SUCCESS";
	
	public static final String IMPORT_FAILED = "IMPORT FAILED";

	public void logActionFile(String action, PosteCandidature postecandidature, PosteCandidatureFile postecandidatureFile, HttpServletRequest request, Date currentTime) {
	    

	    User candidat = postecandidature.getCandidat();
	    PosteAPourvoir poste = postecandidature.getPoste();
		
		LogFile logFile = new LogFile();
		
		String userId = SecurityContextHolder.getContext().getAuthentication().getName();		
		// Switch User par un admin / super-manager ?                                                                                                                                                            
		for (GrantedAuthority a : SecurityContextHolder.getContext().getAuthentication().getAuthorities()) {
			if (a instanceof SwitchUserGrantedAuthority) {
				userId = ((SwitchUserGrantedAuthority)a).getSource().getName() + " [SU] " + userId;
			}
		}
		
		logFile.setUserId(userId);
	    	    
	    logFile.setNumEmploi(poste.getNumEmploi());
	    
	    logFile.setIp(request.getRemoteAddr());
	    
	    logFile.setAction(action);
	    logFile.setActionDate(currentTime);
	    
	    logFile.setFilename(postecandidatureFile.getFilename());
	    logFile.setFileSize(postecandidatureFile.getFileSizeFormatted());
	    
	    logFile.setCivilite(candidat.getCivilite());
	    logFile.setEmail(candidat.getEmailAddress());
	    logFile.setNom(candidat.getNom());
	    logFile.setNumCandidat(candidat.getNumCandidat());
	    logFile.setPrenom(candidat.getPrenom());
	    
	    // uer-agent
	    String userAgent = request.getHeader("User-Agent");
	    logFile.setUserAgent(userAgent);
	    

	    logFile.persist();
    }
	
	public void logActionAuth(String action, String userId, String ip) {
		
        Calendar cal = Calendar.getInstance();
        Date currentTime = cal.getTime();       

		LogAuth logAuth = new LogAuth();
		
		logAuth.setUserId(userId);	    	   
	    
		logAuth.setIp(ip);
		
	    logAuth.setAction(action);
	    logAuth.setActionDate(currentTime);

	    logAuth.persist();
    }
	
	public void logMail(String mailTo, String message, String status) {
		
        Calendar cal = Calendar.getInstance();
        Date currentTime = cal.getTime();       

		LogMail logMail = new LogMail();
		
		logMail.setMailTo(mailTo);
		logMail.setMessage(message);
		logMail.setStatus(status);
	    
		logMail.setActionDate(currentTime);

		logMail.persist();
    }
	
	public void logImportGalaxie(String message, String status) {
		
        Calendar cal = Calendar.getInstance();
        Date currentTime = cal.getTime();       

		LogImportGalaxie logImportGalaxie = new LogImportGalaxie();
		
		logImportGalaxie.setMessage(message);
		logImportGalaxie.setStatus(status);
	    
		logImportGalaxie.setActionDate(currentTime);

		logImportGalaxie.persist();
    }
	
	public void logImportCommission(String message, String status) {
		
        Calendar cal = Calendar.getInstance();
        Date currentTime = cal.getTime();       

        LogImportCommission logImportCommission = new LogImportCommission();
		
        logImportCommission.setMessage(message);
        logImportCommission.setStatus(status);
	    
        logImportCommission.setActionDate(currentTime);

        logImportCommission.persist();
    }
	
}
