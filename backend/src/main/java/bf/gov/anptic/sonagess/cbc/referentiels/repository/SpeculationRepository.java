package bf.gov.anptic.sonagess.cbc.referentiels.repository;

import bf.gov.anptic.sonagess.cbc.referentiels.domain.Speculation;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface SpeculationRepository extends JpaRepository<Speculation, Long> {

    List<Speculation> findByActiveTrueOrderByNomAsc();

    List<Speculation> findAllByOrderByNomAsc();

    Optional<Speculation> findByCode(String code);

    boolean existsByCode(String code);

    boolean existsByCodeAndIdNot(String code, Long id);
}
