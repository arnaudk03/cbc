package bf.gov.anptic.sonagess.cbc.referentiels.repository;

import bf.gov.anptic.sonagess.cbc.referentiels.domain.MagasinCentral;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface MagasinCentralRepository extends JpaRepository<MagasinCentral, Long> {

    List<MagasinCentral> findAllByActifTrueOrderByNomAsc();

    List<MagasinCentral> findAllByOrderByNomAsc();

    Optional<MagasinCentral> findByCodeMagasin(String codeMagasin);

    boolean existsByCodeMagasin(String codeMagasin);

    boolean existsByCodeMagasinAndIdNot(String codeMagasin, Long id);
}
