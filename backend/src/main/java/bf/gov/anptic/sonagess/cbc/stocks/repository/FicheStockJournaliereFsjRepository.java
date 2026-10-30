package bf.gov.anptic.sonagess.cbc.stocks.repository;

import bf.gov.anptic.sonagess.cbc.stocks.domain.FicheStockJournaliereFsj;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface FicheStockJournaliereFsjRepository extends JpaRepository<FicheStockJournaliereFsj, Long> {

    List<FicheStockJournaliereFsj> findByComptoirIdAndDateFicheOrderByCreeLeAsc(Long comptoirId, LocalDate date);

    List<FicheStockJournaliereFsj> findByComptoirIdAndDateFicheBetweenOrderByDateFicheAscCreeLeAsc(
            Long comptoirId, LocalDate debut, LocalDate fin);

    Optional<FicheStockJournaliereFsj> findTopByComptoirIdOrderByDateFicheDescCreeLeDesc(Long comptoirId);

    Optional<FicheStockJournaliereFsj> findTopByComptoirIdAndDateFicheLessThanEqualOrderByDateFicheDescCreeLeDesc(
            Long comptoirId, LocalDate dateFiche);
}
