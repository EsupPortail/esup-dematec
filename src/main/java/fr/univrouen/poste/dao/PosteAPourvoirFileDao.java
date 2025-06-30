package fr.univrouen.poste.dao;

import fr.univrouen.poste.domain.PosteAPourvoirFile;
import fr.univrouen.poste.repository.PosteAPourvoirFileRepository;
import jakarta.persistence.EntityManager;
import jakarta.persistence.Query;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class PosteAPourvoirFileDao {

    @Autowired
    PosteAPourvoirFileRepository poste_a_pourvoir_fileRepository;

    @Autowired
    EntityManager entityManager;

    public long countPosteAPourvoirFiles() {
        return poste_a_pourvoir_fileRepository.count();
    }

    public List<PosteAPourvoirFile> findAllPosteAPourvoirFiles() {
        return poste_a_pourvoir_fileRepository.findAll();
    }

    public List<PosteAPourvoirFile> findAllPosteAPourvoirFiles(String sortFieldName, String sortOrder) {
        Sort.Direction direction = "DESC".equalsIgnoreCase(sortOrder) ? Sort.Direction.DESC : Sort.Direction.ASC;
        Sort sort = Sort.by(direction, sortFieldName);
        return poste_a_pourvoir_fileRepository.findAll(sort);
    }

    public PosteAPourvoirFile findPosteAPourvoirFile(Long id) {
        if (id == null) return null;
        Optional<PosteAPourvoirFile> result = poste_a_pourvoir_fileRepository.findById(id);
        return result.orElse(null);
    }

    public Page<PosteAPourvoirFile> findPosteAPourvoirFileEntries(Pageable pageable) {
        return poste_a_pourvoir_fileRepository.findAll(pageable);
    }

    public Page<PosteAPourvoirFile> findPosteAPourvoirFileEntries(Pageable pageable, String sortFieldName, String sortOrder) {
        if (sortFieldName != null && sortOrder != null) {
            Sort.Direction direction = "DESC".equalsIgnoreCase(sortOrder) ? Sort.Direction.DESC : Sort.Direction.ASC;
            Sort sort = Sort.by(direction, sortFieldName);
            return poste_a_pourvoir_fileRepository.findAll(PageRequest.of(pageable.getPageNumber(), pageable.getPageSize(), sort));
        }
        return poste_a_pourvoir_fileRepository.findAll(pageable);
    }

    public PosteAPourvoirFile savePosteAPourvoirFile(PosteAPourvoirFile poste_a_pourvoir_file) {
        return poste_a_pourvoir_fileRepository.save(poste_a_pourvoir_file);
    }

    public void deletePosteAPourvoirFile(Long id) {
        poste_a_pourvoir_fileRepository.deleteById(id);
    }

    public void deletePosteAPourvoirFile(PosteAPourvoirFile poste_a_pourvoir_file) {
        poste_a_pourvoir_fileRepository.delete(poste_a_pourvoir_file);
    }

    public void flush() {
        poste_a_pourvoir_fileRepository.flush();
    }


    public List<Object[]> sumPosteAPourvoirFileSizeByDate() {
        String sql = "SELECT date_part('year', send_time) as year, date_part('month', send_time) as month, date_part('day', send_time) as day, "
                + "sum(sum(file_size)) over(order by date_part('year', send_time), date_part('month', send_time), date_part('day', send_time)) as file_size_sum "
                + "from posteapourvoir_file GROUP BY year, month, day";
        Query q = entityManager.createNativeQuery(sql);
        return q.getResultList();
    }

    public long getSumFileSize() {
        String sql = "SELECT SUM(file_size) FROM posteapourvoir_file";
        Query q = entityManager.createNativeQuery(sql);
        BigDecimal bigValue = (BigDecimal)q.getSingleResult();
        if(bigValue != null) {
            return bigValue.longValue();
        } else {
            return Long.valueOf(0);
        }
    }
}
