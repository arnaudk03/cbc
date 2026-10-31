package bf.gov.anptic.sonagess.cbc.equipements.repository;

import bf.gov.anptic.sonagess.cbc.equipements.domain.AffectationEquipement;
import bf.gov.anptic.sonagess.cbc.equipements.domain.TypeEquipement;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface AffectationEquipementRepository extends JpaRepository<AffectationEquipement, Long> {

    Optional<AffectationEquipement> findByEquipementIdAndDateFinIsNull(Long equipementId);

    boolean existsByEquipementIdAndDateFinIsNull(Long equipementId);

    @Query("""
            select a from AffectationEquipement a
            join fetch a.equipement e
            left join fetch a.utilisateur u
            join fetch a.comptoir c
            where a.comptoir.id = :comptoirId
              and a.dateFin is null
            order by e.designation asc
            """)
    List<AffectationEquipement> findActivesParComptoir(@Param("comptoirId") Long comptoirId);

    @Query("""
            select count(a) from AffectationEquipement a
            where a.utilisateur.id = :utilisateurId
              and a.equipement.type = :type
              and a.dateFin is null
            """)
    long countActivesParUtilisateurEtType(@Param("utilisateurId") Long utilisateurId,
                                          @Param("type") TypeEquipement type);
}
