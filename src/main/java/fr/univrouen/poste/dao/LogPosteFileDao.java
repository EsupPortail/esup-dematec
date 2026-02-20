package fr.univrouen.poste.dao;

import fr.univrouen.poste.domain.LogPosteFile;
import fr.univrouen.poste.repository.LogPosteFileRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@Transactional
public class LogPosteFileDao {

    @Autowired
    LogPosteFileRepository log_poste_fileRepository;

    public LogPosteFile findLogPosteFile(Long id) {
        if (id == null) return null;
        Optional<LogPosteFile> result = log_poste_fileRepository.findById(id);
        return result.orElse(null);
    }

    public Page<LogPosteFile> findLogPosteFileEntries(Pageable pageable) {
        return log_poste_fileRepository.findAll(pageable);
    }

    public LogPosteFile saveLogPosteFile(LogPosteFile log_poste_file) {
        return log_poste_fileRepository.save(log_poste_file);
    }
    public void flush() {
        log_poste_fileRepository.flush();
    }


}
