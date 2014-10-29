// WARNING: DO NOT EDIT THIS FILE. THIS FILE IS MANAGED BY SPRING ROO.
// You may push code into the target .java compilation unit if you wish to edit any member(s).

package fr.univrouen.poste.domain;

import fr.univrouen.poste.domain.GalaxieMapping;
import java.util.List;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import org.springframework.transaction.annotation.Transactional;

privileged aspect GalaxieMapping_Roo_Jpa_ActiveRecord {
    
    @PersistenceContext
    transient EntityManager GalaxieMapping.entityManager;
    
    public static final List<String> GalaxieMapping.fieldNames4OrderClauseFilter = java.util.Arrays.asList("id_numemploi", "id_numCandidat", "id_civilite", "id_nom", "id_prenom", "id_email", "id_localisation", "id_profil", "cache_id_numemploi", "cache_id_numCandidat", "cache_id_civilite", "cache_id_nom", "cache_id_prenom", "cache_id_email", "cache_id_localisation", "cache_id_profil");
    
    public static final EntityManager GalaxieMapping.entityManager() {
        EntityManager em = new GalaxieMapping().entityManager;
        if (em == null) throw new IllegalStateException("Entity manager has not been injected (is the Spring Aspects JAR configured as an AJC/AJDT aspects library?)");
        return em;
    }
    
    public static long GalaxieMapping.countGalaxieMappings() {
        return entityManager().createQuery("SELECT COUNT(o) FROM GalaxieMapping o", Long.class).getSingleResult();
    }
    
    public static List<GalaxieMapping> GalaxieMapping.findAllGalaxieMappings() {
        return entityManager().createQuery("SELECT o FROM GalaxieMapping o", GalaxieMapping.class).getResultList();
    }
    
    public static List<GalaxieMapping> GalaxieMapping.findAllGalaxieMappings(String sortFieldName, String sortOrder) {
        String jpaQuery = "SELECT o FROM GalaxieMapping o";
        if (fieldNames4OrderClauseFilter.contains(sortFieldName)) {
            jpaQuery = jpaQuery + " ORDER BY " + sortFieldName;
            if ("ASC".equalsIgnoreCase(sortOrder) || "DESC".equalsIgnoreCase(sortOrder)) {
                jpaQuery = jpaQuery + " " + sortOrder;
            }
        }
        return entityManager().createQuery(jpaQuery, GalaxieMapping.class).getResultList();
    }
    
    public static GalaxieMapping GalaxieMapping.findGalaxieMapping(Long id) {
        if (id == null) return null;
        return entityManager().find(GalaxieMapping.class, id);
    }
    
    public static List<GalaxieMapping> GalaxieMapping.findGalaxieMappingEntries(int firstResult, int maxResults) {
        return entityManager().createQuery("SELECT o FROM GalaxieMapping o", GalaxieMapping.class).setFirstResult(firstResult).setMaxResults(maxResults).getResultList();
    }
    
    public static List<GalaxieMapping> GalaxieMapping.findGalaxieMappingEntries(int firstResult, int maxResults, String sortFieldName, String sortOrder) {
        String jpaQuery = "SELECT o FROM GalaxieMapping o";
        if (fieldNames4OrderClauseFilter.contains(sortFieldName)) {
            jpaQuery = jpaQuery + " ORDER BY " + sortFieldName;
            if ("ASC".equalsIgnoreCase(sortOrder) || "DESC".equalsIgnoreCase(sortOrder)) {
                jpaQuery = jpaQuery + " " + sortOrder;
            }
        }
        return entityManager().createQuery(jpaQuery, GalaxieMapping.class).setFirstResult(firstResult).setMaxResults(maxResults).getResultList();
    }
    
    @Transactional
    public void GalaxieMapping.persist() {
        if (this.entityManager == null) this.entityManager = entityManager();
        this.entityManager.persist(this);
    }
    
    @Transactional
    public void GalaxieMapping.remove() {
        if (this.entityManager == null) this.entityManager = entityManager();
        if (this.entityManager.contains(this)) {
            this.entityManager.remove(this);
        } else {
            GalaxieMapping attached = GalaxieMapping.findGalaxieMapping(this.id);
            this.entityManager.remove(attached);
        }
    }
    
    @Transactional
    public void GalaxieMapping.flush() {
        if (this.entityManager == null) this.entityManager = entityManager();
        this.entityManager.flush();
    }
    
    @Transactional
    public void GalaxieMapping.clear() {
        if (this.entityManager == null) this.entityManager = entityManager();
        this.entityManager.clear();
    }
    
    @Transactional
    public GalaxieMapping GalaxieMapping.merge() {
        if (this.entityManager == null) this.entityManager = entityManager();
        GalaxieMapping merged = this.entityManager.merge(this);
        this.entityManager.flush();
        return merged;
    }
    
}
