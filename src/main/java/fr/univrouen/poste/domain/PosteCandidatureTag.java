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
import java.util.HashSet;
import java.util.Set;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.EntityManager;
import javax.persistence.OneToMany;
import javax.persistence.OrderBy;
import javax.persistence.TypedQuery;

import org.springframework.roo.addon.javabean.RooJavaBean;
import org.springframework.roo.addon.jpa.activerecord.RooJpaActiveRecord;
import org.springframework.roo.addon.tostring.RooToString;

@RooJavaBean
@RooToString
@RooJpaActiveRecord(finders = { "findPosteCandidatureTagsByValues", "findPosteCandidatureTagsByName" })
public class PosteCandidatureTag {

	@Column(unique=true)
    private String name;

    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("value ASC")
    private Set<PosteCandidatureTagValue> values = new HashSet<PosteCandidatureTagValue>();
    
    public static PosteCandidatureTag findPosteCandidatureTagsByValue(PosteCandidatureTagValue value) {
        if (value == null) throw new IllegalArgumentException("The value argument is required");
        EntityManager em = entityManager();
        StringBuilder queryBuilder = new StringBuilder("SELECT o FROM PosteCandidatureTag AS o WHERE :value MEMBER OF o.values");
        TypedQuery<PosteCandidatureTag> q = em.createQuery(queryBuilder.toString(), PosteCandidatureTag.class);
        q.setParameter("value", value);
        return q.getSingleResult();
    }
    
}

