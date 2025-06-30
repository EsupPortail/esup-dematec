package fr.univrouen.poste.dao;

import fr.univrouen.poste.domain.AppliConfigFileType;
import fr.univrouen.poste.domain.PosteCandidatureFile;
import fr.univrouen.poste.repository.PosteCandidatureFileRepository;
import jakarta.annotation.Resource;
import jakarta.persistence.EntityManager;
import jakarta.persistence.Query;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class PosteCandidatureFileDao {

    @Resource
    PosteCandidatureFileRepository poste_candidature_fileRepository;

    @Resource
    EntityManager entityManager;

    public long countPosteCandidatureFiles() {
        return poste_candidature_fileRepository.count();
    }

    public List<PosteCandidatureFile> findAllPosteCandidatureFiles() {
        return poste_candidature_fileRepository.findAll();
    }

    public PosteCandidatureFile findPosteCandidatureFile(Long id) {
        if (id == null) return null;
        Optional<PosteCandidatureFile> result = poste_candidature_fileRepository.findById(id);
        return result.orElse(null);
    }

    public long countFindPosteCandidatureFilesByFileType(AppliConfigFileType fileType) {
        return poste_candidature_fileRepository.countByFileType(fileType);
    }

    public void flush() {
        poste_candidature_fileRepository.flush();
    }

    public static String readableFileSize(long size) {
        if (size <= 0) return "0";
        final String[] units = new String[] { "B", "KB", "MB", "GB", "TB" };
        int digitGroups = (int) (Math.log10(size) / Math.log10(1024));
        return new DecimalFormat("#,##0.#").format(size / Math.pow(1024, digitGroups)) + " " + units[digitGroups];
    }

    public String getMaxFileSize() {
        List<PosteCandidatureFile> files = entityManager.createQuery("SELECT o FROM PosteCandidatureFile o order by o.fileSize desc ", PosteCandidatureFile.class).setMaxResults(1).getResultList();
        if (!files.isEmpty()) return files.get(0).getFileSizeFormatted(); else return "Nan";
    }

    public Long getSumFileSize() {
        String sql = "SELECT SUM(file_size) FROM poste_candidature_file";
        Query q = entityManager.createNativeQuery(sql);
        BigDecimal bigValue = (BigDecimal)q.getSingleResult();
        if(bigValue != null) {
            return bigValue.longValue();
        } else {
            return Long.valueOf(0);
        }
    }

    public Long getSumNbPages() {
        String sql = "SELECT SUM(nb_pages) FROM poste_candidature_file";
        Query q = entityManager.createNativeQuery(sql);
        BigDecimal bigValue = (BigDecimal)q.getSingleResult();
        if(bigValue != null) {
            return bigValue.longValue();
        } else {
            return Long.valueOf(0);
        }
    }

    public List<Object[]> sumPosteCandidatureFileSizeByDate() {
        String sql = "SELECT date_part('year', send_time) as year, date_part('month', send_time) as month, date_part('day', send_time) as day, "
                + "sum(sum(file_size)) over(order by date_part('year', send_time), date_part('month', send_time), date_part('day', send_time)) as file_size_sum "
                + "from poste_candidature_file GROUP BY year, month, day";
        Query q = entityManager.createNativeQuery(sql);
        return q.getResultList();
    }

}
