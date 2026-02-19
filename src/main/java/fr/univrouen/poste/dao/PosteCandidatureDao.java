package fr.univrouen.poste.dao;

import fr.univrouen.poste.domain.*;
import fr.univrouen.poste.repository.PosteCandidatureRepository;
import fr.univrouen.poste.web.searchcriteria.PosteCandidatureSearchCriteria;
import jakarta.annotation.Resource;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.TypedQuery;
import jakarta.persistence.criteria.*;
import org.apache.commons.lang3.StringUtils;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.time.LocalDateTime;

@Service
@Transactional
public class PosteCandidatureDao {

    public static final List<String> fieldNames4OrderClauseFilter = java.util.Arrays.asList("creation", "modification", "poste", "poste.numEmploi", "candidatureFiles", "candidat", "recevable", "o.poste.numEmploi,o.candidat.nom", "candidat.nom", "candidat.emailAddress", "managerReview.reviewStatus", "managerReview.manager", "managerReview.reviewDate", "candidat.numCandidat", "galaxieEntry.etatDossier", "recevable", "auditionnable", "laureat");


    @Resource
    PosteCandidatureRepository posteCandidatureRepository;

    @PersistenceContext
    EntityManager entityManager;

    public long countPosteCandidatures() {
        return posteCandidatureRepository.count();
    }

    public List<PosteCandidature> findAllPosteCandidatures() {
        return posteCandidatureRepository.findAll();
    }

    public PosteCandidature findPosteCandidature(Long id) {
        if (id == null) return null;
        Optional<PosteCandidature> result = posteCandidatureRepository.findById(id);
        return result.orElse(null);
    }


    public Page<PosteCandidature> findPosteCandidaturesByCandidat(User candidat) {
        Page<PosteCandidature> results = posteCandidatureRepository.findByCandidat(candidat, Pageable.unpaged());
        return results;
    }

    public Page<PosteCandidature> findPosteCandidaturesByCandidatAndPoste(User candidat, PosteAPourvoir poste) {
        if (candidat == null) throw new IllegalArgumentException("The candidat argument is required");
        if (poste == null) throw new IllegalArgumentException("The poste argument is required");
        return posteCandidatureRepository.findByCandidatAndPoste(candidat, poste, Pageable.unpaged());
    }

    public Page<PosteCandidature> findPosteCandidaturesByPostes(Set<PosteAPourvoir> postes) {
        if (postes == null || postes.isEmpty()) throw new IllegalArgumentException("The postes argument is required");
        return posteCandidatureRepository.findByPosteIn(postes.stream().toList(), Pageable.unpaged());
    }


    public long countFindPosteCandidaturesByTag(PosteCandidatureTag tag, PosteCandidatureTagValue tagValue) {
        return posteCandidatureRepository.countFindPosteCandidaturesByTag(tag, tagValue);
    }

    public Page<PosteCandidature> findPosteCandidaturesByCandidatAndByDateEndCandidatGreaterThanAndNoAuditionnableOrByDateEndCandidatAuditionnableGreaterThanAndAuditionnable(User candidat, LocalDateTime date, Pageable pageable) {
        return posteCandidatureRepository.findPosteCandidaturesByCandidatAndByDateEndCandidatGreaterThanAndNoAuditionnableOrByDateEndCandidatAuditionnableGreaterThanAndAuditionnable(candidat, date, pageable);
    }

    public Page<PosteCandidature> findPosteCandidatureEntries(Pageable pageable) {
        return posteCandidatureRepository.findAll(pageable);
    }

    public Page<PosteCandidature> findPosteCandidatureEntries(Pageable pageable, String sortFieldName, String sortOrder) {
        if (sortFieldName != null && sortOrder != null) {
            Sort.Direction direction = "DESC".equalsIgnoreCase(sortOrder) ? Sort.Direction.DESC : Sort.Direction.ASC;
            Sort sort = Sort.by(direction, sortFieldName);
            return posteCandidatureRepository.findAll(PageRequest.of(pageable.getPageNumber(), pageable.getPageSize(), sort));
        }
        return posteCandidatureRepository.findAll(pageable);
    }

    public PosteCandidature savePosteCandidature(PosteCandidature posteCandidature) {
        return posteCandidatureRepository.save(posteCandidature);
    }

    public void deletePosteCandidature(PosteCandidature posteCandidature) {
        posteCandidatureRepository.delete(posteCandidature);
    }

