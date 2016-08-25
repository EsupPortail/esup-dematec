// WARNING: DO NOT EDIT THIS FILE. THIS FILE IS MANAGED BY SPRING ROO.
// You may push code into the target .java compilation unit if you wish to edit any member(s).

package fr.univrouen.poste.domain;

import fr.univrouen.poste.domain.PosteAPourvoir;
import java.util.List;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import org.springframework.transaction.annotation.Transactional;

privileged aspect PosteAPourvoir_Roo_Jpa_ActiveRecord {
    
    @PersistenceContext
    transient EntityManager PosteAPourvoir.entityManager;
    
    public static final List<String> PosteAPourvoir.fieldNames4OrderClauseFilter = java.util.Arrays.asList("numEmploi", "profil", "localisation", "membres", "presidents", "dateEndCandidatAuditionnable", "dateEndSignupCandidat");
    
    public static final EntityManager PosteAPourvoir.entityManager() {
        EntityManager em = new PosteAPourvoir().entityManager;
        if (em == null) throw new IllegalStateException("Entity manager has not been injected (is the Spring Aspects JAR configured as an AJC/AJDT aspects library?)");
        return em;
    }
    
    public static long PosteAPourvoir.countPosteAPourvoirs() {
        return entityManager().createQuery("SELECT COUNT(o) FROM PosteAPourvoir o", Long.class).getSingleResult();
    }
    
    public static List<PosteAPourvoir> PosteAPourvoir.findAllPosteAPourvoirs(String sortFieldName, String sortOrder) {
        String jpaQuery = "SELECT o FROM PosteAPourvoir o";
        if (fieldNames4OrderClauseFilter.contains(sortFieldName)) {
            jpaQuery = jpaQuery + " ORDER BY " + sortFieldName;
            if ("ASC".equalsIgnoreCase(sortOrder) || "DESC".equalsIgnoreCase(sortOrder)) {
                jpaQuery = jpaQuery + " " + sortOrder;
            }
        }
        return entityManager().createQuery(jpaQuery, PosteAPourvoir.class).getResultList();
    }
    
    public static PosteAPourvoir PosteAPourvoir.findPosteAPourvoir(Long id) {
        if (id == null) return null;
        return entityManager().find(PosteAPourvoir.class, id);
    }
    
    public static List<PosteAPourvoir> PosteAPourvoir.findPosteAPourvoirEntries(int firstResult, int maxResults, String sortFieldName, String sortOrder) {
        String jpaQuery = "SELECT o FROM PosteAPourvoir o";
        if (fieldNames4OrderClauseFilter.contains(sortFieldName)) {
            jpaQuery = jpaQuery + " ORDER BY " + sortFieldName;
            if ("ASC".equalsIgnoreCase(sortOrder) || "DESC".equalsIgnoreCase(sortOrder)) {
                jpaQuery = jpaQuery + " " + sortOrder;
            }
        }
        return entityManager().createQuery(jpaQuery, PosteAPourvoir.class).setFirstResult(firstResult).setMaxResults(maxResults).getResultList();
    }
    
    @Transactional
    public void PosteAPourvoir.persist() {
        if (this.entityManager == null) this.entityManager = entityManager();
        this.entityManager.persist(this);
    }
    
    @Transactional
    public void PosteAPourvoir.remove() {
        if (this.entityManager == null) this.entityManager = entityManager();
        if (this.entityManager.contains(this)) {
            this.entityManager.remove(this);
        } else {
            PosteAPourvoir attached = PosteAPourvoir.findPosteAPourvoir(this.id);
            this.entityManager.remove(attached);
        }
    }
    
    @Transactional
    public void PosteAPourvoir.flush() {
        if (this.entityManager == null) this.entityManager = entityManager();
        this.entityManager.flush();
    }
    
    @Transactional
    public void PosteAPourvoir.clear() {
        if (this.entityManager == null) this.entityManager = entityManager();
        this.entityManager.clear();
    }
    
    @Transactional
    public PosteAPourvoir PosteAPourvoir.merge() {
        if (this.entityManager == null) this.entityManager = entityManager();
        PosteAPourvoir merged = this.entityManager.merge(this);
        this.entityManager.flush();
        return merged;
    }
    
}
