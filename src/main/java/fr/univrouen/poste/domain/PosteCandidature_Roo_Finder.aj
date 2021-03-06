// WARNING: DO NOT EDIT THIS FILE. THIS FILE IS MANAGED BY SPRING ROO.
// You may push code into the target .java compilation unit if you wish to edit any member(s).

package fr.univrouen.poste.domain;

import fr.univrouen.poste.domain.PosteAPourvoir;
import fr.univrouen.poste.domain.PosteCandidature;
import fr.univrouen.poste.domain.User;
import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;

privileged aspect PosteCandidature_Roo_Finder {
    
    public static Long PosteCandidature.countFindPosteCandidaturesByCandidat(User candidat) {
        if (candidat == null) throw new IllegalArgumentException("The candidat argument is required");
        EntityManager em = PosteCandidature.entityManager();
        TypedQuery q = em.createQuery("SELECT COUNT(o) FROM PosteCandidature AS o WHERE o.candidat = :candidat", Long.class);
        q.setParameter("candidat", candidat);
        return ((Long) q.getSingleResult());
    }
    
    public static Long PosteCandidature.countFindPosteCandidaturesByCandidatAndPoste(User candidat, PosteAPourvoir poste) {
        if (candidat == null) throw new IllegalArgumentException("The candidat argument is required");
        if (poste == null) throw new IllegalArgumentException("The poste argument is required");
        EntityManager em = PosteCandidature.entityManager();
        TypedQuery q = em.createQuery("SELECT COUNT(o) FROM PosteCandidature AS o WHERE o.candidat = :candidat AND o.poste = :poste", Long.class);
        q.setParameter("candidat", candidat);
        q.setParameter("poste", poste);
        return ((Long) q.getSingleResult());
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
        StringBuilder queryBuilder = new StringBuilder("SELECT o FROM PosteCandidature AS o WHERE o.candidat = :candidat");
        if (fieldNames4OrderClauseFilter.contains(sortFieldName)) {
            queryBuilder.append(" ORDER BY ").append(sortFieldName);
            if ("ASC".equalsIgnoreCase(sortOrder) || "DESC".equalsIgnoreCase(sortOrder)) {
                queryBuilder.append(" ").append(sortOrder);
            }
        }
        TypedQuery<PosteCandidature> q = em.createQuery(queryBuilder.toString(), PosteCandidature.class);
        q.setParameter("candidat", candidat);
        return q;
    }
    
    public static TypedQuery<PosteCandidature> PosteCandidature.findPosteCandidaturesByCandidatAndPoste(User candidat, PosteAPourvoir poste) {
        if (candidat == null) throw new IllegalArgumentException("The candidat argument is required");
        if (poste == null) throw new IllegalArgumentException("The poste argument is required");
        EntityManager em = PosteCandidature.entityManager();
        TypedQuery<PosteCandidature> q = em.createQuery("SELECT o FROM PosteCandidature AS o WHERE o.candidat = :candidat AND o.poste = :poste", PosteCandidature.class);
        q.setParameter("candidat", candidat);
        q.setParameter("poste", poste);
        return q;
    }
    
    public static TypedQuery<PosteCandidature> PosteCandidature.findPosteCandidaturesByCandidatAndPoste(User candidat, PosteAPourvoir poste, String sortFieldName, String sortOrder) {
        if (candidat == null) throw new IllegalArgumentException("The candidat argument is required");
        if (poste == null) throw new IllegalArgumentException("The poste argument is required");
        EntityManager em = PosteCandidature.entityManager();
        StringBuilder queryBuilder = new StringBuilder("SELECT o FROM PosteCandidature AS o WHERE o.candidat = :candidat AND o.poste = :poste");
        if (fieldNames4OrderClauseFilter.contains(sortFieldName)) {
            queryBuilder.append(" ORDER BY ").append(sortFieldName);
            if ("ASC".equalsIgnoreCase(sortOrder) || "DESC".equalsIgnoreCase(sortOrder)) {
                queryBuilder.append(" ").append(sortOrder);
            }
        }
        TypedQuery<PosteCandidature> q = em.createQuery(queryBuilder.toString(), PosteCandidature.class);
        q.setParameter("candidat", candidat);
        q.setParameter("poste", poste);
        return q;
    }
    
}
