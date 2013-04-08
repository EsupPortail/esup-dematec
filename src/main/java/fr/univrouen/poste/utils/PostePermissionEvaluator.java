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
package fr.univrouen.poste.utils;

import java.io.Serializable;

import org.springframework.security.access.PermissionEvaluator;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.GrantedAuthorityImpl;

import fr.univrouen.poste.domain.PosteAPourvoir;
import fr.univrouen.poste.domain.PosteCandidature;
import fr.univrouen.poste.domain.User;

public class PostePermissionEvaluator implements PermissionEvaluator {

	@Override
	public boolean hasPermission(Authentication auth, Object targetDomainObject, Object permission) {
		
		
		if(auth.getAuthorities().contains(new GrantedAuthorityImpl("ROLE_ADMIN")) || 
				auth.getAuthorities().contains(new GrantedAuthorityImpl("ROLE_MANAGER")))
			return true;
		
		
		String permissionKey = (String) permission;
		
		if(!"manage".equals(permissionKey) && !"view".equals(permissionKey))
				return false;
		
		if(auth == null || auth.getName() == null || "".equals(auth.getName()))
			return false;
		
        if(!(targetDomainObject instanceof PosteCandidature || targetDomainObject instanceof Long))
        	return false;
        
        String email = auth.getName();
        			
        PosteCandidature pc;
        
        if(targetDomainObject instanceof PosteCandidature) {
        	pc = (PosteCandidature) targetDomainObject;
        }
        else {
        	Long id = (Long) targetDomainObject;
        	pc = PosteCandidature.findPosteCandidature(id);
        }
        
        if(pc != null) {
	        User user = User.findUsersByEmailAddress(email).getSingleResult();
	        if(user.getIsCandidat()) {
	        	if(pc.getCandidat().equals(user))
	        		return true;
	        }  
	        
	        if("view".equals(permissionKey) && user.getIsMembre()) {
	        		PosteAPourvoir poste = pc.getPoste();
	        		return poste.getMembres().contains(user) && pc.getRecevable();
	        }
        }
        
        return false;
        
	}

	@Override
	public boolean hasPermission(Authentication arg0, Serializable arg1,
			String arg2, Object arg3) {
		// TODO Auto-generated method stub
		return false;
	}

}