    public void flush() {
        posteCandidatureRepository.flush();
    }

    public Page<PosteCandidature> findPosteCandidaturesRecevableByPostes(Set<PosteAPourvoir> postes, Boolean auditionnable, Pageable pageable) {
        return posteCandidatureRepository.findPosteCandidaturesRecevableByPostesAndByAuditionnable(postes, auditionnable, pageable);
    }

    public Long countFindPosteCandidatures(PosteCandidatureSearchCriteria searchCriteria) {
        CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
        CriteriaQuery<Long> query = criteriaBuilder.createQuery(Long.class);
        Root<PosteCandidature> c = query.from(PosteCandidature.class);

        final List<Predicate> predicates = new ArrayList<Predicate>();

        if (searchCriteria.getNumEmploiPostes() != null && !searchCriteria.getNumEmploiPostes().isEmpty()) {
            Join<PosteCandidature, PosteAPourvoir> cp = c.join("poste");
            predicates.add(cp.get("numEmploi").in(searchCriteria.getNumEmploiPostes()));
        }
        if (searchCriteria.getEmailCandidats() != null && !searchCriteria.getEmailCandidats().isEmpty()) {
            Join<PosteCandidature, User> cp = c.join("candidat");
            predicates.add(cp.get("emailAddress").in(searchCriteria.getEmailCandidats()));
        }
        if (searchCriteria.getReviewStatus() != null && !searchCriteria.getReviewStatus().isEmpty()) {
            Join<PosteCandidature, ManagerReview> m = c.join("managerReview");
            predicates.add(m.get("reviewStatus").in(searchCriteria.getReviewStatus()));
        }
        if (searchCriteria.getRecevable() != null) {
            predicates.add(c.get("recevableEnum").in(searchCriteria.getRecevable()));
        }
        if (searchCriteria.getAuditionnable() != null) {
            predicates.add(c.get("auditionnable").in(searchCriteria.getAuditionnable()));
        }
        if (searchCriteria.getModification() != null) {
            if(searchCriteria.getModification()) {
                predicates.add(c.get("modification").isNotNull());
            } else {
                predicates.add(c.get("modification").isNull());
            }
        }
        if(searchCriteria.getSearchText()!=null && !searchCriteria.getSearchText().isEmpty()) {
            String searchString = computeSearchString(searchCriteria.getSearchText());
            Expression<Boolean> fullTestSearchExpression = getFullTestSearchExpression(criteriaBuilder, searchString);
            predicates.add(criteriaBuilder.isTrue(fullTestSearchExpression));
        }

        if(searchCriteria.getTags() != null) {
            for(PosteCandidatureTag tag : searchCriteria.getTags().keySet()) {
                if(searchCriteria.getTags().get(tag) != null) {
                    MapJoin<PosteCandidature, PosteCandidatureTag, PosteCandidatureTagValue> t = c.joinMap("tags");
                    predicates.add(criteriaBuilder.equal(t.key(), tag));
                    predicates.add(criteriaBuilder.equal(t.value(), searchCriteria.getTags().get(tag)));
                }
            }
        }

        query.where(criteriaBuilder.and(predicates.toArray(new Predicate[predicates.size()])));

        query.select(criteriaBuilder.count(c));
        return entityManager.createQuery(query).getSingleResult();
    }

