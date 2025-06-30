package fr.univrouen.poste.dao;

import fr.univrouen.poste.domain.PosteCandidatureTagValue;
import fr.univrouen.poste.repository.PosteCandidatureTagValueRepository;
import jakarta.annotation.Resource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class PosteCandidatureTagValueDao {

    @Resource
    PosteCandidatureTagValueRepository poste_candidature_tag_valueRepository;

    public List<PosteCandidatureTagValue> findAllPosteCandidatureTagValues() {
        return poste_candidature_tag_valueRepository.findAll();
    }

    public PosteCandidatureTagValue findPosteCandidatureTagValue(Long id) {
        if (id == null) return null;
        Optional<PosteCandidatureTagValue> result = poste_candidature_tag_valueRepository.findById(id);
        return result.orElse(null);
    }

    public Page<PosteCandidatureTagValue> findPosteCandidatureTagValueEntries(Pageable pageable) {
        return poste_candidature_tag_valueRepository.findAll(pageable);
    }

    public PosteCandidatureTagValue savePosteCandidatureTagValue(PosteCandidatureTagValue poste_candidature_tag_value) {
        return poste_candidature_tag_valueRepository.save(poste_candidature_tag_value);
    }

    public void deletePosteCandidatureTagValue(PosteCandidatureTagValue poste_candidature_tag_value) {
        poste_candidature_tag_valueRepository.delete(poste_candidature_tag_value);
    }

    public void flush() {
        poste_candidature_tag_valueRepository.flush();
    }
}
