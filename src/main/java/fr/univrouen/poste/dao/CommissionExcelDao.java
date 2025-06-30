package fr.univrouen.poste.dao;

import fr.univrouen.poste.domain.CommissionExcel;
import fr.univrouen.poste.repository.CommissionExcelRepository;
import jakarta.annotation.Resource;
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
public class CommissionExcelDao {

    @Resource
    CommissionExcelRepository commission_excelRepository;

    public long countCommissionExcels() {
        return commission_excelRepository.count();
    }

    public List<CommissionExcel> findAllCommissionExcels() {
        return commission_excelRepository.findAll();
    }

    public List<CommissionExcel> findAllCommissionExcels(String sortFieldName, String sortOrder) {
        Sort.Direction direction = "DESC".equalsIgnoreCase(sortOrder) ? Sort.Direction.DESC : Sort.Direction.ASC;
        Sort sort = Sort.by(direction, sortFieldName);
        return commission_excelRepository.findAll(sort);
    }

    public CommissionExcel findCommissionExcel(Long id) {
        if (id == null) return null;
        Optional<CommissionExcel> result = commission_excelRepository.findById(id);
        return result.orElse(null);
    }

    public Page<CommissionExcel> findCommissionExcelEntries(Pageable pageable) {
        return commission_excelRepository.findAll(pageable);
    }

    public Page<CommissionExcel> findCommissionExcelEntries(Pageable pageable, String sortFieldName, String sortOrder) {
        if (sortFieldName != null && sortOrder != null) {
            Sort.Direction direction = "DESC".equalsIgnoreCase(sortOrder) ? Sort.Direction.DESC : Sort.Direction.ASC;
            Sort sort = Sort.by(direction, sortFieldName);
            return commission_excelRepository.findAll(PageRequest.of(pageable.getPageNumber(), pageable.getPageSize(), sort));
        }
        return commission_excelRepository.findAll(pageable);
    }

    public CommissionExcel saveCommissionExcel(CommissionExcel commission_excel) {
        return commission_excelRepository.save(commission_excel);
    }

    public void deleteCommissionExcel(Long id) {
        commission_excelRepository.deleteById(id);
    }

    public void deleteCommissionExcel(CommissionExcel commission_excel) {
        commission_excelRepository.delete(commission_excel);
    }

    public void flush() {
        commission_excelRepository.flush();
    }
}
