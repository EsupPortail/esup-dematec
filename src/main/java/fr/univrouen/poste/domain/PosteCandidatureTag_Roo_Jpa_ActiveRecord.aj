// WARNING: DO NOT EDIT THIS FILE. THIS FILE IS MANAGED BY SPRING ROO.
// You may push code into the target .java compilation unit if you wish to edit any member(s).

package fr.univrouen.poste.domain;

import fr.univrouen.poste.domain.PosteCandidatureTag;
import java.util.List;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import org.springframework.transaction.annotation.Transactional;

privileged aspect PosteCandidatureTag_Roo_Jpa_ActiveRecord {
    
    @PersistenceContext
    transient EntityManager PosteCandidatureTag.entityManager;
    
    public static final List<String> PosteCandidatureTag.fieldNames4OrderClauseFilter = java.util.Arrays.asList("name", "values");
    
    public static final EntityManager PosteCandidatureTag.entityManager() {
        EntityManager em = new PosteCandidatureTag().entityManager;
        if (em == null) throw new IllegalStateException("Entity manager has not been injected (is the Spring Aspects JAR configured as an AJC/AJDT aspects library?)");
        return em;
    }
    
    public static long PosteCandidatureTag.countPosteCandidatureTags() {
        return entityManager().createQuery("SELECT COUNT(o) FROM PosteCandidatureTag o", Long.class).getSingleResult();
    }
    
    public static List<PosteCandidatureTag> PosteCandidatureTag.findAllPosteCandidatureTags() {
        return entityManager().createQuery("SELECT o FROM PosteCandidatureTag o", PosteCandidatureTag.class).getResultList();
    }
    
    public static List<PosteCandidatureTag> PosteCandidatureTag.findAllPosteCandidatureTags(String sortFieldName, String sortOrder) {
        String jpaQuery = "SELECT o FROM PosteCandidatureTag o";
        if (fieldNames4OrderClauseFilter.contains(sortFieldName)) {
            jpaQuery = jpaQuery + " ORDER BY " + sortFieldName;
            if ("ASC".equalsIgnoreCase(sortOrder) || "DESC".equalsIgnoreCase(sortOrder)) {
                jpaQuery = jpaQuery + " " + sortOrder;
            }
        }
        return entityManager().createQuery(jpaQuery, PosteCandidatureTag.class).getResultList();
    }
    
    public static PosteCandidatureTag PosteCandidatureTag.findPosteCandidatureTag(Long id) {
        if (id == null) return null;
        return entityManager().find(PosteCandidatureTag.class, id);
    }
    
    public static List<PosteCandidatureTag> PosteCandidatureTag.findPosteCandidatureTagEntries(int firstResult, int maxResults) {
        return entityManager().createQuery("SELECT o FROM PosteCandidatureTag o", PosteCandidatureTag.class).setFirstResult(firstResult).setMaxResults(maxResults).getResultList();
    }
    
    public static List<PosteCandidatureTag> PosteCandidatureTag.findPosteCandidatureTagEntries(int firstResult, int maxResults, String sortFieldName, String sortOrder) {
        String jpaQuery = "SELECT o FROM PosteCandidatureTag o";
        if (fieldNames4OrderClauseFilter.contains(sortFieldName)) {
            jpaQuery = jpaQuery + " ORDER BY " + sortFieldName;
            if ("ASC".equalsIgnoreCase(sortOrder) || "DESC".equalsIgnoreCase(sortOrder)) {
                jpaQuery = jpaQuery + " " + sortOrder;
            }
        }
        return entityManager().createQuery(jpaQuery, PosteCandidatureTag.class).setFirstResult(firstResult).setMaxResults(maxResults).getResultList();
    }
    
    @Transactional
    public void PosteCandidatureTag.persist() {
        if (this.entityManager == null) this.entityManager = entityManager();
        this.entityManager.persist(this);
    }
    
    @Transactional
    public void PosteCandidatureTag.remove() {
        if (this.entityManager == null) this.entityManager = entityManager();
        if (this.entityManager.contains(this)) {
            this.entityManager.remove(this);
        } else {
            PosteCandidatureTag attached = PosteCandidatureTag.findPosteCandidatureTag(this.id);
            this.entityManager.remove(attached);
        }
    }
    
    @Transactional
    public void PosteCandidatureTag.flush() {
        if (this.entityManager == null) this.entityManager = entityManager();
        this.entityManager.flush();
    }
    
    @Transactional
    public void PosteCandidatureTag.clear() {
        if (this.entityManager == null) this.entityManager = entityManager();
        this.entityManager.clear();
    }
    
    @Transactional
    public PosteCandidatureTag PosteCandidatureTag.merge() {
        if (this.entityManager == null) this.entityManager = entityManager();
        PosteCandidatureTag merged = this.entityManager.merge(this);
        this.entityManager.flush();
        return merged;
    }
    
}