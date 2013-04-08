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
@RooJpaActiveRecord(finders = { "findLogImportCommissionsByStatusEquals" })
public class LogImportCommission {

    @Temporal(TemporalType.TIMESTAMP)
    @DateTimeFormat(pattern = "dd/MM/yyyy HH:mm")
    private Date actionDate;

    @Column(columnDefinition = "TEXT")
    private String message;

    private String status;

    public static List<fr.univrouen.poste.domain.LogImportCommission> findAllLogImportCommissions() {
        return entityManager().createQuery("SELECT o FROM LogImportCommission o order by o.actionDate desc", LogImportCommission.class).getResultList();
    }

    public static List<fr.univrouen.poste.domain.LogImportCommission> findLogImportCommissionEntries(int firstResult, int maxResults) {
        return entityManager().createQuery("SELECT o FROM LogImportCommission o order by o.actionDate desc", LogImportCommission.class).setFirstResult(firstResult).setMaxResults(maxResults).getResultList();
    }
        
    public static TypedQuery<LogImportCommission> findLogImportCommissionsByStatusEquals(String status) {
        if (status == null || status.length() == 0) throw new IllegalArgumentException("The status argument is required");
        EntityManager em = entityManager();
        TypedQuery<LogImportCommission> q = em.createQuery("SELECT o FROM LogImportCommission AS o WHERE o.status = :status order by o.actionDate desc", LogImportCommission.class);
        q.setParameter("status", status);
        return q;
    }

}
