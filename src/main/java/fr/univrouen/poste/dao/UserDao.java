package fr.univrouen.poste.dao;

import fr.univrouen.poste.domain.PosteCandidature;
import fr.univrouen.poste.domain.User;
import fr.univrouen.poste.exceptions.EsupDematEcException;
import fr.univrouen.poste.repository.UserRepository;
import jakarta.annotation.Resource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class UserDao {

    @Resource
    UserRepository userRepository;

    @Resource
    PosteCandidatureDao posteCandidatureDao;

    public long countUsers() {
        return userRepository.count();
    }

    public long countActifCUsers() {
        return userRepository.countByEnabled(true);
    }

    public long countAdmins() {
        return userRepository.countByIsAdmin(true);
    }

    public long countSuperManagers() {
        return userRepository.countByIsSuperManager(true);
    }

    public long countManagers() {
        return userRepository.countByIsManager(true);
    }

    public long countCandidats() {
        return userRepository.findAll().stream()
                .filter(u -> u.getNumCandidat() != null && !u.getNumCandidat().isEmpty())
                .count();
    }

    public long countActifCandidats() {
        return userRepository.findAll().stream()
                .filter(u -> u.getNumCandidat() != null && !u.getNumCandidat().isEmpty() && u.getActivationDate() != null)
                .count();
    }

    public long countMembres() {
        return userRepository.findAll().stream()
                .filter(u -> u.getPostes() != null && !u.getPostes().isEmpty())
                .count();
    }

    public List<User> findAllUsers() {
        return userRepository.findAll();
    }

    public List<User> findAllUsers(String sortFieldName, String sortOrder) {
        Sort.Direction direction = "DESC".equalsIgnoreCase(sortOrder) ? Sort.Direction.DESC : Sort.Direction.ASC;
        Sort sort = Sort.by(direction, sortFieldName);
        return userRepository.findAll(sort);
    }

    public List<User> findAllCandidats(String sortFieldName, String sortOrder) {
        if (sortFieldName == null) sortFieldName = "emailAddress";
        Sort.Direction direction = "DESC".equalsIgnoreCase(sortOrder) ? Sort.Direction.DESC : Sort.Direction.ASC;
        Sort sort = Sort.by(direction, sortFieldName);
        return userRepository.findAll(sort);
    }

    public List<User> findAllMembres(String sortFieldName, String sortOrder) {
        if (sortFieldName == null) sortFieldName = "emailAddress";
        Sort.Direction direction = "DESC".equalsIgnoreCase(sortOrder) ? Sort.Direction.DESC : Sort.Direction.ASC;
        Sort sort = Sort.by(direction, sortFieldName);
        return userRepository.findAll(sort);
    }

    public List<User> findAllNoCandidatsAndNoManagers() {
        return userRepository.findAll().stream()
                .filter(u -> (u.getNumCandidat() == null || u.getNumCandidat().isEmpty()) &&
                        (u.getIsManager() == null || !u.getIsManager())
                        && (u.getIsSuperManager() == null || !u.getIsSuperManager())
                        && (u.getIsAdmin() == null || !u.getIsAdmin())
                )
                .sorted((a, b) -> a.getEmailAddress().compareTo(b.getEmailAddress()))
                .toList();
    }

    public List<String> findAllUserIds() {
        return userRepository.findAll().stream()
                .map(User::getEmailAddress)
                .sorted()
                .toList();
    }

    public List<String> findAllUserNoms() {
        return userRepository.findAll().stream()
                .map(User::getNom)
                .filter(nom -> nom != null && !nom.isEmpty())
                .distinct()
                .sorted()
                .toList();
    }

    public List<String> findAllCandidatsIds() {
        return userRepository.findAll().stream()
                .filter(u -> u.getNumCandidat() != null && !u.getNumCandidat().isEmpty())
                .map(User::getEmailAddress)
                .sorted()
                .toList();
    }

    public List<User> findUsersByEmailAddresses(List<String> emails) {
        if (emails == null) throw new IllegalArgumentException("The emails argument is required");
        return userRepository.findAll().stream()
                .filter(u -> emails.contains(u.getEmailAddress()))
                .sorted((a, b) -> a.getEmailAddress().compareTo(b.getEmailAddress()))
                .toList();
    }

    public User findUser(Long id) {
        if (id == null) return null;
        Optional<User> result = userRepository.findById(id);
        return result.orElse(null);
    }

    public User findUserByEmailAddress(String emailAddress) {
        if (emailAddress == null || emailAddress.isEmpty()) throw new IllegalArgumentException("The emailAddress argument is required");
        return userRepository.findByEmailAddressIgnoreCase(emailAddress).orElse(null);
    }

    public List<User> findUsersByActivationKey(String activationKey) {
        if (activationKey == null || activationKey.isEmpty()) throw new IllegalArgumentException("The activationKey argument is required");
        return userRepository.findByActivationKey(activationKey);
    }

    public List<User> findUsersByActivationKeyAndEmailAddress(String activationKey, String emailAddress) {
        if (activationKey == null || activationKey.isEmpty()) throw new IllegalArgumentException("The activationKey argument is required");
        if (emailAddress == null || emailAddress.isEmpty()) throw new IllegalArgumentException("The emailAddress argument is required");
        return userRepository.findByActivationKeyIgnoreCaseAndEmailAddressIgnoreCase(activationKey, emailAddress);
    }

    public List<User> findUsersByEmailAddressAndActivationDateIsNotNull(String emailAddress) {
        if (emailAddress == null || emailAddress.isEmpty()) throw new IllegalArgumentException("The emailAddress argument is required");
        return userRepository.findByEmailAddressIgnoreCaseAndActivationDateIsNotNull(emailAddress);
    }

    public List<User> findUsersByIsAdmin(Boolean isAdmin) {
        if (isAdmin == null) throw new IllegalArgumentException("The isAdmin argument is required");
        return userRepository.findByIsAdmin(isAdmin);
    }

    public List<User> findUsersByIsManager(Boolean isManager) {
        if (isManager == null) throw new IllegalArgumentException("The isManager argument is required");
        return userRepository.findByIsManager(isManager);
    }

    public List<User> findUsersByIsSuperManager(Boolean isSuperManager) {
        if (isSuperManager == null) throw new IllegalArgumentException("The isSuperManager argument is required");
        return userRepository.findByIsSuperManager(isSuperManager);
    }

    public User findUserByNumCandidat(String numCandidat) {
        if (numCandidat == null || numCandidat.isEmpty()) throw new IllegalArgumentException("The numCandidat argument is required");
        return userRepository.findByNumCandidat(numCandidat).orElse(null);
    }

    public long countFindUsersByActivationKey(String activationKey) {
        if (activationKey == null || activationKey.isEmpty()) throw new IllegalArgumentException("The activationKey argument is required");
        return userRepository.countByActivationKey(activationKey);
    }

    public long countFindUsersByEmailAddress(String emailAddress) {
        if (emailAddress == null || emailAddress.isEmpty()) throw new IllegalArgumentException("The emailAddress argument is required");
        return userRepository.countByEmailAddressIgnoreCase(emailAddress);
    }

    public Page<User> findUserEntries(String status, String nomOrPrenomOrEmailAddress, Pageable pageable) {
        if( status != null && !status.isEmpty()) {
            if(nomOrPrenomOrEmailAddress == null) {
                nomOrPrenomOrEmailAddress = "";
            }
            if (status.equals("Admin")) {
                return userRepository.findByIsAdminTrueAndNomContainingOrPrenomContainingOrEmailAddressContainingAllIgnoreCase(
                        nomOrPrenomOrEmailAddress, pageable);
            } else if (status.equals("Manager")) {
                return userRepository.findByIsManagerTrueAndNomContainingOrPrenomContainingOrEmailAddressContainingAllIgnoreCase(
                        nomOrPrenomOrEmailAddress, pageable);
            } else if (status.equals("SuperManager")) {
                return userRepository.findByIsSuperManagerTrueAndNomContainingOrPrenomContainingOrEmailAddressContainingAllIgnoreCase(
                        nomOrPrenomOrEmailAddress, pageable);
            } else if (status.equals("Membre")) {
                return userRepository.findByPostesIsNotEmptyAndNomContainingOrPrenomContainingOrEmailAddressContainingAllIgnoreCase(
                        nomOrPrenomOrEmailAddress, pageable);
            } else if (status.equals("Candidat")) {
                return userRepository.findByNumCandidatIsNotNullAndNomContainingOrPrenomContainingOrEmailAddressContainingAllIgnoreCase(
                        nomOrPrenomOrEmailAddress, pageable);
            }
        } else {
            if (nomOrPrenomOrEmailAddress != null && !nomOrPrenomOrEmailAddress.isEmpty()) {
                return userRepository.findByNomContainingOrPrenomContainingOrEmailAddressContainingAllIgnoreCase(
                        nomOrPrenomOrEmailAddress, pageable);
            }
        }
        return userRepository.findAll(pageable);
    }

    public User saveUser(User user) {
        return userRepository.save(user);
    }

    public void deleteUser(User user) {
        Page<PosteCandidature> candidatures = posteCandidatureDao.findPosteCandidaturesByCandidat(user);
        if(candidatures.getContent().size() > 0) {
            throw new EsupDematEcException("Impossible de supprimer l'utilisateur " + user.getEmailAddress() + " car il est candidat sur des postes.");
        }
        if(user.getPostes() != null && user.getPostes().size() > 0) {
            throw new EsupDematEcException("Impossible de supprimer l'utilisateur " + user.getEmailAddress() + " car il est membre sur des postes.");
        }
        userRepository.delete(user);
    }

    public void flush() {
        userRepository.flush();
    }

    public User findUsersByEmailAddress(String email) {
        return userRepository.findByEmailAddressIgnoreCase(email).orElse(null);
    }


    /**
     * @return true if one of his candidatures has a modification date not nul
     */
    public Boolean isCandidatActif(User user) {
        Page<PosteCandidature> candidatures = posteCandidatureDao.findPosteCandidaturesByCandidat(user);
        for(PosteCandidature candidature: candidatures) {
            if(candidature.getModification() != null) {
                return true;
            }
        }
        return false;
    }

    public Long countSupermanagers() {
        return userRepository.countByIsSuperManager(true);
    }
}
