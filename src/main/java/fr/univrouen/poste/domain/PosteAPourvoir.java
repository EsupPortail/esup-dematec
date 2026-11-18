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

import java.time.LocalDateTime;
import java.util.*;

@Entity
@Getter
@Setter
public class PosteAPourvoir {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "my_seq")
    @SequenceGenerator(name = "my_seq", sequenceName = "hibernate_sequence", allocationSize = 1)
    @Column(name = "id")
    Long id;

    @Version
    Integer version;

	@Column(nullable = false, unique = true)
	String numEmploi;

    @Column(length=300)
    String profil;

    @Column(length=300)
    String localisation;

    @ManyToMany
    @JoinTable(
        name = "posteapourvoir_membres",
        joinColumns = @JoinColumn(name = "postes"),
        inverseJoinColumns = @JoinColumn(name = "membres")
    )
    Set<User> membres;

    @ManyToMany
    @JoinTable(
        name = "posteapourvoir_presidents",
        joinColumns = @JoinColumn(name = "posteapourvoir"),
        inverseJoinColumns = @JoinColumn(name = "presidents")
    )
    Set<User> presidents;

    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinTable(name = "posteapourvoir_poste_files",
        joinColumns = @JoinColumn(name = "posteapourvoir"),
        inverseJoinColumns = @JoinColumn(name = "poste_files", unique = true))
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
    
    @FormDateTime
    LocalDateTime dateEndCandidatAuditionnable;

    @FormDateTime
    LocalDateTime dateEndSignupCandidat;

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
