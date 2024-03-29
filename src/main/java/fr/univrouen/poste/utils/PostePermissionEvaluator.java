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
import java.util.Set;

import org.springframework.security.access.PermissionEvaluator;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.AuthorityUtils;

import fr.univrouen.poste.domain.AppliConfig;
import fr.univrouen.poste.domain.MemberReviewFile;
import fr.univrouen.poste.domain.PosteAPourvoir;
import fr.univrouen.poste.domain.PosteCandidature;
import fr.univrouen.poste.domain.PosteCandidatureFile;
import fr.univrouen.poste.domain.User;

public class PostePermissionEvaluator implements PermissionEvaluator {

	@Override
	public boolean hasPermission(Authentication auth, Object targetDomainObject, Object permission) {
		
		Set<String> roles = AuthorityUtils.authorityListToSet(auth.getAuthorities());
		
		if(roles.contains("ROLE_ADMIN") || 
				roles.contains("ROLE_MANAGER"))
			return true;
		
		boolean isMembre = roles.contains("ROLE_MEMBRE");
		boolean isCandidat = roles.contains("ROLE_CANDIDAT");
		
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
			Boolean confSupprReviewFile = AppliConfig.getCacheMembreSupprReviewFile();
			if(!confSupprReviewFile) {
				return false;
			}
        	Long id = (Long) targetDomainObject;
        	MemberReviewFile reviewFile = MemberReviewFile.findMemberReviewFile(id);
        	User user = User.findUsersByEmailAddress(email, null, null).getSingleResult();
			return reviewFile.getMember().equals(user);
		}
		
		if("manageReporters".equals(permissionKey)) {
			Long id = (Long) targetDomainObject;
        	PosteCandidature pc = PosteCandidature.findPosteCandidature(id);
        	User user = User.findUsersByEmailAddress(email, null, null).getSingleResult();
			return pc.getPoste().getPresidents()!=null && pc.getPoste().getPresidents().contains(user);
		}
		
		if("viewposte".equals(permissionKey)) {
			Long id = (Long) targetDomainObject;
			PosteAPourvoir posteAPourvoir = PosteAPourvoir.findPosteAPourvoir(id);
        	User user = User.findUsersByEmailAddress(email, null, null).getSingleResult();
			return posteAPourvoir!= null && posteAPourvoir.getMembres()!=null &&  posteAPourvoir.getMembres().contains(user);
		}
		
		if("manageposte".equals(permissionKey)) {
			Long id = (Long) targetDomainObject;
			PosteAPourvoir posteAPourvoir = PosteAPourvoir.findPosteAPourvoir(id);
        	User user = User.findUsersByEmailAddress(email, null, null).getSingleResult();
			return posteAPourvoir!= null && posteAPourvoir.getPresidents()!=null &&  posteAPourvoir.getPresidents().contains(user);
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
        		return user.getIsAdmin() || user.getIsManager() || user.getIsMembre() && poste.getMembres().contains(user) && pc.isRecevable();
	        }
	        
	        if(isCandidat && pc.getCandidat().equals(user)) {
	        	
	        	if(AppliConfig.getCacheCandidatCanSignup()) {
	        		Date currentTime = new Date();
	        		if((pc.getAuditionnable() || (pc.getPoste().getDateEndSignupCandidat() == null || currentTime.compareTo(pc.getPoste().getDateEndSignupCandidat()) > 0)) && 
	        				(!pc.getAuditionnable() || (pc.getPoste().getDateEndCandidatAuditionnable() == null || currentTime.compareTo(pc.getPoste().getDateEndCandidatAuditionnable()) > 0))) {
	        			return false;
	        		} else {
	        			return true;
	        		}
	        	} else {
	        	
		        	if(pc.getCandidat().equals(user)) {
		        		// restrictions si phase auditionnable
		        		Date currentTime = new Date();     
		    			if(currentTime.compareTo(AppliConfig.getCacheDateEndCandidat()) > 0 && 
		    				currentTime.compareTo(AppliConfig.getCacheDateEndCandidatActif()) > 0) {
		    				return pc.getAuditionnable() && currentTime.compareTo(pc.getPoste().getDateEndCandidatAuditionnable()) < 0;
		    			} else {
		    				return true;
		    			}
		        	}
	        	}
	        }  
	        
	        if("view".equals(permissionKey) && isMembre) {
	        		PosteAPourvoir poste = pc.getPoste();
	        		return poste.getMembres().contains(user) && pc.isRecevable();
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
