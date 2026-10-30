package bf.gov.anptic.sonagess.cbc.stocks.repository;

import bf.gov.anptic.sonagess.cbc.stocks.domain.BebcLigneEnlevement;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface BebcLigneEnlevementRepository extends JpaRepository<BebcLigneEnlevement, Long> {

    @Query("""
            select l.speculation.id, coalesce(sum(l.quantiteKg), 0)
            from BebcLigneEnlevement l
            where l.bordereauEnlevement.comptoirDepart.id = :comptoirId
            group by l.speculation.id
            """)
    List<Object[]> sumQuantiteEnleveeParComptoirEtSpeculation(@Param("comptoirId") Long comptoirId);
}
