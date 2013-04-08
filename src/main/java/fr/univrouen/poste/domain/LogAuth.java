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
@RooJpaActiveRecord(finders = { "findLogAuthsByActionEquals" })
public class LogAuth {

    @Temporal(TemporalType.TIMESTAMP)
    @DateTimeFormat(pattern = "dd/MM/yyyy HH:mm")
    private Date actionDate;

    private String userId;

    private String ip;

    private String action;

    public static List<fr.univrouen.poste.domain.LogAuth> findAllLogAuths() {
        return entityManager().createQuery("SELECT o FROM LogAuth o order by o.actionDate desc", LogAuth.class).getResultList();
    }

    public static List<fr.univrouen.poste.domain.LogAuth> findLogAuthEntries(int firstResult, int maxResults) {
        return entityManager().createQuery("SELECT o FROM LogAuth o order by o.actionDate desc", LogAuth.class).setFirstResult(firstResult).setMaxResults(maxResults).getResultList();
    }
    
    public static TypedQuery<LogAuth> findLogAuthsByActionEquals(String action) {
        if (action == null || action.length() == 0) throw new IllegalArgumentException("The action argument is required");
        EntityManager em = entityManager();
        TypedQuery<LogAuth> q = em.createQuery("SELECT o FROM LogAuth AS o WHERE o.action = :action order by o.actionDate desc", LogAuth.class);
        q.setParameter("action", action);
        return q;
    }

}
