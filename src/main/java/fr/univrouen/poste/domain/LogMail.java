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
import java.util.List;

import javax.persistence.Column;
import javax.persistence.EntityManager;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.persistence.TypedQuery;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.roo.addon.javabean.RooJavaBean;
import org.springframework.roo.addon.jpa.activerecord.RooJpaActiveRecord;
import org.springframework.roo.addon.tostring.RooToString;

@RooJavaBean
@RooToString
@RooJpaActiveRecord(finders = { "findLogMailsByStatusEquals", "findLogMailsByStatusEqualsAndMailToEquals", "findLogMailsByMailToEquals"})
public class LogMail {

    @Temporal(TemporalType.TIMESTAMP)
    @DateTimeFormat(pattern = "dd/MM/yyyy HH:mm")
    private Date actionDate;

    private String mailTo;

    @Column(columnDefinition = "TEXT")
    private String message;

    private String status;
    
	
    public static Long countFindLogMails(String status, String mailTo) {
    	if("".equals(mailTo)) {
			return countFindLogMailsByStatusEquals(status);
		}
		if("".equals(status)) {
			return countFindLogMailsByMailToEquals(mailTo);
		}
		return countFindLogMailsByStatusEqualsAndMailToEquals(status, mailTo);	
    }
    
    
    public static TypedQuery<LogMail> findLogMails(String status, String mailTo, String sortFieldName, String sortOrder) {
    	if("".equals(mailTo)) {
			return findLogMailsByStatusEquals(status, sortFieldName, sortOrder);
		}
		if("".equals(status)) {
			return findLogMailsByMailToEquals(mailTo, sortFieldName, sortOrder);
		}
		return findLogMailsByStatusEqualsAndMailToEquals(status, mailTo, sortFieldName, sortOrder);	
    }
    
    public static List<String> getAllMailTo() {
    	EntityManager em = entityManager();
    	TypedQuery<String> q = em.createQuery("select distinct(o.mailTo) FROM LogMail o ORDER BY o.mailTo", String.class);
    	return q.getResultList();
    }

}

