package fr.univrouen.poste.repository;

import fr.univrouen.poste.domain.PosteAPourvoir;
import fr.univrouen.poste.domain.PosteCandidature;
import fr.univrouen.poste.domain.PosteCandidatureTag;
import fr.univrouen.poste.domain.PosteCandidatureTagValue;
import fr.univrouen.poste.web.searchcriteria.PosteCandidatureSearchCriteria;

import java.util.List;
import java.util.Set;

public interface PosteCandidatureRepositoryCustom {
    List<PosteCandidature> findPostesCandidatures(PosteCandidatureSearchCriteria searchCriteria, String sortFieldName, String sortOrder);
    long countFindPosteCandidatures(PosteCandidatureSearchCriteria searchCriteria);
    long countFindPosteCandidaturesByTag(PosteCandidatureTag tag, PosteCandidatureTagValue tagValue);
    List<PosteCandidature> findPosteCandidaturesRecevableByPostes(Set<PosteAPourvoir> postes, Boolean auditionnable, String sortFieldName, String sortOrder);
}
