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
import javax.persistence.EntityManager;
import javax.persistence.FetchType;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import javax.persistence.OrderBy;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.persistence.TypedQuery;
import javax.validation.constraints.NotNull;

import org.hibernate.annotations.Type;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.roo.addon.javabean.RooJavaBean;
import org.springframework.roo.addon.jpa.activerecord.RooJpaActiveRecord;
import org.springframework.roo.addon.tostring.RooToString;

import fr.univrouen.poste.domain.ManagerReview.ReviewStatusTypes;

@RooJavaBean
@RooToString(excludeFields = {"candidatureFiles", "memberReviewFiles"})
@RooJpaActiveRecord(finders = { "findPosteCandidaturesByCandidat"})
public class PosteCandidature {

    public static final List<String> fieldNames4OrderClauseFilter = java.util.Arrays.asList("creation", "modification", "poste", "candidatureFiles", "candidat", "recevable", "o.poste.numEmploi,o.candidat.nom", "candidat.nom", "candidat.emailAddress", "managerReview.reviewStatus", "managerReview.manager", "managerReview.reviewDate", "candidat.numCandidat", "galaxieEntry.etatDossier");

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

    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("sendTime DESC")
    private Set<MemberReviewFile> memberReviewFiles = new HashSet<MemberReviewFile>();

    @NotNull
    @ManyToOne
    private User candidat;

    private Boolean recevable = true;

    private Boolean auditionnable = false;
    
    @Type(type="org.hibernate.type.StringClobType")
    private String managerComment4Members = "";

    @NotNull
    @OneToOne(cascade = CascadeType.ALL, orphanRemoval = true)
    private ManagerReview managerReview = new ManagerReview();
    
    
    @OneToOne (mappedBy="candidature", fetch=FetchType.LAZY)
    private GalaxieEntry galaxieEntry;

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

    public String getManagerReviewStateColor() {
        return ManagerReviewLegendColor.getColor(managerReview.getReviewStatus());
    }

    public void setModification(Date modification) {
        if (ReviewStatusTypes.Vue.equals(managerReview.getReviewStatus())) {
            managerReview.setReviewStatus(ReviewStatusTypes.Vue_mais_modifie_depuis);
        } else if (ReviewStatusTypes.Vue_incomplet.equals(managerReview.getReviewStatus())) {
            managerReview.setReviewStatus(ReviewStatusTypes.Vue_incomplet_mais_modifie_depuis);
        }
        this.modification = modification;
    }

    public static Long countPosteActifCandidatures() {
        return entityManager().createQuery("SELECT COUNT(o) FROM PosteCandidature o WHERE o.modification is not NULL", Long.class).getSingleResult();
    }

    public static TypedQuery<PosteCandidature> findPosteCandidaturesRecevableByPostes(Set<PosteAPourvoir> postes, String sortFieldName, String sortOrder) {
        if (postes == null) throw new IllegalArgumentException("The postes argument is required");
        String jpaQuery = "SELECT o FROM PosteCandidature AS o WHERE o.poste IN :postes AND o.recevable = TRUE";
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

    
    public static Long countFindPosteCandidaturesByPostesAndCandidatsAndRecevableAndAuditionnableAndModification(List<PosteAPourvoir> postes, List<User> candidats, Boolean recevable, Boolean auditionnable, Boolean modification) {
        EntityManager em = entityManager();
        String jpaQuery = "SELECT COUNT(o) FROM PosteCandidature AS o WHERE";
        if (postes != null) {
        	jpaQuery = jpaQuery + " o.poste IN :postes AND";
        }
        if (candidats != null) {
        	jpaQuery = jpaQuery + " o.candidat IN :candidats AND";
        }
        if (recevable != null) {
        	jpaQuery = jpaQuery + " o.recevable = :recevable AND";
        }
        if (auditionnable != null) {
        	jpaQuery = jpaQuery + " o.auditionnable = :auditionnable AND";
        }
        if (modification != null) {
        	if(modification) {
        		jpaQuery = jpaQuery + " o.modification IS NOT NULL AND";
        	} else {
        		jpaQuery = jpaQuery + " o.modification IS NULL AND";
        	}
        }
        jpaQuery = jpaQuery + " 1=1";
        TypedQuery q = em.createQuery(jpaQuery, Long.class);
        if (postes != null) {
        q.setParameter("postes", postes);
        }
        if (candidats != null) {
        q.setParameter("candidats", candidats);
        }
        if (recevable != null) {
        q.setParameter("recevable", recevable);
        }
        if (auditionnable != null) {
        q.setParameter("auditionnable", auditionnable);
        }
        return ((Long) q.getSingleResult());
    }
    
    public static TypedQuery<PosteCandidature> findPostesCandidaturesByPostesAndCandidatAndRecevableAndAuditionnableAndModification(List<PosteAPourvoir> postes, List<User> candidats, Boolean recevable, Boolean auditionnable, Boolean modification, String sortFieldName, String sortOrder) {
        EntityManager em = entityManager();
        String jpaQuery = "SELECT o FROM PosteCandidature AS o WHERE";
        if (postes != null) {
        	jpaQuery = jpaQuery + " o.poste IN :postes AND";
        }
        if (candidats != null) {
        	jpaQuery = jpaQuery + " o.candidat IN :candidats AND";
        }
        if (recevable != null) {
        	jpaQuery = jpaQuery + " o.recevable = :recevable AND";
        }
        if (auditionnable != null) {
        	jpaQuery = jpaQuery + " o.auditionnable = :auditionnable AND";
        }
        if (modification != null) {
        	if(modification) {
        		jpaQuery = jpaQuery + " o.modification IS NOT NULL AND";
        	} else {
        		jpaQuery = jpaQuery + " o.modification IS NULL AND";
        	}
        }
        jpaQuery = jpaQuery + " 1=1";
        if (fieldNames4OrderClauseFilter.contains(sortFieldName)) {
            jpaQuery = jpaQuery + " ORDER BY " + sortFieldName;
            if ("ASC".equalsIgnoreCase(sortOrder) || "DESC".equalsIgnoreCase(sortOrder)) {
                jpaQuery = jpaQuery + " " + sortOrder;
            }
        }
        TypedQuery<PosteCandidature> q = em.createQuery(jpaQuery, PosteCandidature.class);
        if (postes != null) {
        q.setParameter("postes", postes);
        }
        if (candidats != null) {
        q.setParameter("candidats", candidats);
        }
        if (recevable != null) {
        q.setParameter("recevable", recevable);
        }
        if (auditionnable != null) {
        q.setParameter("auditionnable", auditionnable);
        }
        return q;
    }

    public static TypedQuery<PosteCandidature> findPosteCandidaturesByCandidatAndByDateEndCandidatGreaterThan(
    		User candidat, Date dateEndSignupCandidat) {
    	EntityManager em = entityManager();
    	String jpaQuery = "SELECT o FROM PosteCandidature AS o WHERE o.candidat = :candidat and o.poste.dateEndSignupCandidat > :dateEndSignupCandidat";
    	TypedQuery<PosteCandidature> q = em.createQuery(jpaQuery, PosteCandidature.class);			
    	q.setParameter("candidat", candidat);
    	q.setParameter("dateEndSignupCandidat", dateEndSignupCandidat);
    	return q;
    }
    
}
