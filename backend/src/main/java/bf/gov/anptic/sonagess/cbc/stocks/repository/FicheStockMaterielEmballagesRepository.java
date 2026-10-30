package bf.gov.anptic.sonagess.cbc.stocks.repository;

import bf.gov.anptic.sonagess.cbc.stocks.domain.FicheStockMaterielEmballages;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface FicheStockMaterielEmballagesRepository extends JpaRepository<FicheStockMaterielEmballages, Long> {

    List<FicheStockMaterielEmballages> findByComptoirIdOrderByDesignationMaterielAsc(Long comptoirId);
}
