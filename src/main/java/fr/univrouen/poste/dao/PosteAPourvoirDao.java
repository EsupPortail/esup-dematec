package fr.univrouen.poste.dao;

import fr.univrouen.poste.domain.PosteAPourvoir;
import fr.univrouen.poste.domain.User;
import fr.univrouen.poste.repository.PosteAPourvoirRepository;
import jakarta.annotation.Resource;
import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class PosteAPourvoirDao {

    public static final List<String> fieldNames4OrderClauseFilter = java.util.Arrays.asList("numEmploi", "profil", "localisation", "membres", "presidents", "posteFiles", "dateEndCandidatAuditionnable", "dateEndSignupCandidat");

    @Resource
    PosteAPourvoirRepository posteAPourvoirRepository;

    @Resource
    EntityManager entityManager;

    public long countPosteAPourvoirs() {
        return posteAPourvoirRepository.count();
    }

    public List<PosteAPourvoir> findAllPosteAPourvoirs() {
        return posteAPourvoirRepository.findAll();
    }

    public List<String> findAllPosteAPourvoirNumEplois() {
        return posteAPourvoirRepository.findAll().stream()
                .map(PosteAPourvoir::getNumEmploi)
                .sorted()
                .toList();
    }

    public List<PosteAPourvoir> findAllPosteAPourvoirs(String sortFieldName, String sortOrder) {
        Sort.Direction direction = "DESC".equalsIgnoreCase(sortOrder) ? Sort.Direction.DESC : Sort.Direction.ASC;
        Sort sort = Sort.by(direction, sortFieldName);
        return posteAPourvoirRepository.findAll(sort);
    }

    public PosteAPourvoir findPosteAPourvoir(Long id) {
        if (id == null) return null;
        Optional<PosteAPourvoir> result = posteAPourvoirRepository.findById(id);
        return result.orElse(null);
    }

    public List<PosteAPourvoir> findPosteAPourvoirsByNumEmplois(List<String> numEmplois) {
        if (numEmplois == null) throw new IllegalArgumentException("The numEmplois argument is required");
        return posteAPourvoirRepository.findByNumEmploiIn(numEmplois).stream()
                .sorted((a, b) -> a.getNumEmploi().compareTo(b.getNumEmploi()))
                .toList();
    }

    public List<PosteAPourvoir> findPosteAPourvoirsByMembre(User membre) {
        if (membre == null) throw new IllegalArgumentException("The membre argument is required");
        return posteAPourvoirRepository.findByMembresContains(membre).stream()
                .sorted((a, b) -> a.getNumEmploi().compareTo(b.getNumEmploi()))
                .toList();
    }

    public Page<PosteAPourvoir> findPosteAPourvoirEntries(Pageable pageable, String sortFieldName, String sortOrder) {
        if (sortFieldName != null && sortOrder != null) {
            Sort.Direction direction = "DESC".equalsIgnoreCase(sortOrder) ? Sort.Direction.DESC : Sort.Direction.ASC;
            Sort sort = Sort.by(direction, sortFieldName);
            return posteAPourvoirRepository.findAll(PageRequest.of(pageable.getPageNumber(), pageable.getPageSize(), sort));
        }
        return posteAPourvoirRepository.findAll(pageable);
    }

    public TypedQuery<PosteAPourvoir> findPosteAPourvoirsByDateEndSignupCandidatGreaterThan(Date dateEndSignupCandidat) {
        if (dateEndSignupCandidat == null) throw new IllegalArgumentException("The dateEndSignupCandidat argument is required");
        TypedQuery<PosteAPourvoir> q = entityManager.createQuery("SELECT o FROM PosteAPourvoir AS o WHERE o.dateEndSignupCandidat > :dateEndSignupCandidat", PosteAPourvoir.class);
        q.setParameter("dateEndSignupCandidat", dateEndSignupCandidat);
        return q;
    }

    public void flush() {
        posteAPourvoirRepository.flush();
    }

    public TypedQuery<PosteAPourvoir> findPosteAPourvoirsByNumEmploi(String numEmploi, String sortFieldName, String sortOrder) {
        if (numEmploi == null || numEmploi.length() == 0) throw new IllegalArgumentException("The numEmploi argument is required");
        StringBuilder queryBuilder = new StringBuilder("SELECT o FROM PosteAPourvoir AS o WHERE o.numEmploi = :numEmploi");
        if (fieldNames4OrderClauseFilter.contains(sortFieldName)) {
            queryBuilder.append(" ORDER BY ").append(sortFieldName);
            if ("ASC".equalsIgnoreCase(sortOrder) || "DESC".equalsIgnoreCase(sortOrder)) {
                queryBuilder.append(" ").append(sortOrder);
            }
        }
        TypedQuery<PosteAPourvoir> q = entityManager.createQuery(queryBuilder.toString(), PosteAPourvoir.class);
        q.setParameter("numEmploi", numEmploi);
        return q;
    }

    public void savePosteAPourvoir(PosteAPourvoir poste) {
        posteAPourvoirRepository.save(poste);
    }
}
