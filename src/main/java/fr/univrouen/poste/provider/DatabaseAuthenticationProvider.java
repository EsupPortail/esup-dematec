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
package fr.univrouen.poste.provider;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import javax.annotation.Resource;
import javax.persistence.EntityNotFoundException;
import javax.persistence.NonUniqueResultException;
import javax.persistence.TypedQuery;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.authentication.dao.AbstractUserDetailsAuthenticationProvider;
import org.springframework.security.authentication.encoding.MessageDigestPasswordEncoder;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetails;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import fr.univrouen.poste.domain.AppliConfig;
import fr.univrouen.poste.domain.PosteCandidature;
import fr.univrouen.poste.domain.User;
import fr.univrouen.poste.services.LogService;
import fr.univrouen.poste.utils.DateClotureChecker;


@Service("databaseAuthenticationProvider")
public class DatabaseAuthenticationProvider extends AbstractUserDetailsAuthenticationProvider {

	private final Logger logger = Logger.getLogger(getClass());

	@Resource
	private LogService logService;

	@Resource
	private MessageDigestPasswordEncoder messageDigestPasswordEncoder;
	
	@Resource
	private DatabaseUserDetailsService databaseUserDetailsService;
	
	@Resource
	DateClotureChecker dateClotureChecker;

	private List<String> ipsStart4AdminManagerAuthList;

	@Value("${ipsStart4AdminManagerAuth}")
	public void SetIpsStart4AdminManagerAuth(String ipsStart4AdminManagerAuth) {
		ipsStart4AdminManagerAuthList = Arrays.asList(ipsStart4AdminManagerAuth.split(" "));
		logger.warn("Restricted access from this (started) ip for admins, super-managers and managers : " + ipsStart4AdminManagerAuthList);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.springframework.security.authentication.dao.
	 * AbstractUserDetailsAuthenticationProvider
	 * #additionalAuthenticationChecks(org
	 * .springframework.security.core.userdetails.UserDetails,
	 * org.springframework
	 * .security.authentication.UsernamePasswordAuthenticationToken)
	 */
	@Override
	protected void additionalAuthenticationChecks(UserDetails arg0, UsernamePasswordAuthenticationToken arg1) throws AuthenticationException {
		return;

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.springframework.security.authentication.dao.
	 * AbstractUserDetailsAuthenticationProvider#retrieveUser(java.lang.String,
	 * org
	 * .springframework.security.authentication.UsernamePasswordAuthenticationToken
	 * )
	 */
	@Override
	@Transactional(noRollbackFor = BadCredentialsException.class)
	protected UserDetails retrieveUser(String username, UsernamePasswordAuthenticationToken authentication) throws AuthenticationException {

		UserDetails userDetails = null;
		
		logger.debug("Inside retrieveUser");

		WebAuthenticationDetails wad = (WebAuthenticationDetails) authentication.getDetails();
		String userIPAddress = wad.getRemoteAddress();

		Boolean ipCanBeUsed4AuthAdminManager = this.isIpCanBeUsed4AuthAdminManager(userIPAddress);

		username = username.toLowerCase();

		String password = (String) authentication.getCredentials();
		if (!StringUtils.hasText(password) || !StringUtils.hasText(username)) {
			logService.logActionAuth(LogService.AUTH_FAILED, username, userIPAddress);
			throw new BadCredentialsException("Merci de saisir votre email et password");
		}
		String encryptedPassword = messageDigestPasswordEncoder.encodePassword(password, null);
		List<GrantedAuthority> authorities = new ArrayList<GrantedAuthority>();
		Boolean enabled;

		try {
			TypedQuery<User> query = User.findUsersByEmailAddress(username, null, null);

			User targetUser = (User) query.getSingleResult();		
			
			if (targetUser.isLocked()) {
				throw new BadCredentialsException("Compte vérouillé, merci de retenter d'ici quelques secondes.");
			}

			// authenticate the person
			String expectedPassword = targetUser.getPassword();
			if (!StringUtils.hasText(expectedPassword)) {
				logService.logActionAuth(LogService.AUTH_FAILED, username, userIPAddress);
				throw new BadCredentialsException("Aucun password pour " + username
				        + " n'est enregistré dans la base, merci d'activer votre compte via le lien d'activation envoyé par email. Contactez un administrateur si problème.");
			}
			if (!encryptedPassword.equals(expectedPassword)) {
				logService.logActionAuth(LogService.AUTH_FAILED, username, userIPAddress);
				throw new BadCredentialsException("Utilisateur/Password invalide.");
			}
			
			// restriction accés réseau
			if (!ipCanBeUsed4AuthAdminManager && (targetUser.getIsAdmin() || targetUser.getIsSuperManager() || targetUser.getIsManager())) {
				logService.logActionAuth(LogService.AUTH_FAILED, username, userIPAddress);
				logger.warn("User " + username + " tried to access to his admin/manager/supermanager account from this IP " + userIPAddress);
				throw new BadCredentialsException("Vous ne pouvez pas vous authentifier sur ce compte depuis cet accès réseau. Contactez un administrateur si problème.");
			}

			// restriction dates accés pour candidats et membres 
			boolean isCurrentTimeOk4ThisCandidat = dateClotureChecker.isCurrentTimeOk4ThisCandidat(targetUser);
			boolean isCurrentTimeOk4ThisMembre = dateClotureChecker.isCurrentTimeOk4ThisMembre(targetUser);
			if(!isCurrentTimeOk4ThisCandidat && !isCurrentTimeOk4ThisMembre) {
				if(targetUser.getIsCandidat() && !isCurrentTimeOk4ThisCandidat) {
			        logger.warn("User " + username + " tried to access to his candidat account but the dateEndCandidat is < current time");
			    }    	        
		        if(targetUser.getIsMembre() && !isCurrentTimeOk4ThisMembre) {
					logger.warn("User " + username + " tried to access to his membre account but the dateEndMembre is < current time");
				}     
		        logService.logActionAuth(LogService.AUTH_FAILED, username, userIPAddress);
		        throw new BadCredentialsException("La date de clôture des dépôts est dépassée, vous ne pouvez maintenant plus accéder à l'application.");
			}
	        
	        userDetails = databaseUserDetailsService.loadUserByUser(targetUser);

		} catch (EmptyResultDataAccessException e) {
			logService.logActionAuth(LogService.AUTH_FAILED, username, userIPAddress);
			throw new BadCredentialsException("Utilisateur/Password invalide");
		} catch (EntityNotFoundException e) {
			logService.logActionAuth(LogService.AUTH_FAILED, username, userIPAddress);
			throw new BadCredentialsException("Utilisateur/Password invalide");
		} catch (NonUniqueResultException e) {
			logService.logActionAuth(LogService.AUTH_FAILED, username, userIPAddress);
			throw new BadCredentialsException("Utilisateur non unique, contactez l'administrateur.");
		}

		logService.logActionAuth(LogService.AUTH_SUCCESS, username, userIPAddress);

		return userDetails;
	}

	private Boolean isIpCanBeUsed4AuthAdminManager(String userIPAddress) {
		if (ipsStart4AdminManagerAuthList != null && !ipsStart4AdminManagerAuthList.isEmpty()) {
			for (String ipStart : ipsStart4AdminManagerAuthList) {
				if (userIPAddress.startsWith(ipStart)) {
					return true;
				}
			}
			return false;
		} else {
			// no restrictions is done
			return true;
		}
	}
}
