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

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;
import org.apache.commons.lang3.builder.ReflectionToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import org.apache.commons.lang3.text.WordUtils;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDateTime;
import java.util.Set;

@Entity
@Table(name = "cUser")
@Getter
@Setter
public class User {

	static final int MAX_LOGIN_ATTEMPTS_BEFORE_LOCK = 3;
	
	static final int MAX_MILISECONDS_LOCK = 10000;

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "my_seq")
    @SequenceGenerator(
            name = "my_seq",
            sequenceName = "hibernate_sequence",
            allocationSize = 1
    )
    Long id;

	
    String civilite = "";

    String nom = "";

    String prenom = "";
	
    @NotNull
    @Column(unique = true)
    @Size(min = 1)
    String emailAddress;

    String password;

    @DateTimeFormat(style = "S-")
    LocalDateTime activationDate;

    String activationKey;

    
    Boolean enabled = true;

    Long loginFailedNb = Long.valueOf(0);
    
    Long loginFailedTime = Long.valueOf(0);

    @NotNull
    Boolean isManager = false;

    @NotNull
    Boolean isSuperManager = false;
    
    @NotNull
    Boolean isAdmin = false;
    
	// only for candidat
    String numCandidat;
    
	// only for membre
    @ManyToMany(mappedBy="membres")
    Set<PosteAPourvoir> postes;
    
      
    public Boolean getIsCandidat() {
    	return (numCandidat!=null && !numCandidat.isEmpty());
    }

    public Boolean getIsMembre() {
    	return (postes!=null && !postes.isEmpty());
    }
    
    public String getDisplayName() {
    	return WordUtils.capitalizeFully(prenom + " " + nom);
    }

    // don't care of upper/lower case for authentication with email ...
	public void setEmailAddress(String emailAddress) {
    	this.emailAddress = emailAddress.toLowerCase();
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

	
	public void reportLoginFailure() {
		if(!isLocked() && ++loginFailedNb >= MAX_LOGIN_ATTEMPTS_BEFORE_LOCK) {
	        
			loginFailedTime = System.currentTimeMillis();
		}
	}

	public void reportLoginOK() {
		loginFailedNb = Long.valueOf(0);
		loginFailedTime = Long.valueOf(0);
	}

	public Boolean isLocked() {
        
		Long currentTimeMS = System.currentTimeMillis();
		if(currentTimeMS < loginFailedTime + MAX_MILISECONDS_LOCK) {
			loginFailedNb = Long.valueOf(0);
			return true;
		}
		return false;
	}

	public String toString() {
        return new ReflectionToStringBuilder(this, ToStringStyle.SHORT_PREFIX_STYLE).setExcludeFieldNames("postes").toString();
    }
}

