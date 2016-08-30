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
import javax.persistence.Query;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.persistence.TypedQuery;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.roo.addon.javabean.RooJavaBean;
import org.springframework.roo.addon.jpa.activerecord.RooJpaActiveRecord;
import org.springframework.roo.addon.tostring.RooToString;

@RooJavaBean
@RooToString
@RooJpaActiveRecord(finders = { "findLogPosteFilesByNumEmploi", "findLogPosteFilesByActionEquals", "findLogPosteFilesByActionEqualsAndEmailEquals", "findLogPosteFilesByEmailEquals"})
public class LogPosteFile {

    @Temporal(TemporalType.TIMESTAMP)
    @DateTimeFormat(pattern = "dd/MM/yyyy HH:mm")
    private Date actionDate;

    private String numEmploi;

    private String email;

    private String ip;

    private String action;

    private String filename;

    private String fileSize;

    @Column(length=512)
    private String userAgent;

	public static TypedQuery<LogPosteFile>  findLogPosteFiles(String action2, String userId2,
			String sortFieldName, String sortOrder) {
		if("".equals(userId2)) {
			return findLogPosteFilesByActionEquals(action2, sortFieldName, sortOrder);
		}
		if("".equals(action2)) {
			return findLogPosteFilesByEmailEquals(userId2, sortFieldName, sortOrder);
		}
		return findLogPosteFilesByActionEqualsAndEmailEquals(action2, userId2, sortFieldName, sortOrder);	
	}

	public static Long countFindLogPosteFiles(String action2, String userId2) {
		if("".equals(userId2)) {
			return countFindLogPosteFilesByActionEquals(action2);
		}
		if("".equals(action2)) {
			return countFindLogPosteFilesByEmailEquals(userId2);
		}
		return countFindLogPosteFilesByActionEqualsAndEmailEquals(action2, userId2);	
	}

	public static List<Object[]>  countUploadLogPosteFilesBydate() {
    	String sql = "SELECT date_part('year', action_date) as year, date_part('month', action_date) as month, date_part('day', action_date) as day, count(*) as count FROM log_poste_file WHERE action='UPLOAD' GROUP BY year, month, day ORDER BY year, month, day";
		Query q = entityManager().createNativeQuery(sql);
        return q.getResultList();
    }

	public static List<String> findAllUserIds() {
    	String sql = "SELECT distinct(user_id) FROM log_poste_file ORDER BY user_id";
		Query q = entityManager().createNativeQuery(sql);
        return q.getResultList();
	}
    
}
