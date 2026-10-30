package bf.gov.anptic.sonagess.cbc.paiements.repository;

import bf.gov.anptic.sonagess.cbc.paiements.domain.OrdrePaiement;
import bf.gov.anptic.sonagess.cbc.paiements.domain.OrdrePaiementStatut;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface OrdrePaiementRepository extends JpaRepository<OrdrePaiement, Long> {

    Optional<OrdrePaiement> findByBordereauReceptionId(Long bordereauId);

    boolean existsByBordereauReceptionId(Long bordereauId);

    @EntityGraph(attributePaths = {
            "bordereauReception", "bordereauReception.collectes",
            "bordereauReception.collectes.campagne", "bordereauReception.collectes.producteur",
            "initiateur", "validateur2FA"
    })
    List<OrdrePaiement> findByStatutOrderByDateInitiationDesc(OrdrePaiementStatut statut);

    @EntityGraph(attributePaths = {
            "bordereauReception", "bordereauReception.collectes",
            "bordereauReception.collectes.campagne", "bordereauReception.collectes.producteur",
            "initiateur", "validateur2FA"
    })
    List<OrdrePaiement> findAllByOrderByDateInitiationDesc();

    long countByStatut(OrdrePaiementStatut statut);

    @Query("""
            select coalesce(sum(o.montantPayeCfa), 0)
            from OrdrePaiement o
            where o.statut = :statut
            """)
    Double sumMontantByStatut(@Param("statut") OrdrePaiementStatut statut);

    @EntityGraph(attributePaths = {
            "bordereauReception", "bordereauReception.collectes",
            "bordereauReception.collectes.campagne", "bordereauReception.collectes.producteur",
            "initiateur", "validateur2FA"
    })
    @Query("select o from OrdrePaiement o where o.id = :id")
    Optional<OrdrePaiement> findDetailedById(@Param("id") Long id);
}
