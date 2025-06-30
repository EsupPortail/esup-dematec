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
import lombok.Getter;
import lombok.Setter;
import org.apache.commons.lang3.builder.ReflectionToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import org.springframework.format.annotation.DateTimeFormat;

import java.util.*;

@Entity
@Getter
@Setter
public class PosteAPourvoir {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "id")
    Long id;

	@Column(nullable = false, unique = true)
	String numEmploi;

    @Column(length=300)
    String profil;

    @Column(length=300)
    String localisation;

    @ManyToMany
    @JoinTable(
        name = "posteapourvoir_user",
        joinColumns = @JoinColumn(name = "posteapourvoir"),
        inverseJoinColumns = @JoinColumn(name = "membres")
    )
    Set<User> membres;

    @ManyToMany
    @JoinTable(
        name = "posteapourvoir_user_1",
        joinColumns = @JoinColumn(name = "posteapourvoir"),
        inverseJoinColumns = @JoinColumn(name = "presidents")
    )
    Set<User> presidents;

    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinColumn(name = "posteapourvoir_postefiles")
    @OrderBy("sendTime DESC")
    Set<PosteAPourvoirFile> posteFiles = new HashSet<PosteAPourvoirFile>();
    
    public List<User> getSortedMembres() {
    	List<User> sortedMembres = new ArrayList<User>(this.membres);
    	Collections.sort(sortedMembres, new UserComparator());
    	return sortedMembres;
    }
    
    public List<User> getSortedPresidents() {
    	List<User> sortedPresidents = new ArrayList<User>(this.presidents);
    	Collections.sort(sortedPresidents, new UserComparator());
    	return sortedPresidents;
    }
    
    @Temporal(TemporalType.TIMESTAMP)
    @DateTimeFormat(pattern = "dd/MM/yyyy HH:mm")
    Date dateEndCandidatAuditionnable;

    @Temporal(TemporalType.TIMESTAMP)
    @DateTimeFormat(pattern = "dd/MM/yyyy HH:mm")
    Date dateEndSignupCandidat;
    
    public void setNumEmploi(String numEmploi) {
    	if(numEmploi != null) {
    		numEmploi = numEmploi.trim();
    	}
        this.numEmploi = numEmploi;
    }

	public String toString() {
        return new ReflectionToStringBuilder(this, ToStringStyle.SHORT_PREFIX_STYLE).setExcludeFieldNames("membres").toString();
    }

}
