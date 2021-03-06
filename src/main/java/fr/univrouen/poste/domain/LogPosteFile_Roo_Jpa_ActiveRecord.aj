// WARNING: DO NOT EDIT THIS FILE. THIS FILE IS MANAGED BY SPRING ROO.
// You may push code into the target .java compilation unit if you wish to edit any member(s).

package fr.univrouen.poste.domain;

import fr.univrouen.poste.domain.LogPosteFile;
import java.util.List;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import org.springframework.transaction.annotation.Transactional;

privileged aspect LogPosteFile_Roo_Jpa_ActiveRecord {
    
    @PersistenceContext
    transient EntityManager LogPosteFile.entityManager;
    
    public static final List<String> LogPosteFile.fieldNames4OrderClauseFilter = java.util.Arrays.asList("actionDate", "numEmploi", "email", "ip", "action", "filename", "fileSize", "userAgent");
    
    public static final EntityManager LogPosteFile.entityManager() {
        EntityManager em = new LogPosteFile().entityManager;
        if (em == null) throw new IllegalStateException("Entity manager has not been injected (is the Spring Aspects JAR configured as an AJC/AJDT aspects library?)");
        return em;
    }
    
    public static long LogPosteFile.countLogPosteFiles() {
        return entityManager().createQuery("SELECT COUNT(o) FROM LogPosteFile o", Long.class).getSingleResult();
    }
    
    public static List<LogPosteFile> LogPosteFile.findAllLogPosteFiles() {
        return entityManager().createQuery("SELECT o FROM LogPosteFile o", LogPosteFile.class).getResultList();
    }
    
    public static List<LogPosteFile> LogPosteFile.findAllLogPosteFiles(String sortFieldName, String sortOrder) {
        String jpaQuery = "SELECT o FROM LogPosteFile o";
        if (fieldNames4OrderClauseFilter.contains(sortFieldName)) {
            jpaQuery = jpaQuery + " ORDER BY " + sortFieldName;
            if ("ASC".equalsIgnoreCase(sortOrder) || "DESC".equalsIgnoreCase(sortOrder)) {
                jpaQuery = jpaQuery + " " + sortOrder;
            }
        }
        return entityManager().createQuery(jpaQuery, LogPosteFile.class).getResultList();
    }
    
    public static LogPosteFile LogPosteFile.findLogPosteFile(Long id) {
        if (id == null) return null;
        return entityManager().find(LogPosteFile.class, id);
    }
    
    public static List<LogPosteFile> LogPosteFile.findLogPosteFileEntries(int firstResult, int maxResults) {
        return entityManager().createQuery("SELECT o FROM LogPosteFile o", LogPosteFile.class).setFirstResult(firstResult).setMaxResults(maxResults).getResultList();
    }
    
    public static List<LogPosteFile> LogPosteFile.findLogPosteFileEntries(int firstResult, int maxResults, String sortFieldName, String sortOrder) {
        String jpaQuery = "SELECT o FROM LogPosteFile o";
        if (fieldNames4OrderClauseFilter.contains(sortFieldName)) {
            jpaQuery = jpaQuery + " ORDER BY " + sortFieldName;
            if ("ASC".equalsIgnoreCase(sortOrder) || "DESC".equalsIgnoreCase(sortOrder)) {
                jpaQuery = jpaQuery + " " + sortOrder;
            }
        }
        return entityManager().createQuery(jpaQuery, LogPosteFile.class).setFirstResult(firstResult).setMaxResults(maxResults).getResultList();
    }
    
    @Transactional
    public void LogPosteFile.persist() {
        if (this.entityManager == null) this.entityManager = entityManager();
        this.entityManager.persist(this);
    }
    
    @Transactional
    public void LogPosteFile.remove() {
        if (this.entityManager == null) this.entityManager = entityManager();
        if (this.entityManager.contains(this)) {
            this.entityManager.remove(this);
        } else {
            LogPosteFile attached = LogPosteFile.findLogPosteFile(this.id);
            this.entityManager.remove(attached);
        }
    }
    
    @Transactional
    public void LogPosteFile.flush() {
        if (this.entityManager == null) this.entityManager = entityManager();
        this.entityManager.flush();
    }
    
    @Transactional
    public void LogPosteFile.clear() {
        if (this.entityManager == null) this.entityManager = entityManager();
        this.entityManager.clear();
    }
    
    @Transactional
    public LogPosteFile LogPosteFile.merge() {
        if (this.entityManager == null) this.entityManager = entityManager();
        LogPosteFile merged = this.entityManager.merge(this);
        this.entityManager.flush();
        return merged;
    }
    
}
