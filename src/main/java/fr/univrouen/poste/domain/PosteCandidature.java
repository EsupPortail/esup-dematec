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

import fr.univrouen.poste.domain.ManagerReview.ReviewStatusTypes;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
import org.apache.commons.collections4.map.HashedMap;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDateTime;
import java.util.*;

@Entity
@Getter
@Setter
public class PosteCandidature {

    public enum RecevableEnum {
        RECEVABLE, NON_RECEVABLE, NON_DEFINI
    }

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "my_seq")
    @SequenceGenerator(
            name = "my_seq",
            sequenceName = "hibernate_sequence",
            allocationSize = 1
    )
    Long id;


    @DateTimeFormat(pattern = "dd/MM/yyyy HH:mm")
    LocalDateTime creation;

    @DateTimeFormat(pattern = "dd/MM/yyyy HH:mm")
    LocalDateTime modification;

    @NotNull
    @ManyToOne
    @JoinColumn(name = "poste")
    PosteAPourvoir poste;

    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinColumn(name = "postecandidature_candidaturefiles")
    @OrderBy("sendTime DESC")
    Set<PosteCandidatureFile> candidatureFiles = new HashSet<PosteCandidatureFile>();

    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinColumn(name = "postecandidature_memberreviewfiles")
    @OrderBy("sendTime DESC")
    Set<MemberReviewFile> memberReviewFiles = new HashSet<MemberReviewFile>();

    @NotNull
    @ManyToOne
    @JoinColumn(name = "candidat")
    User candidat;

    @Column
    @Enumerated(EnumType.STRING)
    RecevableEnum recevableEnum = RecevableEnum.RECEVABLE;

    
    Boolean auditionnable = false;
    
    
    Boolean laureat = false;

    @Column(name = "manager_comment4members", columnDefinition = "TEXT")
    String managerComment4Members = "";

    @NotNull
    @OneToOne(cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinColumn(name = "manager_review")
    ManagerReview managerReview = new ManagerReview();

    
    @OneToOne (mappedBy="candidature", fetch=FetchType.LAZY)
    GalaxieEntry galaxieEntry;
    
    @ManyToMany
    @JoinTable(
        name = "postecandidature_user",
        joinColumns = @JoinColumn(name = "postecandidature"),
        inverseJoinColumns = @JoinColumn(name = "reporters")
    )
    Set<User> reporters;
    
    @Transient
    Boolean reporterTag = false;
    
    @ElementCollection
    @CollectionTable(name = "postecandidature_postcandidaturetagvalue",
        joinColumns = @JoinColumn(name = "postecandidature"))
    @MapKeyJoinColumn(name = "tags_key")
    @Column(name = "tags")
    Map<PosteCandidatureTag, PosteCandidatureTagValue> tags = new HashedMap<PosteCandidatureTag, PosteCandidatureTagValue>();

	public boolean isRecevable() {
		return RecevableEnum.RECEVABLE.equals(recevableEnum);
	}
    
    public String getNom() {
        return this.candidat.getNom();
    }

    public String getPrenom() {
        return this.candidat.getPrenom();
    }

    public String getEmail() {
        return this.candidat.getEmailAddress();
    }
    
    public String getNumCandidat() {
        return this.candidat.getNumCandidat();
    }

    public String getManagerReviewState() {
        return managerReview.getReviewStatus().toString();
    }

    public String getTagsAsHtmlString() {
        String tagsAsString = "";
        for(PosteCandidatureTag tag : tags.keySet()) {
            if(tags.get(tag) != null) {
                tagsAsString += String.format("<span class=\"important\">%s : </span> %s<br/>", tag.getName(), tags.get(tag).getValue());
            }
        }
        return tagsAsString;
    }

    public Map<String, String> getMapFields() {
    	Map<String, String> mapStrings = new HashMap<String, String>();
    	mapStrings.put("candidature_candidat_nom", this.getCandidat().getNom());
    	mapStrings.put("candidature_candidat_prenom", this.getCandidat().getPrenom());
    	mapStrings.put("candidature_candidat_emailAddress", this.getCandidat().getEmailAddress());
    	mapStrings.put("candidature_galaxieEntryEtatDossier", this.getGalaxieEntryEtatDossier());
    	mapStrings.put("candidature_managerReviewState", this.getManagerReviewState());
    	mapStrings.put("candidature_candidat_numCandidat", this.getCandidat().getNumCandidat());
    	mapStrings.put("candidature_poste_localisation", this.getPoste().getLocalisation());
    	mapStrings.put("candidature_poste_numEmploi", this.getPoste().getNumEmploi());
    	mapStrings.put("candidature_poste_profil", this.getPoste().getProfil());
    	mapStrings.put("candidature_auditionnable", this.getAuditionnable() ? "AUDITIONNABLE" : "NON_AUDITIONNABLE");
    	mapStrings.put("candidature_recevable", this.getRecevableEnum().toString());
    	for(PosteCandidatureTag tag : this.getTags().keySet()) {
    		String tagValue = this.getTags().get(tag) != null ? this.getTags().get(tag).getValue() : "";
    		mapStrings.put(String.format("candidature_tag_%s", tag.getCleanName()), tagValue);
    	}
    	return mapStrings;
    }
    
    public List<User> getSortedReporters() {
    	List<User> sortedReporters = new ArrayList<User>(this.reporters);
    	Collections.sort(sortedReporters, new UserComparator());
    	return sortedReporters;
    }
    
    public void setModification(LocalDateTime modification) {
        if (ReviewStatusTypes.Vue.equals(managerReview.getReviewStatus())) {
            managerReview.setReviewStatus(ReviewStatusTypes.Vue_mais_modifie_depuis);
        } else if (ReviewStatusTypes.Vue_incomplet.equals(managerReview.getReviewStatus())) {
            managerReview.setReviewStatus(ReviewStatusTypes.Vue_incomplet_mais_modifie_depuis);
        }
        this.modification = modification;
    }

    public String getGalaxieEntryEtatDossier() {
    	String etatDossier = "";
    	if(this.getGalaxieEntry() != null) {
    		etatDossier = this.getGalaxieEntry().getEtatDossier();
    	}
    	return etatDossier;
    }

    public List<PosteCandidatureFile> getSortedCandidatureFiles() {
        List<PosteCandidatureFile> sortedCandidatureFiles = new ArrayList<PosteCandidatureFile>(this.candidatureFiles);
        Collections.sort(sortedCandidatureFiles, new PosteCandidatureFileComparator());
        return sortedCandidatureFiles;
    }

}
