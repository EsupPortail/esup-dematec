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
import java.util.Date;

import org.springframework.security.access.PermissionEvaluator;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.GrantedAuthorityImpl;

import fr.univrouen.poste.domain.AppliConfig;
import fr.univrouen.poste.domain.MemberReviewFile;
import fr.univrouen.poste.domain.PosteAPourvoir;
import fr.univrouen.poste.domain.PosteCandidature;
import fr.univrouen.poste.domain.PosteCandidatureFile;
import fr.univrouen.poste.domain.User;

public class PostePermissionEvaluator implements PermissionEvaluator {

	@Override
	public boolean hasPermission(Authentication auth, Object targetDomainObject, Object permission) {
		
		
		if(auth.getAuthorities().contains(new GrantedAuthorityImpl("ROLE_ADMIN")) || 
				auth.getAuthorities().contains(new GrantedAuthorityImpl("ROLE_MANAGER")))
			return true;
		
		
		String permissionKey = (String) permission;

		if(auth == null || auth.getName() == null || "".equals(auth.getName()))
			return false;
		
        if(!(targetDomainObject instanceof PosteCandidature || targetDomainObject instanceof Long))
        	return false;
        
        String email = auth.getName();
		
		if("delFile".equals(permissionKey)) {
        	Long id = (Long) targetDomainObject;
        	PosteCandidatureFile pcFile = PosteCandidatureFile.findPosteCandidatureFile(id);
			return pcFile.getWriteable();
		}
		
		if("delMemberReviewFile".equals(permissionKey)) {
        	Long id = (Long) targetDomainObject;
        	MemberReviewFile reviewFile = MemberReviewFile.findMemberReviewFile(id);
        	User user = User.findUsersByEmailAddress(email, null, null).getSingleResult();
			return reviewFile.getMember().equals(user);
		}
		
		if(!"manage".equals(permissionKey) && !"view".equals(permissionKey) && !"review".equals(permissionKey))
				return false;
        
        PosteCandidature pc;
        
        if(targetDomainObject instanceof PosteCandidature) {
        	pc = (PosteCandidature) targetDomainObject;
        }
        else {
        	Long id = (Long) targetDomainObject;
        	pc = PosteCandidature.findPosteCandidature(id);
        }
        
        if(pc != null) {
	        User user = User.findUsersByEmailAddress(email, null, null).getSingleResult();
	        
	        if("review".equals(permissionKey)) {
        		PosteAPourvoir poste = pc.getPoste();
        		return user.getIsAdmin() || user.getIsManager() || user.getIsMembre() && poste.getMembres().contains(user) && pc.getRecevable();
	        }
	        
	        if(user.getIsCandidat()) {
	        	if(pc.getCandidat().equals(user)) {
	        		// restrictions si phase auditionnable
	        		Date currentTime = new Date();     
	    			if(currentTime.compareTo(AppliConfig.getCacheDateEndCandidat()) > 0 || 
	    				currentTime.compareTo(AppliConfig.getCacheDateEndCandidatActif()) > 0) {
	    				return pc.getAuditionnable();
	    			} else {
	    				return true;
	    			}
	        	}
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
