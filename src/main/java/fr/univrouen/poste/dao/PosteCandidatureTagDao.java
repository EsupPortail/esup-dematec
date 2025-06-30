package fr.univrouen.poste.dao;

import fr.univrouen.poste.domain.PosteCandidatureTag;
import fr.univrouen.poste.domain.PosteCandidatureTagValue;
import fr.univrouen.poste.repository.PosteCandidatureTagRepository;
import jakarta.annotation.Resource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class PosteCandidatureTagDao {

    @Resource
    PosteCandidatureTagRepository poste_candidature_tagRepository;

    public List<PosteCandidatureTag> findAllPosteCandidatureTags() {
        return poste_candidature_tagRepository.findAll();
    }

    public List<PosteCandidatureTag> findAllPosteCandidatureTags(String sortFieldName, String sortOrder) {
        Sort.Direction direction = "DESC".equalsIgnoreCase(sortOrder) ? Sort.Direction.DESC : Sort.Direction.ASC;
        Sort sort = Sort.by(direction, sortFieldName);
        return poste_candidature_tagRepository.findAll(sort);
    }

    public PosteCandidatureTag findPosteCandidatureTag(Long id) {
        if (id == null) return null;
        Optional<PosteCandidatureTag> result = poste_candidature_tagRepository.findById(id);
        return result.orElse(null);
    }

    public PosteCandidatureTag findPosteCandidatureTagsByValue(PosteCandidatureTagValue value) {
        return poste_candidature_tagRepository.findByValuesContains(value);
    }

    public Page<PosteCandidatureTag> findPosteCandidatureTagEntries(Pageable pageable) {
        return poste_candidature_tagRepository.findAll(pageable);
    }


    public PosteCandidatureTag savePosteCandidatureTag(PosteCandidatureTag poste_candidature_tag) {
        return poste_candidature_tagRepository.save(poste_candidature_tag);
    }

    public void deletePosteCandidatureTag(PosteCandidatureTag poste_candidature_tag) {
        poste_candidature_tagRepository.delete(poste_candidature_tag);
    }

    public void flush() {
        poste_candidature_tagRepository.flush();
    }
}
