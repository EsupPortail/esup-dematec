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
import java.util.List;

import javax.persistence.TypedQuery;

import org.apache.log4j.Logger;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.GrantedAuthorityImpl;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import fr.univrouen.poste.domain.User;


@Service
@Transactional
public class DatabaseUserDetailsService implements UserDetailsService {

	private final Logger logger = Logger.getLogger(getClass());

	@Override
	public UserDetails loadUserByUsername(String username)
			throws UsernameNotFoundException {

		username = username.toLowerCase();

		TypedQuery<User> query = User.findUsersByEmailAddress(username, null, null);

		try {
			User targetUser = (User) query.getSingleResult();		
			return loadUserByUser(targetUser);
		} catch(EmptyResultDataAccessException e) {
			throw new RuntimeException(username + " not found in the Database.", e);
		}				
	}

	public UserDetails loadUserByUser(User targetUser)
			throws UsernameNotFoundException {

		List<GrantedAuthority> authorities = new ArrayList<GrantedAuthority>();
		Boolean enabled;
		
		// Roles
		if (targetUser.getIsAdmin()) {
			authorities.add(new GrantedAuthorityImpl("ROLE_ADMIN"));
		}
		if (targetUser.getIsCandidat()) {
			// TODO : check date
			authorities.add(new GrantedAuthorityImpl("ROLE_CANDIDAT"));
		}
		if (targetUser.getIsManager()) {
			authorities.add(new GrantedAuthorityImpl("ROLE_MANAGER"));
		}
		if (targetUser.getIsSuperManager()) {
			authorities.add(new GrantedAuthorityImpl("ROLE_MANAGER"));
			authorities.add(new GrantedAuthorityImpl("ROLE_SUPER_MANAGER"));
		}
		if (targetUser.getIsMembre()) {
			// TODO : check date
			authorities.add(new GrantedAuthorityImpl("ROLE_MEMBRE"));
		}

		// Enabled
		enabled = targetUser.getEnabled();

		return new org.springframework.security.core.userdetails.User(targetUser.getEmailAddress(), targetUser.getPassword() == null ? "dummy" : targetUser.getPassword(), enabled, // enabled
				true, // account not expired
				true, // credentials not expired
				true, // account not locked
				authorities);

	}

}
