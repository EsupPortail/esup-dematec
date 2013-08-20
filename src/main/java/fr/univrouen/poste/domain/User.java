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
package fr.univrouen.poste.domain;

import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Set;

import javax.persistence.Column;
import javax.persistence.ManyToMany;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.persistence.TypedQuery;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.roo.addon.javabean.RooJavaBean;
import org.springframework.roo.addon.jpa.activerecord.RooJpaActiveRecord;
import org.springframework.roo.addon.tostring.RooToString;

@RooJavaBean
@RooToString(excludeFields = "postes")
@RooJpaActiveRecord(finders = { "findUsersByEmailAddress", "findUsersByActivationKeyAndEmailAddress", "findUsersByNumCandidat",  "findUsersByIsAdmin", "findUsersByIsSuperManager", "findUsersByIsManager"}, table="cUser")
public class User {

	private static final int MAX_LOGIN_ATTEMPTS_BEFORE_LOCK = 3;
	
	private static final int MAX_MILISECONDS_LOCK = 10000;
	
    private String civilite;

    private String nom;

    private String prenom;
	
    @NotNull
    @Column(unique = true)
    @Size(min = 1)
    private String emailAddress;

    private String password;

    @Temporal(TemporalType.TIMESTAMP)
    @DateTimeFormat(style = "S-")
    private Date activationDate;

    private String activationKey;

    private Boolean enabled = true;

    private Long loginFailedNb = new Long(0);
    
    private Long loginFailedTime = new Long(0);

    @NotNull
    @Value("false")
    private Boolean isManager;

    @NotNull
    @Value("false")
    private Boolean isSuperManager;
    
    @NotNull
    @Value("false")
    private Boolean isAdmin;
    
	// only for candidat
    private String numCandidat;
    
      
    public Boolean getIsCandidat() {
    	return (numCandidat!=null && !numCandidat.isEmpty());
    }

    
	// only for membre
    @ManyToMany(mappedBy="membres")
    private Set<PosteAPourvoir> postes;
    
    
    public Boolean getIsMembre() {
    	return (postes!=null && !postes.isEmpty());
    }

    // don't care of upper/lower case for authentication with email ...
	public void setEmailAddress(String emailAddress) {
    	this.emailAddress = emailAddress.toLowerCase();
    }
    
    public static long countActifCUsers() {
        return entityManager().createQuery("SELECT COUNT(o) FROM User o WHERE o.enabled=true", Long.class).getSingleResult();
    }
    
    public String getStatus() {
    	String status = "";
    	if(this.getIsAdmin())
    		status = status.concat("admin, ");
    	if(this.getIsSuperManager())
    		status = status.concat("super-manager, ");
    	if(this.getIsManager())
    		status = status.concat("manager, ");
    	if(this.getIsMembre())
    		status = status.concat("membre, ");
    	if(this.getIsCandidat())
    		status = status.concat("candidat, ");
    	return status;
    }

	public static Long countAdmins() {
		return countFindUsersByIsAdmin(true);
    }

	public static Long countSupermanagers() {
		return countFindUsersByIsSuperManager(true);
	}

	public static Long countManagers() {
		return countFindUsersByIsManager(true);
	}

	public static Long countMembres() {
		return entityManager().createQuery("SELECT COUNT(o) FROM User o WHERE o.postes is not EMPTY", Long.class).getSingleResult();
    }
	
	public static Long countCandidats() {
		return entityManager().createQuery("SELECT COUNT(o) FROM User o WHERE o.numCandidat is not NULL AND o.numCandidat <> ''", Long.class).getSingleResult();
	}
	
	public static Long countActifCandidats() {
		return entityManager().createQuery("SELECT COUNT(o) FROM User o WHERE o.numCandidat is not NULL AND o.numCandidat <> '' AND o.activationDate is not NULL", Long.class).getSingleResult();
    }

    public static TypedQuery<User> findAllCandidats() {
    	return entityManager().createQuery("SELECT o FROM User AS o WHERE o.numCandidat is not NULL AND o.numCandidat <> '' order by o.emailAddress", User.class);
    }
    
    public static TypedQuery<User> findAllMembres() {
        return entityManager().createQuery("SELECT o FROM User AS o WHERE o.postes is not EMPTY order by o.emailAddress", User.class);
    }

	public static Object findAllNoCandidats() {
        return entityManager().createQuery("SELECT o FROM User AS o WHERE o.numCandidat is NULL OR o.numCandidat = '' order by o.emailAddress", User.class).getResultList();
    }

    public static List<User> findUserEntries(int firstResult, int maxResults) {
        return entityManager().createQuery("SELECT o FROM User o order by o.emailAddress", User.class).setFirstResult(firstResult).setMaxResults(maxResults).getResultList();
    }

	public void reportLoginFailure() {
		if(!isLocked() && ++loginFailedNb >= MAX_LOGIN_ATTEMPTS_BEFORE_LOCK) {
	        Calendar cal = Calendar.getInstance();
	        Date currentTime = cal.getTime();      
			loginFailedTime = currentTime.getTime();
		}
	}

	public void reportLoginOK() {
		loginFailedNb = new Long(0);
		loginFailedTime = new Long(0);
	}

	public Boolean isLocked() {
        Calendar cal = Calendar.getInstance();
        Date currentTime = cal.getTime();      
		Long currentTimeMS = currentTime.getTime();
		if(currentTimeMS < loginFailedTime + MAX_MILISECONDS_LOCK) {
			loginFailedNb = new Long(0);
			return true;
		}
		return false;
	}

	/**
	 * @return true if one of his candidatures has a modification date not nul
	 */
	public Boolean isCandidatActif() {
		List<PosteCandidature> candidatures = PosteCandidature.findPosteCandidaturesByCandidat(this, null, null).getResultList();
		for(PosteCandidature candidature: candidatures)
			if(candidature.getModification() != null)
				return true;
		return false;
	}
	
}

