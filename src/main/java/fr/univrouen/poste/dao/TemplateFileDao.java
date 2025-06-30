package fr.univrouen.poste.dao;

import fr.univrouen.poste.domain.TemplateFile;
import fr.univrouen.poste.repository.TemplateFileRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import fr.univrouen.poste.domain.TemplateFile.TemplateFileType;

import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class TemplateFileDao {

    @Autowired
    TemplateFileRepository template_fileRepository;

    public long countTemplateFiles() {
        return template_fileRepository.count();
    }

    public List<TemplateFile> findAllTemplateFiles() {
        return template_fileRepository.findAll();
    }

    public List<TemplateFile> findAllTemplateFiles(String sortFieldName, String sortOrder) {
        Sort.Direction direction = "DESC".equalsIgnoreCase(sortOrder) ? Sort.Direction.DESC : Sort.Direction.ASC;
        Sort sort = Sort.by(direction, sortFieldName);
        return template_fileRepository.findAll(sort);
    }

    public TemplateFile findTemplateFile(Long id) {
        if (id == null) return null;
        Optional<TemplateFile> result = template_fileRepository.findById(id);
        return result.orElse(null);
    }

    public List<TemplateFile> findTemplateFilesByTemplateFileType(TemplateFileType templateFileType) {
        return template_fileRepository.findByTemplateFileType(templateFileType);
    }

    public List<TemplateFile> findTemplateFilesByTemplateFileType(TemplateFileType templateFileType, String sortFieldName, String sortOrder) {
        Sort.Direction direction = "DESC".equalsIgnoreCase(sortOrder) ? Sort.Direction.DESC : Sort.Direction.ASC;
        Sort sort = Sort.by(direction, sortFieldName);
        return template_fileRepository.findByTemplateFileType(templateFileType, sort);
    }

    public Page<TemplateFile> findTemplateFileEntries(int pageNumber, int pageSize) {
        return template_fileRepository.findAll(PageRequest.of(pageNumber, pageSize));
    }

    public Page<TemplateFile> findTemplateFileEntries(int pageNumber, int pageSize, String sortFieldName, String sortOrder) {
        Sort.Direction direction = "DESC".equalsIgnoreCase(sortOrder) ? Sort.Direction.DESC : Sort.Direction.ASC;
        Sort sort = Sort.by(direction, sortFieldName);
        return template_fileRepository.findAll(PageRequest.of(pageNumber, pageSize, sort));
    }

    public TemplateFile saveTemplateFile(TemplateFile template_file) {
        return template_fileRepository.save(template_file);
    }

    public void deleteTemplateFile(Long id) {
        template_fileRepository.deleteById(id);
    }

    public void deleteTemplateFile(TemplateFile template_file) {
        template_fileRepository.delete(template_file);
    }

    public void flush() {
        template_fileRepository.flush();
    }
}
