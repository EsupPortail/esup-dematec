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
import java.util.Vector;
import javax.persistence.CascadeType;
import javax.persistence.EntityManager;
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

@RooJavaBean
@RooToString(excludeFields = "candidatureFiles")
@RooJpaActiveRecord(finders = { "findPosteCandidaturesByCandidat", "findPosteCandidaturesByPoste", "findPosteCandidaturesByRecevable" })
public class PosteCandidature {

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

    public String getNom() {
        return this.candidat.getNom();
    }

    public String getPrenom() {
        return this.candidat.getPrenom();
    }

    public String getEmail() {
        return this.candidat.getEmailAddress();
    }

    public static TypedQuery<fr.univrouen.poste.domain.PosteCandidature> findPosteCandidaturesRecevableByPostes(Set<fr.univrouen.poste.domain.PosteAPourvoir> postes) {
        if (postes == null) throw new IllegalArgumentException("The postes argument is required");
        TypedQuery<PosteCandidature> q = entityManager().createQuery("SELECT o FROM PosteCandidature AS o WHERE o.poste IN :postes AND o.recevable = TRUE ORDER BY o.poste.numEmploi, o.candidat.nom", PosteCandidature.class);
        q.setParameter("postes", postes);
        return q;
    }

    public static List<fr.univrouen.poste.domain.PosteCandidature> findAllPosteCandidatures() {
        return entityManager().createQuery("SELECT o FROM PosteCandidature o ORDER BY o.poste.numEmploi, o.candidat.nom", PosteCandidature.class).getResultList();
    }

    public static List<fr.univrouen.poste.domain.PosteCandidature> findPosteCandidatureEntries(int firstResult, int maxResults) {
        return entityManager().createQuery("SELECT o FROM PosteCandidature o ORDER BY o.poste.numEmploi, o.candidat.nom", PosteCandidature.class).setFirstResult(firstResult).setMaxResults(maxResults).getResultList();
    }

    public static Long countPosteActifCandidatures() {
        return entityManager().createQuery("SELECT COUNT(o) FROM PosteCandidature o WHERE o.modification is not NULL", Long.class).getSingleResult();
    }

    public static TypedQuery<fr.univrouen.poste.domain.PosteCandidature> findPosteCandidaturesByPostes(List<fr.univrouen.poste.domain.PosteAPourvoir> postes) {
        TypedQuery<PosteCandidature> q;
        if (postes == null) {
            q = entityManager().createQuery("SELECT o FROM PosteCandidature AS o ORDER BY o.poste.numEmploi, o.candidat.nom", PosteCandidature.class);
        } else {
            q = entityManager().createQuery("SELECT o FROM PosteCandidature AS o WHERE o.poste IN :postes ORDER BY o.poste.numEmploi, o.candidat.nom", PosteCandidature.class);
            q.setParameter("postes", postes);
        }
        return q;
    }
    
    public static TypedQuery<fr.univrouen.poste.domain.PosteCandidature> findPosteCandidaturesByCandidats(List<fr.univrouen.poste.domain.User> candidats) {
        TypedQuery<PosteCandidature> q;
        if (candidats == null) {
            q = entityManager().createQuery("SELECT o FROM PosteCandidature AS o ORDER BY o.poste.numEmploi, o.candidat.nom", PosteCandidature.class);
        } else {
            q = entityManager().createQuery("SELECT o FROM PosteCandidature AS o WHERE o.candidat IN :candidats ORDER BY o.poste.numEmploi, o.candidat.nom", PosteCandidature.class);
            q.setParameter("candidats", candidats);
        }
        return q;
    }

	public static TypedQuery<PosteCandidature> findPosteCandidaturesByRecevable(Boolean recevable) {
		if (recevable == null)
			throw new IllegalArgumentException("The recevable argument is required");
		EntityManager em = PosteCandidature.entityManager();
		TypedQuery<PosteCandidature> q = em.createQuery("SELECT o FROM PosteCandidature AS o WHERE o.recevable = :recevable ORDER BY o.poste.numEmploi, o.candidat.nom", PosteCandidature.class);
		q.setParameter("recevable", recevable);
		return q;
	}

}
