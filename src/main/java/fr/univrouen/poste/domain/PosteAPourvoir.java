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
import java.util.Set;
import javax.persistence.ManyToMany;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.persistence.TypedQuery;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.roo.addon.javabean.RooJavaBean;
import org.springframework.roo.addon.jpa.activerecord.RooJpaActiveRecord;
import org.springframework.roo.addon.tostring.RooToString;

@RooJavaBean
@RooToString(excludeFields = "membres")
@RooJpaActiveRecord(finders = { "findPosteAPourvoirsByNumEmploi", "findPosteAPourvoirsByDateEndSignupCandidatGreaterThan" })
public class PosteAPourvoir {

    private String numEmploi;

    private String profil;

    private String localisation;

    @ManyToMany
    private Set<User> membres;

    @ManyToMany
    private Set<User> presidents;
    
    @Temporal(TemporalType.TIMESTAMP)
    @DateTimeFormat(pattern = "dd/MM/yyyy HH:mm")
    private Date dateEndCandidatAuditionnable;

    @Temporal(TemporalType.TIMESTAMP)
    @DateTimeFormat(pattern = "dd/MM/yyyy HH:mm")
    private Date dateEndSignupCandidat;

    public static List<PosteAPourvoir> findAllPosteAPourvoirs() {
        return entityManager().createQuery("SELECT o FROM PosteAPourvoir o order by o.numEmploi asc", PosteAPourvoir.class).getResultList();
    }

    public static List<String> findAllPosteAPourvoirNumEplois() {
        return entityManager().createQuery("SELECT o.numEmploi FROM PosteAPourvoir o order by o.numEmploi asc", String.class).getResultList();
    }

    public static List<PosteAPourvoir> findPosteAPourvoirEntries(int firstResult, int maxResults) {
        return entityManager().createQuery("SELECT o FROM PosteAPourvoir o order by o.numEmploi asc", PosteAPourvoir.class).setFirstResult(firstResult).setMaxResults(maxResults).getResultList();
    }

    public static List<PosteAPourvoir> findPosteAPourvoirsByNumEmplois(List<String> numEmplois) {
        if (numEmplois == null) throw new IllegalArgumentException("The numEmplois argument is required");
        TypedQuery<PosteAPourvoir> q = entityManager().createQuery("SELECT o FROM PosteAPourvoir o WHERE o.numEmploi IN :numEmplois ORDER BY o.numEmploi asc", PosteAPourvoir.class);
        q.setParameter("numEmplois", numEmplois);
        return q.getResultList();
    }
}
