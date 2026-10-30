package bf.gov.anptic.sonagess.cbc.referentiels.repository;

import bf.gov.anptic.sonagess.cbc.referentiels.domain.PrixGarantie;
import bf.gov.anptic.sonagess.cbc.referentiels.domain.StatutPrixGarantie;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface PrixGarantieRepository extends JpaRepository<PrixGarantie, Long> {

    List<PrixGarantie> findByCampagneIdAndSpeculationIdOrderByDateEffetDesc(Long campagneId, Long speculationId);

    List<PrixGarantie> findByCampagneIdOrderByDateEffetDesc(Long campagneId);

    Optional<PrixGarantie> findFirstByCampagneIdAndSpeculationIdAndLocalityIdAndStatutAndDateEffetLessThanEqualOrderByDateEffetDesc(
            Long campagneId, Long speculationId, java.util.UUID localityId,
            StatutPrixGarantie statut, LocalDate date);

    Optional<PrixGarantie> findFirstByCampagneIdAndSpeculationIdAndLocalityIsNullAndStatutAndDateEffetLessThanEqualOrderByDateEffetDesc(
            Long campagneId, Long speculationId, StatutPrixGarantie statut, LocalDate date);

    @Query("""
            select p from PrixGarantie p
            where p.campagne.id = :campagneId
              and p.speculation.id = :speculationId
              and p.statut = bf.gov.anptic.sonagess.cbc.referentiels.domain.StatutPrixGarantie.APPROUVE
              and p.dateEffet <= :date
            """)
    List<PrixGarantie> findApplicables(@Param("campagneId") Long campagneId,
                                       @Param("speculationId") Long speculationId,
                                       @Param("date") LocalDate date);
}
