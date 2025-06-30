package fr.univrouen.poste.repository;

import fr.univrouen.poste.dao.PosteAPourvoirDao;
import fr.univrouen.poste.dao.UserDao;
import fr.univrouen.poste.domain.*;
import fr.univrouen.poste.web.searchcriteria.PosteCandidatureSearchCriteria;
import jakarta.annotation.Resource;
import jakarta.persistence.EntityManager;
import jakarta.persistence.criteria.*;
import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class PosteCandidatureRepositoryImpl implements PosteCandidatureRepositoryCustom {

    @Resource
    EntityManager entityManager;

    @Resource
    PosteAPourvoirDao posteAPourvoirDao;

    @Resource
    UserDao userDao;

    @Override
    public List<PosteCandidature> findPostesCandidatures(PosteCandidatureSearchCriteria searchCriteria, String sortFieldName, String sortOrder) {
        CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
        CriteriaQuery<PosteCandidature> query = criteriaBuilder.createQuery(PosteCandidature.class);
        Root<PosteCandidature> c = query.from(PosteCandidature.class);

        final List<Predicate> predicates = new ArrayList<>();
        final List<Order> orders = new ArrayList<>();

        if (sortFieldName != null && !sortFieldName.isEmpty()) {
            String[] sortFieldNameSplit = sortFieldName.split("\\.");
            if ("DESC".equalsIgnoreCase(sortOrder)) {
                if (sortFieldNameSplit.length < 2) {
                    orders.add(criteriaBuilder.desc(c.get(sortFieldName)));
                } else {
                    orders.add(criteriaBuilder.desc(c.join(sortFieldNameSplit[0]).get(sortFieldNameSplit[1])));
                }
            } else {
                if (sortFieldNameSplit.length < 2) {
                    orders.add(criteriaBuilder.asc(c.get(sortFieldName)));
                } else {
                    orders.add(criteriaBuilder.asc(c.join(sortFieldNameSplit[0]).get(sortFieldNameSplit[1])));
                }
            }
        }

        List<PosteAPourvoir> postes = null;
        if (searchCriteria.getNumEmploiPostes() != null && !searchCriteria.getNumEmploiPostes().isEmpty()) {
            postes = posteAPourvoirDao.findPosteAPourvoirsByNumEmplois(searchCriteria.getNumEmploiPostes());
        }
        if (postes != null && !postes.isEmpty()) {
            predicates.add(c.get("poste").in(postes));
        }

        List<User> candidats = null;
        if (searchCriteria.getEmailCandidats() != null && !searchCriteria.getEmailCandidats().isEmpty()) {
            candidats = userDao.findUsersByEmailAddresses(searchCriteria.getEmailCandidats());
        }
        if (candidats != null && !candidats.isEmpty()) {
            predicates.add(c.get("candidat").in(candidats));
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
            if (searchCriteria.getModification()) {
                predicates.add(c.get("modification").isNotNull());
            } else {
                predicates.add(c.get("modification").isNull());
            }
        }

        if (searchCriteria.getSearchText() != null && !searchCriteria.getSearchText().isEmpty()) {
            String searchString = computeSearchString(searchCriteria.getSearchText());
            Expression<Boolean> fullTestSearchExpression = getFullTestSearchExpression(criteriaBuilder, searchString);
            Expression<Double> fullTestSearchRanking = getFullTestSearchRanking(criteriaBuilder, searchString);
            predicates.add(criteriaBuilder.isTrue(fullTestSearchExpression));
            orders.add(criteriaBuilder.desc(fullTestSearchRanking));
        }

        if (searchCriteria.getTags() != null) {
            for (PosteCandidatureTag tag : searchCriteria.getTags().keySet()) {
                if (searchCriteria.getTags().get(tag) != null) {
                    MapJoin<PosteCandidature, PosteCandidatureTag, PosteCandidatureTagValue> t = c.joinMap("tags");
                    predicates.add(criteriaBuilder.equal(t.key(), tag));
                    predicates.add(criteriaBuilder.equal(t.value(), searchCriteria.getTags().get(tag)));
                }
            }
        }

        if ("DESC".equalsIgnoreCase(sortOrder)) {
            if (sortFieldName == null) {
                orders.add(criteriaBuilder.desc(c.join("poste").get("numEmploi")));
                orders.add(criteriaBuilder.desc(c.join("candidat").get("nom")));
            }
        } else {
            if (sortFieldName == null) {
                orders.add(criteriaBuilder.asc(c.join("poste").get("numEmploi")));
                orders.add(criteriaBuilder.asc(c.join("candidat").get("nom")));
            }
        }

        query.where(criteriaBuilder.and(predicates.toArray(new Predicate[0])));
        query.orderBy(orders);
        query.select(c);
        return entityManager.createQuery(query).getResultList();
    }

    @Override
    public long countFindPosteCandidatures(PosteCandidatureSearchCriteria searchCriteria) {
        CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
        CriteriaQuery<Long> query = criteriaBuilder.createQuery(Long.class);
        Root<PosteCandidature> c = query.from(PosteCandidature.class);

        final List<Predicate> predicates = new ArrayList<>();

        List<PosteAPourvoir> postes = null;
        if (searchCriteria.getNumEmploiPostes() != null && !searchCriteria.getNumEmploiPostes().isEmpty()) {
            postes = posteAPourvoirDao.findPosteAPourvoirsByNumEmplois(searchCriteria.getNumEmploiPostes());
        }
        if (postes != null && !postes.isEmpty()) {
            predicates.add(c.get("poste").in(postes));
        }

        List<User> candidats = null;
        if (searchCriteria.getEmailCandidats() != null && !searchCriteria.getEmailCandidats().isEmpty()) {
            candidats = userDao.findUsersByEmailAddresses(searchCriteria.getEmailCandidats());
        }
        if (candidats != null && !candidats.isEmpty()) {
            predicates.add(c.get("candidat").in(candidats));
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
            if (searchCriteria.getModification()) {
                predicates.add(c.get("modification").isNotNull());
            } else {
                predicates.add(c.get("modification").isNull());
            }
        }

        if (searchCriteria.getSearchText() != null && !searchCriteria.getSearchText().isEmpty()) {
            String searchString = computeSearchString(searchCriteria.getSearchText());
            Expression<Boolean> fullTestSearchExpression = getFullTestSearchExpression(criteriaBuilder, searchString);
            predicates.add(criteriaBuilder.isTrue(fullTestSearchExpression));
        }

        if (searchCriteria.getTags() != null) {
            for (PosteCandidatureTag tag : searchCriteria.getTags().keySet()) {
                if (searchCriteria.getTags().get(tag) != null) {
                    MapJoin<PosteCandidature, PosteCandidatureTag, PosteCandidatureTagValue> t = c.joinMap("tags");
                    predicates.add(criteriaBuilder.equal(t.key(), tag));
                    predicates.add(criteriaBuilder.equal(t.value(), searchCriteria.getTags().get(tag)));
                }
            }
        }

        query.where(criteriaBuilder.and(predicates.toArray(new Predicate[0])));
        query.select(criteriaBuilder.count(c));
        return entityManager.createQuery(query).getSingleResult();
    }

    @Override
    public long countFindPosteCandidaturesByTag(PosteCandidatureTag tag, PosteCandidatureTagValue tagValue) {
        CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
        CriteriaQuery<Long> query = criteriaBuilder.createQuery(Long.class);
        Root<PosteCandidature> c = query.from(PosteCandidature.class);

        final List<Predicate> predicates = new ArrayList<>();

        MapJoin<PosteCandidature, PosteCandidatureTag, PosteCandidatureTagValue> t = c.joinMap("tags");
        if (tag != null) {
            predicates.add(criteriaBuilder.equal(t.key(), tag));
        }
        if (tagValue != null) {
            predicates.add(criteriaBuilder.equal(t.value(), tagValue));
        }

        query.where(criteriaBuilder.and(predicates.toArray(new Predicate[0])));
        query.select(criteriaBuilder.count(c));
        return entityManager.createQuery(query).getSingleResult();
    }

    @Override
    public List<PosteCandidature> findPosteCandidaturesRecevableByPostes(Set<PosteAPourvoir> postes, Boolean auditionnable, String sortFieldName, String sortOrder) {
        CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
        CriteriaQuery<PosteCandidature> query = criteriaBuilder.createQuery(PosteCandidature.class);
        Root<PosteCandidature> c = query.from(PosteCandidature.class);

        final List<Predicate> predicates = new ArrayList<>();
        final List<Order> orders = new ArrayList<>();

        predicates.add(c.get("poste").in(postes));
        predicates.add(criteriaBuilder.equal(c.get("recevableEnum"), "RECEVABLE"));
        if (auditionnable != null) {
            predicates.add(criteriaBuilder.equal(c.get("auditionnable"), auditionnable));
        }

        if (sortFieldName != null && !sortFieldName.isEmpty()) {
            String[] sortFieldNameSplit = sortFieldName.split("\\.");
            if ("DESC".equalsIgnoreCase(sortOrder)) {
                if (sortFieldNameSplit.length < 2) {
                    orders.add(criteriaBuilder.desc(c.get(sortFieldName)));
                } else {
                    orders.add(criteriaBuilder.desc(c.join(sortFieldNameSplit[0]).get(sortFieldNameSplit[1])));
                }
            } else {
                if (sortFieldNameSplit.length < 2) {
                    orders.add(criteriaBuilder.asc(c.get(sortFieldName)));
                } else {
                    orders.add(criteriaBuilder.asc(c.join(sortFieldNameSplit[0]).get(sortFieldNameSplit[1])));
                }
            }
        }

        query.where(criteriaBuilder.and(predicates.toArray(new Predicate[0])));
        if (!orders.isEmpty()) {
            query.orderBy(orders);
        }
        query.select(c);
        return entityManager.createQuery(query).getResultList();
    }

    String computeSearchString(String searchString) {
        String[] searchStrings = StringUtils.split(searchString, " ");
        List<String> searchStringsExpr = new ArrayList<>();
        for (String s : searchStrings) {
            searchStringsExpr.add(s + ":*");
        }
        return StringUtils.join(searchStringsExpr, "|");
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
}
