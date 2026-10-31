package bf.gov.anptic.sonagess.cbc.stocks.repository;

import bf.gov.anptic.sonagess.cbc.stocks.domain.LigneFicheStockMateriel;
import bf.gov.anptic.sonagess.cbc.stocks.domain.StatutFicheStockMateriel;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;

public interface LigneFicheStockMaterielRepository
        extends JpaRepository<LigneFicheStockMateriel, Long> {

    List<LigneFicheStockMateriel> findByFicheIdOrderByMaterielCategorieAscMaterielLibelleAsc(
            Long ficheId);

    @Query("""
            select l from LigneFicheStockMateriel l
            join fetch l.materiel m
            join fetch l.fiche f
            where f.comptoir.id = :comptoirId
              and f.statut = :statut
            order by f.dateFiche desc, f.creeLe desc
            """)
    List<LigneFicheStockMateriel> findAllByComptoirAndStatut(
            @Param("comptoirId") Long comptoirId,
            @Param("statut") StatutFicheStockMateriel statut);

    @Query("""
            select l from LigneFicheStockMateriel l
            join fetch l.fiche f
            where f.comptoir.id = :comptoirId
              and l.materiel.id = :materielId
              and f.statut = :statut
              and f.dateFiche <= :dateFiche
            order by f.dateFiche desc, f.creeLe desc
            """)
    List<LigneFicheStockMateriel> findDernieresLignes(
            @Param("comptoirId") Long comptoirId,
            @Param("materielId") Long materielId,
            @Param("statut") StatutFicheStockMateriel statut,
            @Param("dateFiche") LocalDate dateFiche,
            Pageable pageable);
}
