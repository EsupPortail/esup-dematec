package fr.univrouen.poste.dao;

import fr.univrouen.poste.domain.ManagerReview;
import fr.univrouen.poste.repository.ManagerReviewRepository;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class ManagerReviewDao {

    @Resource
    ManagerReviewRepository manager_reviewRepository;

    public ManagerReview saveManagerReview(ManagerReview manager_review) {
        return manager_reviewRepository.save(manager_review);
    }

    public void flush() {
        manager_reviewRepository.flush();
    }
}
