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

    public Page<PosteCandidature> findPosteCandidatures(PosteCandidatureSearchCriteria searchCriteria, Pageable pageable) {
        CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
        CriteriaQuery<PosteCandidature> query = criteriaBuilder.createQuery(PosteCandidature.class);
        Root<PosteCandidature> c = query.from(PosteCandidature.class);

        final List<Predicate> predicates = buildPredicates(searchCriteria, criteriaBuilder, c);
        final List<Order> orders = buildOrders(searchCriteria, criteriaBuilder, c, pageable);

        query.where(criteriaBuilder.and(predicates.toArray(new Predicate[predicates.size()])));
        query.orderBy(orders);
        query.select(c);

        TypedQuery<PosteCandidature> typedQuery = entityManager.createQuery(query);

        // Apply pagination
        typedQuery.setFirstResult((int) pageable.getOffset());
        typedQuery.setMaxResults(pageable.getPageSize());

        List<PosteCandidature> results = typedQuery.getResultList();

        // Count total for pagination
        long total = countPosteCandidatures(searchCriteria, criteriaBuilder);

        return new org.springframework.data.domain.PageImpl<>(results, pageable, total);
    }

    public List<PosteCandidature> findAllPosteCandidatures(PosteCandidatureSearchCriteria searchCriteria) {
        CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
        CriteriaQuery<PosteCandidature> query = criteriaBuilder.createQuery(PosteCandidature.class);
        Root<PosteCandidature> c = query.from(PosteCandidature.class);

        final List<Predicate> predicates = buildPredicates(searchCriteria, criteriaBuilder, c);

        query.where(criteriaBuilder.and(predicates.toArray(new Predicate[predicates.size()])));
        query.select(c);

        return entityManager.createQuery(query).getResultList();
    }

    private long countPosteCandidatures(PosteCandidatureSearchCriteria searchCriteria, CriteriaBuilder criteriaBuilder) {
        CriteriaQuery<Long> countQuery = criteriaBuilder.createQuery(Long.class);
        Root<PosteCandidature> c = countQuery.from(PosteCandidature.class);

        final List<Predicate> predicates = buildPredicates(searchCriteria, criteriaBuilder, c);

        countQuery.where(criteriaBuilder.and(predicates.toArray(new Predicate[predicates.size()])));
        countQuery.select(criteriaBuilder.count(c));

        return entityManager.createQuery(countQuery).getSingleResult();
    }

    private List<Predicate> buildPredicates(PosteCandidatureSearchCriteria searchCriteria, CriteriaBuilder criteriaBuilder, Root<PosteCandidature> c) {
        final List<Predicate> predicates = new ArrayList<>();

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

        return predicates;
    }

    private List<Order> buildOrders(PosteCandidatureSearchCriteria searchCriteria, CriteriaBuilder criteriaBuilder, Root<PosteCandidature> c, Pageable pageable) {
        final List<Order> orders = new ArrayList<>();
        Map<String, Join<PosteCandidature, ?>> joins = new HashMap<>();

        // Add full-text search ranking if applicable
        if(searchCriteria.getSearchText()!=null && !searchCriteria.getSearchText().isEmpty()) {
            String searchString = computeSearchString(searchCriteria.getSearchText());
            Expression<Double> fullTestSearchRanking = getFullTestSearchRanking(criteriaBuilder, searchString);
            orders.add(criteriaBuilder.desc(fullTestSearchRanking));
        }

        // Add sorts from Pageable
        if (pageable.getSort().isSorted()) {
            for (Sort.Order order : pageable.getSort()) {
                String property = order.getProperty();
                String[] propertyParts = property.split("\\.");

                if (propertyParts.length == 1) {
                    if (order.isAscending()) {
                        orders.add(criteriaBuilder.asc(c.get(property)));
                    } else {
                        orders.add(criteriaBuilder.desc(c.get(property)));
                    }
                } else if (propertyParts.length == 2) {
                    // Get or create join
                    Join<PosteCandidature, ?> join = joins.get(propertyParts[0]);
                    if (join == null) {
                        join = c.join(propertyParts[0], JoinType.LEFT);
                        joins.put(propertyParts[0], join);
                    }
                    if (order.isAscending()) {
                        orders.add(criteriaBuilder.asc(join.get(propertyParts[1])));
                    } else {
                        orders.add(criteriaBuilder.desc(join.get(propertyParts[1])));
                    }
                }
            }
        }

        // If no sort specified, add default sorting
        if (orders.isEmpty() || (searchCriteria.getSearchText() != null && !searchCriteria.getSearchText().isEmpty() && orders.size() == 1)) {
            Join<PosteCandidature, ?> posteJoin = joins.get("poste");
            if (posteJoin == null) {
                posteJoin = c.join("poste", JoinType.LEFT);
                joins.put("poste", posteJoin);
            }
            Join<PosteCandidature, ?> candidatJoin = joins.get("candidat");
            if (candidatJoin == null) {
                candidatJoin = c.join("candidat", JoinType.LEFT);
                joins.put("candidat", candidatJoin);
            }
            orders.add(criteriaBuilder.asc(posteJoin.get("numEmploi")));
            orders.add(criteriaBuilder.asc(candidatJoin.get("nom")));
        }

        return orders;
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
