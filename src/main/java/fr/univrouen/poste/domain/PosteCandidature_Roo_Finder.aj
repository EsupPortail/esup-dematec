// WARNING: DO NOT EDIT THIS FILE. THIS FILE IS MANAGED BY SPRING ROO.
// You may push code into the target .java compilation unit if you wish to edit any member(s).

package fr.univrouen.poste.domain;

import fr.univrouen.poste.domain.PosteAPourvoir;
import fr.univrouen.poste.domain.PosteCandidature;
import fr.univrouen.poste.domain.User;
import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;

privileged aspect PosteCandidature_Roo_Finder {
    
    public static Long PosteCandidature.countFindPosteCandidaturesByAuditionnable(Boolean auditionnable) {
        if (auditionnable == null) throw new IllegalArgumentException("The auditionnable argument is required");
        EntityManager em = PosteCandidature.entityManager();
        TypedQuery q = em.createQuery("SELECT COUNT(o) FROM PosteCandidature AS o WHERE o.auditionnable = :auditionnable", Long.class);
        q.setParameter("auditionnable", auditionnable);
        return ((Long) q.getSingleResult());
    }
    
    public static Long PosteCandidature.countFindPosteCandidaturesByCandidat(User candidat) {
        if (candidat == null) throw new IllegalArgumentException("The candidat argument is required");
        EntityManager em = PosteCandidature.entityManager();
        TypedQuery q = em.createQuery("SELECT COUNT(o) FROM PosteCandidature AS o WHERE o.candidat = :candidat", Long.class);
        q.setParameter("candidat", candidat);
        return ((Long) q.getSingleResult());
    }
    
    public static Long PosteCandidature.countFindPosteCandidaturesByPoste(PosteAPourvoir poste) {
        if (poste == null) throw new IllegalArgumentException("The poste argument is required");
        EntityManager em = PosteCandidature.entityManager();
        TypedQuery q = em.createQuery("SELECT COUNT(o) FROM PosteCandidature AS o WHERE o.poste = :poste", Long.class);
        q.setParameter("poste", poste);
        return ((Long) q.getSingleResult());
    }
    
    public static Long PosteCandidature.countFindPosteCandidaturesByRecevable(Boolean recevable) {
        if (recevable == null) throw new IllegalArgumentException("The recevable argument is required");
        EntityManager em = PosteCandidature.entityManager();
        TypedQuery q = em.createQuery("SELECT COUNT(o) FROM PosteCandidature AS o WHERE o.recevable = :recevable", Long.class);
        q.setParameter("recevable", recevable);
        return ((Long) q.getSingleResult());
    }
    
    public static TypedQuery<PosteCandidature> PosteCandidature.findPosteCandidaturesByAuditionnable(Boolean auditionnable) {
        if (auditionnable == null) throw new IllegalArgumentException("The auditionnable argument is required");
        EntityManager em = PosteCandidature.entityManager();
        TypedQuery<PosteCandidature> q = em.createQuery("SELECT o FROM PosteCandidature AS o WHERE o.auditionnable = :auditionnable", PosteCandidature.class);
        q.setParameter("auditionnable", auditionnable);
        return q;
    }
    
    public static TypedQuery<PosteCandidature> PosteCandidature.findPosteCandidaturesByAuditionnable(Boolean auditionnable, String sortFieldName, String sortOrder) {
        if (auditionnable == null) throw new IllegalArgumentException("The auditionnable argument is required");
        EntityManager em = PosteCandidature.entityManager();
        String jpaQuery = "SELECT o FROM PosteCandidature AS o WHERE o.auditionnable = :auditionnable";
        if (fieldNames4OrderClauseFilter.contains(sortFieldName)) {
            jpaQuery = jpaQuery + " ORDER BY " + sortFieldName;
            if ("ASC".equalsIgnoreCase(sortOrder) || "DESC".equalsIgnoreCase(sortOrder)) {
                jpaQuery = jpaQuery + " " + sortOrder;
            }
        }
        TypedQuery<PosteCandidature> q = em.createQuery(jpaQuery, PosteCandidature.class);
        q.setParameter("auditionnable", auditionnable);
        return q;
    }
    
    public static TypedQuery<PosteCandidature> PosteCandidature.findPosteCandidaturesByCandidat(User candidat) {
        if (candidat == null) throw new IllegalArgumentException("The candidat argument is required");
        EntityManager em = PosteCandidature.entityManager();
        TypedQuery<PosteCandidature> q = em.createQuery("SELECT o FROM PosteCandidature AS o WHERE o.candidat = :candidat", PosteCandidature.class);
        q.setParameter("candidat", candidat);
        return q;
    }
    
    public static TypedQuery<PosteCandidature> PosteCandidature.findPosteCandidaturesByCandidat(User candidat, String sortFieldName, String sortOrder) {
        if (candidat == null) throw new IllegalArgumentException("The candidat argument is required");
        EntityManager em = PosteCandidature.entityManager();
        String jpaQuery = "SELECT o FROM PosteCandidature AS o WHERE o.candidat = :candidat";
        if (fieldNames4OrderClauseFilter.contains(sortFieldName)) {
            jpaQuery = jpaQuery + " ORDER BY " + sortFieldName;
            if ("ASC".equalsIgnoreCase(sortOrder) || "DESC".equalsIgnoreCase(sortOrder)) {
                jpaQuery = jpaQuery + " " + sortOrder;
            }
        }
        TypedQuery<PosteCandidature> q = em.createQuery(jpaQuery, PosteCandidature.class);
        q.setParameter("candidat", candidat);
        return q;
    }
    
    public static TypedQuery<PosteCandidature> PosteCandidature.findPosteCandidaturesByPoste(PosteAPourvoir poste) {
        if (poste == null) throw new IllegalArgumentException("The poste argument is required");
        EntityManager em = PosteCandidature.entityManager();
        TypedQuery<PosteCandidature> q = em.createQuery("SELECT o FROM PosteCandidature AS o WHERE o.poste = :poste", PosteCandidature.class);
        q.setParameter("poste", poste);
        return q;
    }
    
    public static TypedQuery<PosteCandidature> PosteCandidature.findPosteCandidaturesByPoste(PosteAPourvoir poste, String sortFieldName, String sortOrder) {
        if (poste == null) throw new IllegalArgumentException("The poste argument is required");
        EntityManager em = PosteCandidature.entityManager();
        String jpaQuery = "SELECT o FROM PosteCandidature AS o WHERE o.poste = :poste";
        if (fieldNames4OrderClauseFilter.contains(sortFieldName)) {
            jpaQuery = jpaQuery + " ORDER BY " + sortFieldName;
            if ("ASC".equalsIgnoreCase(sortOrder) || "DESC".equalsIgnoreCase(sortOrder)) {
                jpaQuery = jpaQuery + " " + sortOrder;
            }
        }
        TypedQuery<PosteCandidature> q = em.createQuery(jpaQuery, PosteCandidature.class);
        q.setParameter("poste", poste);
        return q;
    }
    
    public static TypedQuery<PosteCandidature> PosteCandidature.findPosteCandidaturesByRecevable(Boolean recevable) {
        if (recevable == null) throw new IllegalArgumentException("The recevable argument is required");
        EntityManager em = PosteCandidature.entityManager();
        TypedQuery<PosteCandidature> q = em.createQuery("SELECT o FROM PosteCandidature AS o WHERE o.recevable = :recevable", PosteCandidature.class);
        q.setParameter("recevable", recevable);
        return q;
    }
    
    public static TypedQuery<PosteCandidature> PosteCandidature.findPosteCandidaturesByRecevable(Boolean recevable, String sortFieldName, String sortOrder) {
        if (recevable == null) throw new IllegalArgumentException("The recevable argument is required");
        EntityManager em = PosteCandidature.entityManager();
        String jpaQuery = "SELECT o FROM PosteCandidature AS o WHERE o.recevable = :recevable";
        if (fieldNames4OrderClauseFilter.contains(sortFieldName)) {
            jpaQuery = jpaQuery + " ORDER BY " + sortFieldName;
            if ("ASC".equalsIgnoreCase(sortOrder) || "DESC".equalsIgnoreCase(sortOrder)) {
                jpaQuery = jpaQuery + " " + sortOrder;
            }
        }
        TypedQuery<PosteCandidature> q = em.createQuery(jpaQuery, PosteCandidature.class);
        q.setParameter("recevable", recevable);
        return q;
    }
    
}
