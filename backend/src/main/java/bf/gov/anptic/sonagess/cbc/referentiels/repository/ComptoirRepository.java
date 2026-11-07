package bf.gov.anptic.sonagess.cbc.referentiels.repository;

import bf.gov.anptic.sonagess.cbc.referentiels.domain.Comptoir;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ComptoirRepository extends JpaRepository<Comptoir, Long> {

    List<Comptoir> findAllByOrderByNomAsc();

    List<Comptoir> findAllByActifTrueOrderByNomAsc();

    Optional<Comptoir> findByCode(String code);

    boolean existsByCode(String code);

    boolean existsByCodeAndIdNot(String code, Long id);

    boolean existsByIdAndActifTrue(Long id);

    long countByActifTrue();
}
