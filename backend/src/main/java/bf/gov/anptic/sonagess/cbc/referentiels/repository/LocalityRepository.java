package bf.gov.anptic.sonagess.cbc.referentiels.repository;

import bf.gov.anptic.sonagess.cbc.referentiels.domain.Locality;
import bf.gov.anptic.sonagess.cbc.referentiels.domain.LocalityType;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface LocalityRepository extends JpaRepository<Locality, UUID> {

    Optional<Locality> findByCode(String code);

    List<Locality> findByTypeOrderByNameAsc(LocalityType type);

    List<Locality> findByParentIdOrderByNameAsc(UUID parentId);

    long countByType(LocalityType type);
}