    public TypedQuery<PosteCandidature> findPostesCandidatures(PosteCandidatureSearchCriteria searchCriteria, String sortFieldName, String sortOrder) {

        CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
        CriteriaQuery<PosteCandidature> query = criteriaBuilder.createQuery(PosteCandidature.class);
        Root<PosteCandidature> c = query.from(PosteCandidature.class);

        final List<Predicate> predicates = new ArrayList<Predicate>();
        final List<Order> orders = new ArrayList<Order>();

        if(sortFieldName != null && !sortFieldName.isEmpty()) {
            String[] sortFieldNameSplit = sortFieldName.split("\\.");
            if("DESC".equalsIgnoreCase(sortOrder)) {
                if(sortFieldNameSplit.length<2) {
                    orders.add(criteriaBuilder.desc(c.get(sortFieldName)));
                } else {
                    orders.add(criteriaBuilder.desc(c.join(sortFieldNameSplit[0]).get(sortFieldNameSplit[1])));
                }
            } else {
                if(sortFieldNameSplit.length<2) {
                    orders.add(criteriaBuilder.asc(c.get(sortFieldName)));
                } else {
                    orders.add(criteriaBuilder.asc(c.join(sortFieldNameSplit[0]).get(sortFieldNameSplit[1])));
                }
            }
        }

        if (searchCriteria.getNumEmploiPostes() != null && !searchCriteria.getNumEmploiPostes().isEmpty()) {
            Join<PosteCandidature, PosteAPourvoir> cp = c.join("poste");
            predicates.add(cp.get("numEmploi").in(searchCriteria.getNumEmploiPostes()));
        }
        if (searchCriteria.getEmailCandidats() != null && !searchCriteria.getEmailCandidats().isEmpty()) {
            Join<PosteCandidature, User> cp = c.join("candidat");
            predicates.add(cp.get("emailAddress").in(searchCriteria.getEmailCandidats()));
        }
        if (searchCriteria.getReviewStatus() != null && !searchCriteria.getReviewStatus().isEmpty()) {
            Join<PosteCandidature, ManagerReview> m = c.join("managerReview");
            predicates.add(m.get("reviewStatus").in(searchCriteria.getReviewStatus()));
        }
        if (searchCriteria.getRecevable() != null) {
            predicates.add(c.get("recevableEnum").in(searchCriteria.getRecevable()));
        }
        if (searchCriteria.getAuditionnable() != null) {
            predicates.add(c.get("auditionnable").in(searchCriteria.getAuditionnable()));
        }
        if (searchCriteria.getModification() != null) {
            if(searchCriteria.getModification()) {
                predicates.add(c.get("modification").isNotNull());
            } else {
                predicates.add(c.get("modification").isNull());
            }
        }

        if(searchCriteria.getSearchText()!=null && !searchCriteria.getSearchText().isEmpty()) {
            String searchString = computeSearchString(searchCriteria.getSearchText());
            Expression<Boolean> fullTestSearchExpression = getFullTestSearchExpression(criteriaBuilder, searchString);
            Expression<Double> fullTestSearchRanking = getFullTestSearchRanking(criteriaBuilder, searchString);
            predicates.add(criteriaBuilder.isTrue(fullTestSearchExpression));
            orders.add(criteriaBuilder.desc(fullTestSearchRanking));
        }

        if(searchCriteria.getTags() != null) {
            for(PosteCandidatureTag tag : searchCriteria.getTags().keySet()) {
                if(searchCriteria.getTags().get(tag) != null) {
                    MapJoin<PosteCandidature, PosteCandidatureTag, PosteCandidatureTagValue> t = c.joinMap("tags");
                    predicates.add(criteriaBuilder.equal(t.key(), tag));
                    predicates.add(criteriaBuilder.equal(t.value(), searchCriteria.getTags().get(tag)));
                }
            }
        }

        if("DESC".equalsIgnoreCase(sortOrder)) {
            if(sortFieldName == null) {
                orders.add(criteriaBuilder.desc(c.join("poste").get("numEmploi")));
                orders.add(criteriaBuilder.desc(c.join("candidat").get("nom")));
            }
        } else {
            if(sortFieldName == null) {
                orders.add(criteriaBuilder.asc(c.join("poste").get("numEmploi")));
                orders.add(criteriaBuilder.asc(c.join("candidat").get("nom")));
            }
        }

        query.where(criteriaBuilder.and(predicates.toArray(new Predicate[predicates.size()])));
        query.orderBy(orders);

        query.select(c);
        return entityManager.createQuery(query);
    }

    String computeSearchString(String searchString) {
        String[] searchStrings = StringUtils.split(searchString, " ");
        List<String> searchStringsExpr = new ArrayList<String>();
        for(String s : searchStrings) {
            searchStringsExpr.add(s + ":*");
        }
        searchString = StringUtils.join(searchStringsExpr, "|");
        return searchString;
    }

    Expression<Boolean> getFullTestSearchExpression(CriteriaBuilder cb, String searchString) {
        return cb.function(
                "fts",
                Boolean.class,
                cb.literal(searchString)
        );
    }

    Expression<Double> getFullTestSearchRanking(CriteriaBuilder cb, String searchString) {
        return cb.function(
                "ts_rank",
                Double.class,
                cb.literal(searchString)
        );
    }

    public Long countPosteActifCandidatures() {
        return entityManager.createQuery("SELECT COUNT(o) FROM PosteCandidature o WHERE o.modification is not NULL", Long.class).getSingleResult();
    }

}
