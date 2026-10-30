package bf.gov.anptic.sonagess.cbc.referentiels.repository;

import bf.gov.anptic.sonagess.cbc.referentiels.domain.Campagne;
import bf.gov.anptic.sonagess.cbc.referentiels.domain.CampagneStatut;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface CampagneRepository extends JpaRepository<Campagne, Long> {

    Optional<Campagne> findByCode(String code);

    Optional<Campagne> findFirstByStatutOrderByDateDebutDesc(CampagneStatut statut);

    List<Campagne> findAllByOrderByDateDebutDesc();

    boolean existsByCode(String code);

    boolean existsByCodeAndIdNot(String code, Long id);
}
