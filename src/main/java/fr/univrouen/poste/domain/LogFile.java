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
import java.util.Date;
import java.util.List;

import javax.persistence.Column;
import javax.persistence.EntityManager;
import javax.persistence.Query;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Expression;
import javax.persistence.criteria.Order;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.roo.addon.javabean.RooJavaBean;
import org.springframework.roo.addon.jpa.activerecord.RooJpaActiveRecord;
import org.springframework.roo.addon.tostring.RooToString;

import fr.univrouen.poste.web.searchcriteria.LogSearchCriteria;

@RooJavaBean
@RooToString
@RooJpaActiveRecord(finders = { "findLogFilesByNumEmploi", "findLogFilesByNumCandidat", "findLogFilesByActionEquals", "findLogFilesByActionEqualsAndEmailEquals", "findLogFilesByEmailEquals"})
public class LogFile {

    @Temporal(TemporalType.TIMESTAMP)
    @DateTimeFormat(pattern = "dd/MM/yyyy HH:mm")
    private Date actionDate;

    private String userId;

    private String numEmploi;

    private String numCandidat;

    private String civilite;

    private String nom;

    private String prenom;

    private String email;

    private String ip;

    private String action;

    private String filename;

    private String fileSize;

    @Column(length=512)
    private String userAgent;

	public static TypedQuery<LogFile>  findLogFiles(LogSearchCriteria logSearchCriteria,
			String sortFieldName, String sortOrder) {
		EntityManager em = entityManager();
		CriteriaBuilder criteriaBuilder = em.getCriteriaBuilder();
		CriteriaQuery<LogFile> query = criteriaBuilder.createQuery(LogFile.class);
		Root<LogFile> c = query.from(LogFile.class);
		Expression<Boolean> logSearchCriteriaRestriction = computeLogSearchCriteriaRestriction(logSearchCriteria, c, criteriaBuilder);
		
		final List<Order> orders = new ArrayList<Order>();
		
		if(sortFieldName != null && !sortFieldName.isEmpty()) {
			String[] sortFieldNameSplit = sortFieldName.split("\\.");
			if("DESC".equalsIgnoreCase(sortOrder)) {	
				if(sortFieldNameSplit.length<2) {
					orders.add(criteriaBuilder.desc(c.get(sortFieldName)));   
				} else {
					orders.add(criteriaBuilder.desc(c.join(sortFieldNameSplit[0]).get(sortFieldNameSplit[1])));   
				}
			} else {
				if(sortFieldNameSplit.length<2) {
					orders.add(criteriaBuilder.asc(c.get(sortFieldName)));   
				} else {
					orders.add(criteriaBuilder.asc(c.join(sortFieldNameSplit[0]).get(sortFieldNameSplit[1])));   
				}
			}
		}
        
		query.where(logSearchCriteriaRestriction);	
		query.orderBy(orders);
		
		query.select(c);
		return em.createQuery(query);
	}
	
	private static Expression<Boolean> computeLogSearchCriteriaRestriction(LogSearchCriteria logSearchCriteria,
			Root<LogFile> c, CriteriaBuilder criteriaBuilder) {
		final List<Predicate> predicates = new ArrayList<Predicate>();
        if (logSearchCriteria.getUserId() != null && !logSearchCriteria.getUserId().isEmpty()) {
			predicates.add(c.get("email").in(logSearchCriteria.getUserId()));
        }
        if (logSearchCriteria.getStatus() != null && !logSearchCriteria.getStatus().isEmpty()) {
			predicates.add(c.get("action").in(logSearchCriteria.getStatus()));
        }
        return criteriaBuilder.and(predicates.toArray(new Predicate[predicates.size()]));
	}


	public static Long countFindLogFiles(LogSearchCriteria logSearchCriteria) {
	   	EntityManager em = entityManager();
		CriteriaBuilder criteriaBuilder = em.getCriteriaBuilder();
		CriteriaQuery<Long> query = criteriaBuilder.createQuery(Long.class);
		Root<LogFile> c = query.from(LogFile.class);
		Expression<Boolean> logSearchCriteriaRestriction = computeLogSearchCriteriaRestriction(logSearchCriteria, c, criteriaBuilder);

		query.where(logSearchCriteriaRestriction);	
		
		query.select(criteriaBuilder.count(c));
		return em.createQuery(query).getSingleResult();
	}

	public static List<Object[]>  countUploadLogFilesBydate() {
    	String sql = "SELECT date_part('year', action_date) as year, date_part('month', action_date) as month, date_part('day', action_date) as day, count(*) as count FROM log_file WHERE action='UPLOAD' GROUP BY year, month, day ORDER BY year, month, day";
		Query q = entityManager().createNativeQuery(sql);
        return q.getResultList();
    }
    
}
