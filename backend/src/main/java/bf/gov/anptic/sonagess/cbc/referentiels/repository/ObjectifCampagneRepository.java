package bf.gov.anptic.sonagess.cbc.referentiels.repository;

import bf.gov.anptic.sonagess.cbc.referentiels.domain.ObjectifCampagne;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ObjectifCampagneRepository extends JpaRepository<ObjectifCampagne, Long> {

    List<ObjectifCampagne> findByCampagneId(Long campagneId);

    Optional<ObjectifCampagne> findByCampagneIdAndSpeculationIdAndLocalityId(
            Long campagneId, Long speculationId, UUID localityId);

    Optional<ObjectifCampagne> findByCampagneIdAndSpeculationIdAndLocalityIsNull(
            Long campagneId, Long speculationId);

    @Query("select coalesce(sum(o.objectifTonnes), 0) from ObjectifCampagne o where o.campagne.id = :campagneId")
    Double sumObjectifsByCampagne(@Param("campagneId") Long campagneId);
}
