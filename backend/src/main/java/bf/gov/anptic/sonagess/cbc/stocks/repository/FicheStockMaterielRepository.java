package bf.gov.anptic.sonagess.cbc.stocks.repository;

import bf.gov.anptic.sonagess.cbc.stocks.domain.FicheStockMateriel;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface FicheStockMaterielRepository extends JpaRepository<FicheStockMateriel, Long> {

    List<FicheStockMateriel> findByComptoirIdOrderByDateFicheDescCreeLeDesc(Long comptoirId);

    List<FicheStockMateriel> findByComptoirIdAndDateFicheOrderByCreeLeAsc(
            Long comptoirId, LocalDate dateFiche);

    Optional<FicheStockMateriel> findFirstByComptoirIdAndDateFicheOrderByCreeLeDesc(
            Long comptoirId, LocalDate dateFiche);

    boolean existsByNumeroFiche(String numeroFiche);
}
