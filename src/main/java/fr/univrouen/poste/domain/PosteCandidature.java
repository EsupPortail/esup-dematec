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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.persistence.CascadeType;
import javax.persistence.EntityManager;
import javax.persistence.FetchType;
import javax.persistence.ManyToMany;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import javax.persistence.OrderBy;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.persistence.Transient;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Expression;
import javax.persistence.criteria.Join;
import javax.persistence.criteria.Order;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import javax.validation.constraints.NotNull;

import org.apache.commons.lang3.StringUtils;
import org.hibernate.annotations.Type;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.roo.addon.javabean.RooJavaBean;
import org.springframework.roo.addon.jpa.activerecord.RooJpaActiveRecord;
import org.springframework.roo.addon.tostring.RooToString;

import fr.univrouen.poste.domain.ManagerReview.ReviewStatusTypes;
import fr.univrouen.poste.web.searchcriteria.PosteCandidatureSearchCriteria;

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
    
    private Boolean laureat = false;   
    
    @Type(type="org.hibernate.type.StringClobType")
    private String managerComment4Members = "";

    @NotNull
    @OneToOne(cascade = CascadeType.ALL, orphanRemoval = true)
    private ManagerReview managerReview = new ManagerReview();
    
    
    @OneToOne (mappedBy="candidature", fetch=FetchType.LAZY)
    private GalaxieEntry galaxieEntry;
    
    @ManyToMany
    private Set<User> reporters;
    
    @Transient
    private Boolean reporterTag = false;

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
    
    public String getReporterTagColor() {
        return reporterTag  ?  AppliConfig.getCacheColorReporterTag() : "#fafafa";
    }

    public List<User> getSortedReporters() {
    	List<User> sortedReporters = new ArrayList<User>(this.reporters);
    	Collections.sort(sortedReporters, new UserComparator());
    	return sortedReporters;
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

    public static TypedQuery<PosteCandidature> findPosteCandidaturesRecevableByPostes(Set<PosteAPourvoir> postes, Boolean auditionnable, String sortFieldName, String sortOrder) {
        if (postes == null) throw new IllegalArgumentException("The postes argument is required");
        String jpaQuery = "SELECT o FROM PosteCandidature AS o WHERE o.poste IN :postes AND o.recevable = TRUE";
        if (auditionnable != null) {
        	jpaQuery = jpaQuery + " AND o.auditionnable = :auditionnable";
        }
        if (fieldNames4OrderClauseFilter.contains(sortFieldName)) {
            jpaQuery = jpaQuery + " ORDER BY " + sortFieldName;
            if ("ASC".equalsIgnoreCase(sortOrder) || "DESC".equalsIgnoreCase(sortOrder)) {
                jpaQuery = jpaQuery + " " + sortOrder;
            }
        }
        TypedQuery<PosteCandidature> q = entityManager().createQuery(jpaQuery, PosteCandidature.class);
        q.setParameter("postes", postes);
        if (auditionnable != null) {
        	q.setParameter("auditionnable", auditionnable);
        }
        return q;
    }

    
    public static Long countFindPosteCandidatures(PosteCandidatureSearchCriteria searchCriteria) {
    	EntityManager em = entityManager();
		CriteriaBuilder criteriaBuilder = em.getCriteriaBuilder();
		CriteriaQuery<Long> query = criteriaBuilder.createQuery(Long.class);
		Root<PosteCandidature> c = query.from(PosteCandidature.class);
		
		final List<Predicate> predicates = new ArrayList<Predicate>();

        if (searchCriteria.getPostes() != null) {
			predicates.add(c.get("poste").in(searchCriteria.getPostes()));
        }
        if (searchCriteria.getCandidats() != null) {
        	predicates.add(c.get("candidat").in(searchCriteria.getCandidats()));
        }
        if (searchCriteria.getReviewStatus() != null) {
        	Join<PosteCandidature, ManagerReview> m = c.join("managerReview");
			predicates.add(m.get("reviewStatus").in(searchCriteria.getReviewStatus()));
        }
        if (searchCriteria.getRecevable() != null) {
        	predicates.add(c.get("recevable").in(searchCriteria.getRecevable()));
        }
        if (searchCriteria.getAuditionnable() != null) {
        	predicates.add(c.get("auditionnable").in(searchCriteria.getAuditionnable()));
        }
        if (searchCriteria.getModification() != null) {
        	if(searchCriteria.getModification()) {
        		predicates.add(c.get("modification").isNotNull());
        	} else {
        		predicates.add(c.get("modification").isNull());
        	}
        }
        if(searchCriteria.getSearchText()!=null && !searchCriteria.getSearchText().isEmpty()) {
			String searchString = computeSearchString(searchCriteria.getSearchText());
			Expression<Boolean> fullTestSearchExpression = getFullTestSearchExpression(criteriaBuilder, searchString);
			predicates.add(criteriaBuilder.isTrue(fullTestSearchExpression));
		}
        
        query.where(criteriaBuilder.and(predicates.toArray(new Predicate[predicates.size()])));	
		
		query.select(criteriaBuilder.count(c));
		return em.createQuery(query).getSingleResult();
    }
    
    public static TypedQuery<PosteCandidature> findPostesCandidatures(PosteCandidatureSearchCriteria searchCriteria, String sortFieldName, String sortOrder) {
       
    	EntityManager em = entityManager();
		CriteriaBuilder criteriaBuilder = em.getCriteriaBuilder();
		CriteriaQuery<PosteCandidature> query = criteriaBuilder.createQuery(PosteCandidature.class);
		Root<PosteCandidature> c = query.from(PosteCandidature.class);
		
		final List<Predicate> predicates = new ArrayList<Predicate>();
		final List<Order> orders = new ArrayList<Order>();
		
		if("DESC".equalsIgnoreCase(sortOrder)) {
			if("nom".equals(sortFieldName)) {
				orders.add(criteriaBuilder.desc(c.join("candidat").get("nom"))); 
		    } else if("email".equals(sortFieldName)) {
				orders.add(criteriaBuilder.desc(c.join("candidat").get("emailAddress")));   
			} else if("numCandidat".equals(sortFieldName)) {
				orders.add(criteriaBuilder.desc(c.join("candidat").get("numCandidat")));   
			} else if("managerReviewState".equals(sortFieldName)) {
				orders.add(criteriaBuilder.desc(c.join("managerReview").get("reviewStatus")));   
			} 
		} else {
			if("nom".equals(sortFieldName)) {
				orders.add(criteriaBuilder.asc(c.join("candidat").get("nom"))); 
		    } else if("email".equals(sortFieldName)) {
				orders.add(criteriaBuilder.asc(c.join("candidat").get("emailAddress")));   
			} else if("numCandidat".equals(sortFieldName)) {
				orders.add(criteriaBuilder.asc(c.join("candidat").get("numCandidat")));   
			} else if("managerReviewState".equals(sortFieldName)) {
				orders.add(criteriaBuilder.asc(c.join("managerReview").get("reviewStatus")));   
			} 
		}
		
        if (searchCriteria.getPostes() != null) {
			predicates.add(c.get("poste").in(searchCriteria.getPostes()));
        }
        if (searchCriteria.getCandidats() != null) {
        	predicates.add(c.get("candidat").in(searchCriteria.getCandidats()));
        }
        if (searchCriteria.getReviewStatus() != null) {
        	Join<PosteCandidature, ManagerReview> m = c.join("managerReview");
			predicates.add(m.get("reviewStatus").in(searchCriteria.getReviewStatus()));
        }
        if (searchCriteria.getRecevable() != null) {
        	predicates.add(c.get("recevable").in(searchCriteria.getRecevable()));
        }
        if (searchCriteria.getAuditionnable() != null) {
        	predicates.add(c.get("auditionnable").in(searchCriteria.getAuditionnable()));
        }
        if (searchCriteria.getModification() != null) {
        	if(searchCriteria.getModification()) {
        		predicates.add(c.get("modification").isNotNull());
        	} else {
        		predicates.add(c.get("modification").isNull());
        	}
        }
        
        if(searchCriteria.getSearchText()!=null && !searchCriteria.getSearchText().isEmpty()) {
			String searchString = computeSearchString(searchCriteria.getSearchText());
			Expression<Boolean> fullTestSearchExpression = getFullTestSearchExpression(criteriaBuilder, searchString);
			Expression<Double> fullTestSearchRanking = getFullTestSearchRanking(criteriaBuilder, searchString);	
			predicates.add(criteriaBuilder.isTrue(fullTestSearchExpression));
			orders.add(criteriaBuilder.desc(fullTestSearchRanking));
		}
        
		if("DESC".equalsIgnoreCase(sortOrder)) {
			if(sortFieldName == null) {
				orders.add(criteriaBuilder.desc(c.join("poste").get("numEmploi")));
				orders.add(criteriaBuilder.desc(c.join("candidat").get("nom")));   
			}
		} else {
			if(sortFieldName == null) {
				orders.add(criteriaBuilder.asc(c.join("poste").get("numEmploi")));
				orders.add(criteriaBuilder.asc(c.join("candidat").get("nom")));   
			}
		}
        
		query.where(criteriaBuilder.and(predicates.toArray(new Predicate[predicates.size()])));	
		query.orderBy(orders);
		
		query.select(c);
		return em.createQuery(query);
    }
    
	private static String computeSearchString(String searchString) {
		List<String> searchStrings = Arrays.asList(StringUtils.split(searchString, " "));
		List<String> searchStringsExpr = new ArrayList<String>();
		for(String s : searchStrings) {
			searchStringsExpr.add(s + ":*");
		}
		searchString = StringUtils.join(searchStringsExpr, "|");
		return searchString;
	}
	
	private static Expression<Boolean> getFullTestSearchExpression(CriteriaBuilder cb, String searchString) {
		return cb.function(
				"fts",
				Boolean.class,
				cb.literal(searchString)
				);
	}

	private static Expression<Double> getFullTestSearchRanking(CriteriaBuilder cb, String searchString) {
		return cb.function(
				"ts_rank",
				Double.class,
				cb.literal(searchString)
				);
	}

    public static TypedQuery<PosteCandidature> findPosteCandidaturesByCandidatAndByDateEndCandidatGreaterThan(
    		User candidat, Date date) {
    	EntityManager em = entityManager();
    	String jpaQuery = "SELECT o FROM PosteCandidature AS o WHERE o.candidat = :candidat and o.poste.dateEndSignupCandidat > :date";
    	TypedQuery<PosteCandidature> q = em.createQuery(jpaQuery, PosteCandidature.class);			
    	q.setParameter("candidat", candidat);
    	q.setParameter("date", date);
    	return q;
    }

	public static TypedQuery<PosteCandidature> findPosteCandidaturesByCandidatAndByDateEndCandidatGreaterThanAndNoAuditionnableOrByDateEndCandidatAuditionnableGreaterThanAndAuditionnable(
			User candidat, Date date) {
    	EntityManager em = entityManager();
    	String jpaQuery = "SELECT o FROM PosteCandidature AS o WHERE o.candidat = :candidat and (o.poste.dateEndSignupCandidat > :date and o.auditionnable = FALSE or o.poste.dateEndCandidatAuditionnable > :date and o.auditionnable = TRUE)";
    	TypedQuery<PosteCandidature> q = em.createQuery(jpaQuery, PosteCandidature.class);			
    	q.setParameter("candidat", candidat);
    	q.setParameter("date", date);
    	return q;
	}
	
    public List<PosteCandidatureFile> getSortedCandidatureFiles() {
    	List<PosteCandidatureFile> sortedCandidatureFiles = new ArrayList<PosteCandidatureFile>(this.candidatureFiles);
    	Collections.sort(sortedCandidatureFiles, new PosteCandidatureFileComparator());
        return sortedCandidatureFiles;
    }
    
}
