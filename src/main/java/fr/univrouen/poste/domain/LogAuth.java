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
@RooJpaActiveRecord(finders = { "findLogAuthsByActionEquals", "findLogAuthsByActionEqualsAndUserIdEquals", "findLogAuthsByUserIdEquals"})
public class LogAuth {

    @Temporal(TemporalType.TIMESTAMP)
    @DateTimeFormat(pattern = "dd/MM/yyyy HH:mm")
    private Date actionDate;

    private String userId;

    private String ip;

    private String action;

	public static TypedQuery<LogAuth>  findLogAuths(String action2, String userId2,
			String sortFieldName, String sortOrder) {
		if("".equals(userId2)) {
			return findLogAuthsByActionEquals(action2, sortFieldName, sortOrder);
		}
		if("".equals(action2)) {
			return findLogAuthsByUserIdEquals(userId2, sortFieldName, sortOrder);
		}
		return findLogAuthsByActionEqualsAndUserIdEquals(action2, userId2, sortFieldName, sortOrder);	
	}

	public static Long countFindLogAuths(String action2, String userId2) {
		if("".equals(userId2)) {
			return countFindLogAuthsByActionEquals(action2);
		}
		if("".equals(action2)) {
			return countFindLogAuthsByUserIdEquals(userId2);
		}
		return countFindLogAuthsByActionEqualsAndUserIdEquals(action2, userId2);	
	}

	public static List<Object[]> countSuccessLogAuthsByDate() {
    	String sql = "SELECT date_part('year', action_date) as year, date_part('month', action_date) as month, date_part('day', action_date) as day, count(*) as count FROM log_auth WHERE action='AUTH SUCCESS' GROUP BY year, month, day ORDER BY year, month, day";
		Query q = entityManager().createNativeQuery(sql);
        return q.getResultList();
    }
    
}
