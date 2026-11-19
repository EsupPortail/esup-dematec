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

import fr.univrouen.poste.dao.*;
import fr.univrouen.poste.domain.*;
import fr.univrouen.poste.services.AppliConfigService;
import jakarta.annotation.Resource;
import org.springframework.security.access.PermissionEvaluator;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.stereotype.Service;

import java.io.Serializable;
import java.time.Clock;
import java.time.LocalDateTime;
import java.util.Set;

@Service
public class PostePermissionEvaluator implements PermissionEvaluator {

    @Resource
    Clock clock;

    @Resource
    DateClotureChecker dateClotureChecker;

	@Resource
	AppliConfigService appliConfigService;

	@Resource
	UserDao userDao;

	@Resource
	PosteCandidatureDao posteCandidatureDao;

	@Resource
	PosteAPourvoirDao posteAPourvoirDao;

	@Resource
	PosteCandidatureFileDao posteCandidatureFileDao;

	@Resource
	MemberReviewFileDao memberReviewFileDao;

	@Override
	public boolean hasPermission(Authentication auth, Object targetDomainObject, Object permission) {
		
		if (auth == null || !auth.isAuthenticated() || auth.getName() == null || auth.getName().isEmpty()) {
			return false;
		}

		Set<String> roles = AuthorityUtils.authorityListToSet(auth.getAuthorities());
		
		if(roles.contains("ROLE_ADMIN") || 
				roles.contains("ROLE_MANAGER"))
			return true;
		
		boolean isMembre = roles.contains("ROLE_MEMBRE");
		boolean isCandidat = roles.contains("ROLE_CANDIDAT");
		
		String permissionKey = (String) permission;

		
        if(!(targetDomainObject instanceof PosteCandidature || targetDomainObject instanceof Long))
        	return false;
        
        String email = auth.getName();
		
		if("delFile".equals(permissionKey)) {
        	Long id = (Long) targetDomainObject;
        	PosteCandidatureFile pcFile = posteCandidatureFileDao.findPosteCandidatureFile(id);
			return pcFile != null && Boolean.TRUE.equals(pcFile.getWriteable());
		}
		
		if("delMemberReviewFile".equals(permissionKey)) {
			Boolean confSupprReviewFile = appliConfigService.getCacheMembreSupprReviewFile();
			if(!confSupprReviewFile) {
				return false;
			}
        	Long id = (Long) targetDomainObject;
        	MemberReviewFile reviewFile = memberReviewFileDao.findMemberReviewFile(id);
        	User user = userDao.findUsersByEmailAddress(email);
			return reviewFile != null && user != null && user.equals(reviewFile.getMember());
		}
		
		if("manageReporters".equals(permissionKey)) {
			Long id = (Long) targetDomainObject;
        	PosteCandidature pc = posteCandidatureDao.findPosteCandidature(id);
        	User user = userDao.findUsersByEmailAddress(email);
			return pc != null && pc.getPoste().getPresidents()!=null && pc.getPoste().getPresidents().contains(user);
		}
		
		if("viewposte".equals(permissionKey)) {
			Long id = (Long) targetDomainObject;
			PosteAPourvoir posteAPourvoir = posteAPourvoirDao.findPosteAPourvoir(id);
        	User user = userDao.findUsersByEmailAddress(email);
			return posteAPourvoir!= null && posteAPourvoir.getMembres()!=null &&  posteAPourvoir.getMembres().contains(user);
		}
		
		if("manageposte".equals(permissionKey)) {
			Long id = (Long) targetDomainObject;
			PosteAPourvoir posteAPourvoir = posteAPourvoirDao.findPosteAPourvoir(id);
        	User user = userDao.findUsersByEmailAddress(email);
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
        	pc = posteCandidatureDao.findPosteCandidature(id);
        }
        
        if(pc != null) {
	        User user = userDao.findUsersByEmailAddress(email);
	        if (user == null) return false;
	        
	        if("review".equals(permissionKey)) {
        		PosteAPourvoir poste = pc.getPoste();
        		return user.getIsAdmin() || user.getIsManager() || user.getIsMembre() && poste.getMembres().contains(user) && pc.isRecevable();
	        }
	        
            if (isCandidat && pc.getCandidat().equals(user)) {
                AppliConfig config = appliConfigService.getConfig();
                boolean localMode = config != null && Boolean.TRUE.equals(config.getCandidatCanSignup());
                LocalDateTime deadline = localMode || pc.getAuditionnable()
                        ? null : dateClotureChecker.getCandidateDeadline(user, config);
                return dateClotureChecker.isApplicationOpen(pc, localMode, deadline, LocalDateTime.now(clock));
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
