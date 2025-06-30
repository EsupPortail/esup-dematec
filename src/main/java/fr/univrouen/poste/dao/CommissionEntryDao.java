package fr.univrouen.poste.dao;

import fr.univrouen.poste.domain.CommissionEntry;
import fr.univrouen.poste.domain.User;
import fr.univrouen.poste.repository.CommissionEntryRepository;
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
public class CommissionEntryDao {

    @Resource
    CommissionEntryRepository commission_entryRepository;

    public List<CommissionEntry> findAllCommissionEntrys() {
        return commission_entryRepository.findAll();
    }

    public List<CommissionEntry> findAllCommissionEntrys(String sortFieldName, String sortOrder) {
        Sort.Direction direction = "DESC".equalsIgnoreCase(sortOrder) ? Sort.Direction.DESC : Sort.Direction.ASC;
        Sort sort = Sort.by(direction, sortFieldName);
        return commission_entryRepository.findAll(sort);
    }

    public CommissionEntry findCommissionEntry(Long id) {
        if (id == null) return null;
        Optional<CommissionEntry> result = commission_entryRepository.findById(id);
        return result.orElse(null);
    }

    public List<CommissionEntry> findCommissionEntrysByNumPosteAndEmail(String numPoste, String email) {
        if (numPoste == null || numPoste.isEmpty()) throw new IllegalArgumentException("The numPoste argument is required");
        if (email == null || email.isEmpty()) throw new IllegalArgumentException("The email argument is required");
        return commission_entryRepository.findByNumPosteAndEmail(numPoste, email);
    }

    public List<CommissionEntry> findCommissionEntrysByMembre(User membre) {
        if (membre == null) throw new IllegalArgumentException("The membre argument is required");
        return commission_entryRepository.findByMembre(membre);
    }

    public List<CommissionEntry> findCommissionEntrysByMembreIsNull() {
        return commission_entryRepository.findByMembreIsNull();
    }

    public List<CommissionEntry> findCommissionEntrysByPosteIsNull() {
        return commission_entryRepository.findByPosteIsNull();
    }

    public Page<CommissionEntry> findCommissionEntryEntries(Pageable pageable, String sortFieldName, String sortOrder) {
        if (pageable == null) {
            return commission_entryRepository.findAll(PageRequest.of(0, 10));
        }
        if (sortFieldName != null && sortOrder != null) {
            Sort.Direction direction = "DESC".equalsIgnoreCase(sortOrder) ? Sort.Direction.DESC : Sort.Direction.ASC;
            Sort sort = Sort.by(direction, sortFieldName);
            return commission_entryRepository.findAll(PageRequest.of(pageable.getPageNumber(), pageable.getPageSize(), sort));
        }
        return commission_entryRepository.findAll(pageable);
    }

    public CommissionEntry saveCommissionEntry(CommissionEntry commission_entry) {
        return commission_entryRepository.save(commission_entry);
    }

    public void deleteCommissionEntry(Long id) {
        commission_entryRepository.deleteById(id);
    }

    public void deleteCommissionEntry(CommissionEntry commission_entry) {
        commission_entryRepository.delete(commission_entry);
    }

    public void flush() {
        commission_entryRepository.flush();
    }
}
