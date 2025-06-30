package fr.univrouen.poste.dao;

import fr.univrouen.poste.domain.LogPosteFile;
import fr.univrouen.poste.repository.LogPosteFileRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class LogPosteFileDao {

    @Autowired
    LogPosteFileRepository log_poste_fileRepository;

    public long countLogPosteFiles() {
        return log_poste_fileRepository.count();
    }

    public List<LogPosteFile> findAllLogPosteFiles() {
        return log_poste_fileRepository.findAll();
    }

    public List<LogPosteFile> findAllLogPosteFiles(String sortFieldName, String sortOrder) {
        Sort.Direction direction = "DESC".equalsIgnoreCase(sortOrder) ? Sort.Direction.DESC : Sort.Direction.ASC;
        Sort sort = Sort.by(direction, sortFieldName);
        return log_poste_fileRepository.findAll(sort);
    }

    public LogPosteFile findLogPosteFile(Long id) {
        if (id == null) return null;
        Optional<LogPosteFile> result = log_poste_fileRepository.findById(id);
        return result.orElse(null);
    }

    public Page<LogPosteFile> findLogPosteFileEntries(Pageable pageable) {
        return log_poste_fileRepository.findAll(pageable);
    }

    public Page<LogPosteFile> findLogPosteFileEntries(Pageable pageable, String sortFieldName, String sortOrder) {
        if (sortFieldName != null && sortOrder != null) {
            Sort.Direction direction = "DESC".equalsIgnoreCase(sortOrder) ? Sort.Direction.DESC : Sort.Direction.ASC;
            Sort sort = Sort.by(direction, sortFieldName);
            return log_poste_fileRepository.findAll(PageRequest.of(pageable.getPageNumber(), pageable.getPageSize(), sort));
        }
        return log_poste_fileRepository.findAll(pageable);
    }

    public LogPosteFile saveLogPosteFile(LogPosteFile log_poste_file) {
        return log_poste_fileRepository.save(log_poste_file);
    }

    public void deleteLogPosteFile(Long id) {
        log_poste_fileRepository.deleteById(id);
    }

    public void deleteLogPosteFile(LogPosteFile log_poste_file) {
        log_poste_fileRepository.delete(log_poste_file);
    }

    public void flush() {
        log_poste_fileRepository.flush();
    }

    public List<LogPosteFile> findLogPosteFiles(String action2, String userId2, String sortFieldName, String sortOrder) {
        return log_poste_fileRepository.findLogPosteFiles(action2, userId2, sortFieldName, sortOrder);
    }

    public long countFindLogPosteFiles(String action2, String userId2) {
        return log_poste_fileRepository.countFindLogPosteFiles(action2, userId2);
    }
}
