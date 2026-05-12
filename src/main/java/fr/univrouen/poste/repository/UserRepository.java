package fr.univrouen.poste.repository;

import fr.univrouen.poste.domain.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByEmailAddressIgnoreCase(String emailAddress);
    List<User> findByActivationKey(String activationKey);
    List<User> findByActivationKeyIgnoreCaseAndEmailAddressIgnoreCase(String activationKey, String emailAddress);
    List<User> findByEmailAddressIgnoreCaseAndActivationDateIsNotNull(String emailAddress);
    List<User> findByIsAdmin(Boolean isAdmin);
    List<User> findByIsManager(Boolean isManager);
    List<User> findByIsSuperManager(Boolean isSuperManager);
    Optional<User> findByNumCandidat(String numCandidat);
    long countByActivationKey(String activationKey);
    long countByActivationKeyIgnoreCaseAndEmailAddressIgnoreCase(String activationKey, String emailAddress);
    long countByEmailAddressIgnoreCase(String emailAddress);
    long countByEmailAddressIgnoreCaseAndActivationDateIsNotNull(String emailAddress);
    long countByIsAdmin(Boolean isAdmin);
    long countByIsManager(Boolean isManager);
    long countByIsSuperManager(Boolean isSuperManager);
    long countByNumCandidat(String numCandidat);
    long countByEnabled(Boolean enabled);

    @Query("""
    SELECT u FROM User u
    WHERE u.isAdmin = true
      AND (LOWER(u.nom) LIKE LOWER(CONCAT('%', :term, '%'))
        OR LOWER(u.prenom) LIKE LOWER(CONCAT('%', :term, '%'))
        OR LOWER(u.emailAddress) LIKE LOWER(CONCAT('%', :term, '%')))
    """)
    Page<User> findByIsAdminTrueAndNomContainingOrPrenomContainingOrEmailAddressContainingAllIgnoreCase(@Param("term") String nomOrPrenomOrEmailAddress, Pageable pageable);

    @Query("""
    SELECT u FROM User u
    WHERE u.isManager = true
      AND (LOWER(u.nom) LIKE LOWER(CONCAT('%', :term, '%'))
        OR LOWER(u.prenom) LIKE LOWER(CONCAT('%', :term, '%'))
        OR LOWER(u.emailAddress) LIKE LOWER(CONCAT('%', :term, '%')))
    """)
    Page<User> findByIsManagerTrueAndNomContainingOrPrenomContainingOrEmailAddressContainingAllIgnoreCase(@Param("term") String nomOrPrenomOrEmailAddress, Pageable pageable);

    @Query("""
    SELECT u FROM User u
    WHERE u.isSuperManager = true
      AND (LOWER(u.nom) LIKE LOWER(CONCAT('%', :term, '%'))
        OR LOWER(u.prenom) LIKE LOWER(CONCAT('%', :term, '%'))
        OR LOWER(u.emailAddress) LIKE LOWER(CONCAT('%', :term, '%')))
    """)
    Page<User> findByIsSuperManagerTrueAndNomContainingOrPrenomContainingOrEmailAddressContainingAllIgnoreCase(@Param("term") String nomOrPrenomOrEmailAddress, Pageable pageable);

    @Query("""
    SELECT u FROM User u
    WHERE u.postes IS NOT EMPTY
        AND (LOWER(u.nom) LIKE LOWER(CONCAT('%', :term, '%'))
            OR LOWER(u.prenom) LIKE LOWER(CONCAT('%', :term, '%'))
            OR LOWER(u.emailAddress) LIKE LOWER(CONCAT('%', :term, '%')))
    """)
    Page<User> findByPostesIsNotEmptyAndNomContainingOrPrenomContainingOrEmailAddressContainingAllIgnoreCase(@Param("term") String nomOrPrenomOrEmailAddress, Pageable pageable);

    @Query("""
    SELECT u FROM User u
    WHERE u.numCandidat IS NOT NULL
        AND (LOWER(u.nom) LIKE LOWER(CONCAT('%', :term, '%'))
            OR LOWER(u.prenom) LIKE LOWER(CONCAT('%', :term, '%'))
            OR LOWER(u.emailAddress) LIKE LOWER(CONCAT('%', :term, '%')))
    """)
    Page<User> findByNumCandidatIsNotNullAndNomContainingOrPrenomContainingOrEmailAddressContainingAllIgnoreCase(@Param("term") String nomOrPrenomOrEmailAddress, Pageable pageable);

    @Query("""
    SELECT u FROM User u
    WHERE LOWER(u.nom) LIKE LOWER(CONCAT('%', :term, '%'))
           OR LOWER(u.prenom) LIKE LOWER(CONCAT('%', :term, '%'))
           OR LOWER(u.emailAddress) LIKE LOWER(CONCAT('%', :term, '%'))
    """)
    Page<User> findByNomContainingOrPrenomContainingOrEmailAddressContainingAllIgnoreCase(@Param("term") String nomOrPrenomOrEmailAddress, Pageable pageable);
}
