// WARNING: DO NOT EDIT THIS FILE. THIS FILE IS MANAGED BY SPRING ROO.
// You may push code into the target .java compilation unit if you wish to edit any member(s).

package fr.univrouen.poste.domain;

import fr.univrouen.poste.domain.AppliConfigFileType;
import java.util.List;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import org.springframework.transaction.annotation.Transactional;

privileged aspect AppliConfigFileType_Roo_Jpa_ActiveRecord {
    
    @PersistenceContext
    transient EntityManager AppliConfigFileType.entityManager;
    
    public static final EntityManager AppliConfigFileType.entityManager() {
        EntityManager em = new AppliConfigFileType().entityManager;
        if (em == null) throw new IllegalStateException("Entity manager has not been injected (is the Spring Aspects JAR configured as an AJC/AJDT aspects library?)");
        return em;
    }
    
    public static long AppliConfigFileType.countAppliConfigFileTypes() {
        return entityManager().createQuery("SELECT COUNT(o) FROM AppliConfigFileType o", Long.class).getSingleResult();
    }
    
    public static List<AppliConfigFileType> AppliConfigFileType.findAllAppliConfigFileTypes() {
        return entityManager().createQuery("SELECT o FROM AppliConfigFileType o", AppliConfigFileType.class).getResultList();
    }
    
    public static List<AppliConfigFileType> AppliConfigFileType.findAllAppliConfigFileTypes(String sortFieldName, String sortOrder) {
        String jpaQuery = "SELECT o FROM AppliConfigFileType o";
        if (fieldNames4OrderClauseFilter.contains(sortFieldName)) {
            jpaQuery = jpaQuery + " ORDER BY " + sortFieldName;
            if ("ASC".equalsIgnoreCase(sortOrder) || "DESC".equalsIgnoreCase(sortOrder)) {
                jpaQuery = jpaQuery + " " + sortOrder;
            }
        }
        return entityManager().createQuery(jpaQuery, AppliConfigFileType.class).getResultList();
    }
    
    public static AppliConfigFileType AppliConfigFileType.findAppliConfigFileType(Long id) {
        if (id == null) return null;
        return entityManager().find(AppliConfigFileType.class, id);
    }
    
    public static List<AppliConfigFileType> AppliConfigFileType.findAppliConfigFileTypeEntries(int firstResult, int maxResults) {
        return entityManager().createQuery("SELECT o FROM AppliConfigFileType o", AppliConfigFileType.class).setFirstResult(firstResult).setMaxResults(maxResults).getResultList();
    }
    
    public static List<AppliConfigFileType> AppliConfigFileType.findAppliConfigFileTypeEntries(int firstResult, int maxResults, String sortFieldName, String sortOrder) {
        String jpaQuery = "SELECT o FROM AppliConfigFileType o";
        if (fieldNames4OrderClauseFilter.contains(sortFieldName)) {
            jpaQuery = jpaQuery + " ORDER BY " + sortFieldName;
            if ("ASC".equalsIgnoreCase(sortOrder) || "DESC".equalsIgnoreCase(sortOrder)) {
                jpaQuery = jpaQuery + " " + sortOrder;
            }
        }
        return entityManager().createQuery(jpaQuery, AppliConfigFileType.class).setFirstResult(firstResult).setMaxResults(maxResults).getResultList();
    }
    
    @Transactional
    public void AppliConfigFileType.persist() {
        if (this.entityManager == null) this.entityManager = entityManager();
        this.entityManager.persist(this);
    }
    
    @Transactional
    public void AppliConfigFileType.remove() {
        if (this.entityManager == null) this.entityManager = entityManager();
        if (this.entityManager.contains(this)) {
            this.entityManager.remove(this);
        } else {
            AppliConfigFileType attached = AppliConfigFileType.findAppliConfigFileType(this.id);
            this.entityManager.remove(attached);
        }
    }
    
    @Transactional
    public void AppliConfigFileType.flush() {
        if (this.entityManager == null) this.entityManager = entityManager();
        this.entityManager.flush();
    }
    
    @Transactional
    public void AppliConfigFileType.clear() {
        if (this.entityManager == null) this.entityManager = entityManager();
        this.entityManager.clear();
    }
    
    @Transactional
    public AppliConfigFileType AppliConfigFileType.merge() {
        if (this.entityManager == null) this.entityManager = entityManager();
        AppliConfigFileType merged = this.entityManager.merge(this);
        this.entityManager.flush();
        return merged;
    }
    
}
