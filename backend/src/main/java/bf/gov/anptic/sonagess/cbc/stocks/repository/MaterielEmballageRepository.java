package bf.gov.anptic.sonagess.cbc.stocks.repository;

import bf.gov.anptic.sonagess.cbc.stocks.domain.MaterielEmballage;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface MaterielEmballageRepository extends JpaRepository<MaterielEmballage, Long> {

    List<MaterielEmballage> findAllByOrderByCategorieAscLibelleAsc();

    List<MaterielEmballage> findAllByActifTrueOrderByCategorieAscLibelleAsc();

    boolean existsByCodeIgnoreCase(String code);

    boolean existsByCodeIgnoreCaseAndIdNot(String code, Long id);
}
