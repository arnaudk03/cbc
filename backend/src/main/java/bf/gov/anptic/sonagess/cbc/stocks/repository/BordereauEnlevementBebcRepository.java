package bf.gov.anptic.sonagess.cbc.stocks.repository;

import bf.gov.anptic.sonagess.cbc.stocks.domain.BordereauEnlevementBebc;
import bf.gov.anptic.sonagess.cbc.stocks.domain.StatutBebc;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface BordereauEnlevementBebcRepository extends JpaRepository<BordereauEnlevementBebc, Long> {

    List<BordereauEnlevementBebc> findAllByOrderByCreeLeDesc();

    List<BordereauEnlevementBebc> findByStatutOrderByCreeLeDesc(StatutBebc statut);

    long countByStatut(StatutBebc statut);

    @Query("""
            select coalesce(sum(b.tonnageEnleveKg), 0)
            from BordereauEnlevementBebc b
            where b.comptoirDepart.id = :comptoirId
            """)
    Double sumTonnageEnleveParComptoir(@Param("comptoirId") Long comptoirId);
}
