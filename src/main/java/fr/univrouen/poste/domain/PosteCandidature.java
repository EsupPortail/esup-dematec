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

import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.persistence.CascadeType;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.OrderBy;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.persistence.TypedQuery;
import javax.validation.constraints.NotNull;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.roo.addon.javabean.RooJavaBean;
import org.springframework.roo.addon.jpa.activerecord.RooJpaActiveRecord;
import org.springframework.roo.addon.tostring.RooToString;

import fr.univrouen.poste.domain.ManagerReview.ReviewStatusTypes;

@RooJavaBean
@RooToString(excludeFields = "candidatureFiles")
@RooJpaActiveRecord(finders = { "findPosteCandidaturesByCandidat", "findPosteCandidaturesByPoste", "findPosteCandidaturesByRecevable", "findPosteCandidaturesByAuditionnable" })
public class PosteCandidature {

    public static final List<String> fieldNames4OrderClauseFilter = java.util.Arrays.asList("creation", "modification", "poste", "candidatureFiles", "candidat", "recevable", "o.poste.numEmploi,o.candidat.nom", "candidat.nom", "candidat.emailAddress");

    @Temporal(TemporalType.TIMESTAMP)
    @DateTimeFormat(pattern = "dd/MM/yyyy HH:mm")
    private Date creation;

    @Temporal(TemporalType.TIMESTAMP)
    @DateTimeFormat(pattern = "dd/MM/yyyy HH:mm")
    private Date modification;

    @NotNull
    @ManyToOne
    private PosteAPourvoir poste;

    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("sendTime DESC")
    private Set<PosteCandidatureFile> candidatureFiles = new HashSet<PosteCandidatureFile>();

    @NotNull
    @ManyToOne
    private User candidat;

    private Boolean recevable = true;
    
    private Boolean auditionnable = false;
    
    private ManagerReview managerReview;
    
    public String getNom() {
        return this.candidat.getNom();
    }

    public String getPrenom() {
        return this.candidat.getPrenom();
    }

    public String getEmail() {
        return this.candidat.getEmailAddress();
    }
    
    public ReviewStatusTypes getManagerReviewState() {
    	if(managerReview == null) {
    		return ReviewStatusTypes.Non_vue;
    	} else {
    		if(modification.compareTo(managerReview.getReviewDate()) > 0) {
    			return ReviewStatusTypes.Vue_mais_modifie_depuis;
    		} else {
    			return ReviewStatusTypes.Vue;
    		}
    	}
    }

    public static TypedQuery<fr.univrouen.poste.domain.PosteCandidature> findPosteCandidaturesRecevableByPostes(Set<fr.univrouen.poste.domain.PosteAPourvoir> postes) {
        if (postes == null) throw new IllegalArgumentException("The postes argument is required");
        TypedQuery<PosteCandidature> q = entityManager().createQuery("SELECT o FROM PosteCandidature AS o WHERE o.poste IN :postes AND o.recevable = TRUE ORDER BY o.poste.numEmploi, o.candidat.nom", PosteCandidature.class);
        q.setParameter("postes", postes);
        return q;
    }

    public static Long countPosteActifCandidatures() {
        return entityManager().createQuery("SELECT COUNT(o) FROM PosteCandidature o WHERE o.modification is not NULL", Long.class).getSingleResult();
    }

    public static Long countFindPosteCandidaturesByPostes(List<fr.univrouen.poste.domain.PosteAPourvoir> postes) {
    	TypedQuery<Long> q = entityManager().createQuery("SELECT count(o) FROM PosteCandidature AS o WHERE o.poste IN :postes", Long.class);
    	q.setParameter("postes", postes);
    	return q.getSingleResult();
    }
    
    public static TypedQuery<fr.univrouen.poste.domain.PosteCandidature> findPosteCandidaturesByPostes(List<fr.univrouen.poste.domain.PosteAPourvoir> postes, String sortFieldName, String sortOrder) {
    	String jpaQuery = "SELECT o FROM PosteCandidature AS o WHERE o.poste IN :postes";
        if (fieldNames4OrderClauseFilter.contains(sortFieldName)) {
            jpaQuery = jpaQuery + " ORDER BY " + sortFieldName;
            if ("ASC".equalsIgnoreCase(sortOrder) || "DESC".equalsIgnoreCase(sortOrder)) {
                jpaQuery = jpaQuery + " " + sortOrder;
            }
        }   
        TypedQuery<PosteCandidature> q = entityManager().createQuery(jpaQuery, PosteCandidature.class);
        q.setParameter("postes", postes);
        return q;
    }
    
    public static Long countFindPosteCandidaturesByCandidats(List<fr.univrouen.poste.domain.User> candidats) {
    	TypedQuery<Long> q = entityManager().createQuery("SELECT COUNT(o) FROM PosteCandidature AS o WHERE o.candidat IN :candidats", Long.class);
    	q.setParameter("candidats", candidats);
    	return q.getSingleResult();
    }
    
    public static TypedQuery<fr.univrouen.poste.domain.PosteCandidature> findPosteCandidaturesByCandidats(List<fr.univrouen.poste.domain.User> candidats, String sortFieldName, String sortOrder) {       
        String jpaQuery = "SELECT o FROM PosteCandidature AS o WHERE o.candidat IN :candidats";
        if (fieldNames4OrderClauseFilter.contains(sortFieldName)) {
            jpaQuery = jpaQuery + " ORDER BY " + sortFieldName;
            if ("ASC".equalsIgnoreCase(sortOrder) || "DESC".equalsIgnoreCase(sortOrder)) {
                jpaQuery = jpaQuery + " " + sortOrder;
            }
        }   
        TypedQuery<PosteCandidature> q = entityManager().createQuery(jpaQuery, PosteCandidature.class);
        q.setParameter("candidats", candidats);
        return q;
    }
    
    


}
