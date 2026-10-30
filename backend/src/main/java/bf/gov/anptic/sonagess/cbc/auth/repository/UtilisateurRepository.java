package bf.gov.anptic.sonagess.cbc.auth.repository;

import bf.gov.anptic.sonagess.cbc.auth.domain.Utilisateur;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface UtilisateurRepository extends JpaRepository<Utilisateur, Long>, JpaSpecificationExecutor<Utilisateur> {

    Optional<Utilisateur> findByEmailIgnoreCase(String email);

    Optional<Utilisateur> findByMatriculeIgnoreCase(String matricule);

    @Query("""
            select u from Utilisateur u
            where lower(u.email) = lower(:identifiant)
               or lower(u.matricule) = lower(:identifiant)
            """)
    Optional<Utilisateur> findByIdentifiant(@Param("identifiant") String identifiant);

    List<Utilisateur> findByRoles_Code(String code);

    List<Utilisateur> findAllByOrderByNomAscPrenomAsc();

    boolean existsByEmailIgnoreCaseOrMatriculeIgnoreCase(String email, String matricule);

    boolean existsByEmailIgnoreCase(String email);

    boolean existsByMatriculeIgnoreCase(String matricule);

    boolean existsByTelephone(String telephone);

    boolean existsByEmailIgnoreCaseAndIdNot(String email, Long id);

    boolean existsByMatriculeIgnoreCaseAndIdNot(String matricule, Long id);

    boolean existsByTelephoneAndIdNot(String telephone, Long id);

    long countByRoles_CodeAndActifTrue(String code);
}
