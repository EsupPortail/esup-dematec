package fr.univrouen.poste.dao;

import fr.univrouen.poste.domain.MemberReviewFile;
import fr.univrouen.poste.repository.MemberReviewFileRepository;
import jakarta.annotation.Resource;
import jakarta.persistence.EntityManager;
import jakarta.persistence.Query;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class MemberReviewFileDao {

    @Resource
    MemberReviewFileRepository member_review_fileRepository;

    @Resource
    EntityManager entityManager;

    public long countMemberReviewFiles() {
        return member_review_fileRepository.count();
    }

    public List<MemberReviewFile> findAllMemberReviewFiles() {
        return member_review_fileRepository.findAll();
    }

    public List<MemberReviewFile> findAllMemberReviewFiles(String sortFieldName, String sortOrder) {
        Sort.Direction direction = "DESC".equalsIgnoreCase(sortOrder) ? Sort.Direction.DESC : Sort.Direction.ASC;
        Sort sort = Sort.by(direction, sortFieldName);
        return member_review_fileRepository.findAll(sort);
    }

    public MemberReviewFile findMemberReviewFile(Long id) {
        if (id == null) return null;
        Optional<MemberReviewFile> result = member_review_fileRepository.findById(id);
        return result.orElse(null);
    }

    public Page<MemberReviewFile> findMemberReviewFileEntries(int pageNumber, int pageSize) {
        return member_review_fileRepository.findAll(PageRequest.of(pageNumber, pageSize));
    }

    public Page<MemberReviewFile> findMemberReviewFileEntries(int pageNumber, int pageSize, String sortFieldName, String sortOrder) {
        Sort.Direction direction = "DESC".equalsIgnoreCase(sortOrder) ? Sort.Direction.DESC : Sort.Direction.ASC;
        Sort sort = Sort.by(direction, sortFieldName);
        return member_review_fileRepository.findAll(PageRequest.of(pageNumber, pageSize, sort));
    }

    public MemberReviewFile saveMemberReviewFile(MemberReviewFile member_review_file) {
        return member_review_fileRepository.save(member_review_file);
    }

    public void deleteMemberReviewFile(Long id) {
        member_review_fileRepository.deleteById(id);
    }

    public void deleteMemberReviewFile(MemberReviewFile member_review_file) {
        member_review_fileRepository.delete(member_review_file);
    }

    public void flush() {
        member_review_fileRepository.flush();
    }

    public List<Object[]> sumMemberReviewFileSizeByDate() {
        String sql = "SELECT date_part('year', send_time) as year, date_part('month', send_time) as month, date_part('day', send_time) as day, "
                + "sum(sum(file_size)) over(order by date_part('year', send_time), date_part('month', send_time), date_part('day', send_time)) as file_size_sum "
                + "from member_review_file GROUP BY year, month, day";
        Query q = entityManager.createNativeQuery(sql);
        return q.getResultList();
    }

    public  long getSumFileSize() {
        String sql = "SELECT SUM(file_size) FROM member_review_file";
        Query q = entityManager.createNativeQuery(sql);
        BigDecimal bigValue = (BigDecimal) q.getSingleResult();
        if(bigValue != null) {
            return bigValue.longValue();
        } else {
            return Long.valueOf(0);
        }
    }

}
