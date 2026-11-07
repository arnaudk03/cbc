package bf.gov.anptic.sonagess.cbc.collectes.repository;

import bf.gov.anptic.sonagess.cbc.collectes.domain.CollecteBordChamp;
import bf.gov.anptic.sonagess.cbc.collectes.domain.CollecteStatut;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface CollecteBordChampRepository extends JpaRepository<CollecteBordChamp, Long> {

    @EntityGraph(attributePaths = {
            "campagne", "comptoir", "comptoir.locality", "producteur", "speculation", "agent",
            "controleQualite", "fichePesee", "bordereauReception", "bordereauReception.collectes"
    })
    @Query("select c from CollecteBordChamp c where c.id = :id")
    Optional<CollecteBordChamp> findDetailedById(@Param("id") Long id);

    @EntityGraph(attributePaths = {
            "campagne", "comptoir", "comptoir.locality", "producteur", "speculation", "agent",
            "controleQualite", "fichePesee", "bordereauReception", "bordereauReception.collectes"
    })
    @Query("""
            select c from CollecteBordChamp c
            where c.bordereauReception.id = :bordereauId
            order by c.id
            """)
    List<CollecteBordChamp> findDetailedByBordereauId(@Param("bordereauId") Long bordereauId);

    @EntityGraph(attributePaths = {
            "campagne", "comptoir", "comptoir.locality", "producteur", "speculation", "agent",
            "controleQualite", "fichePesee", "bordereauReception"
    })
    @Query("""
            select c from CollecteBordChamp c
            where (:statut is null or c.statut = :statut)
              and (:campagneId is null or c.campagne.id = :campagneId)
              and (:comptoirId is null or c.comptoir.id = :comptoirId)
              and (:speculationId is null or c.speculation.id = :speculationId)
            """)
    Page<CollecteBordChamp> search(@Param("statut") CollecteStatut statut,
                                   @Param("campagneId") Long campagneId,
                                   @Param("comptoirId") Long comptoirId,
                                   @Param("speculationId") Long speculationId,
                                   Pageable pageable);

    @Query("""
            select coalesce(sum(c.quantiteKg), 0)
            from CollecteBordChamp c
            where c.campagne.id = :campagneId and c.statut <> :statutRejete
              and (:comptoirId is null or c.comptoir.id = :comptoirId)
            """)
    Double sumQuantiteCollectee(@Param("campagneId") Long campagneId,
                                @Param("statutRejete") CollecteStatut statutRejete,
                                @Param("comptoirId") Long comptoirId);

    @Query("""
            select coalesce(sum(c.quantiteKg), 0)
            from CollecteBordChamp c
            where c.comptoir.id = :comptoirId and c.statut = :statut
            """)
    Double sumQuantiteValideeParComptoir(@Param("comptoirId") Long comptoirId,
                                        @Param("statut") CollecteStatut statut);

    @Query("""
            select c.speculation.id, c.speculation.code, c.speculation.nom,
                   coalesce(sum(c.quantiteKg), 0)
            from CollecteBordChamp c
            where c.comptoir.id = :comptoirId and c.statut = :statut
            group by c.speculation.id, c.speculation.code, c.speculation.nom
            order by c.speculation.nom
            """)
    List<Object[]> sumQuantiteValideeParComptoirEtSpeculation(@Param("comptoirId") Long comptoirId,
                                                              @Param("statut") CollecteStatut statut);

    long countByCampagneIdAndStatut(Long campagneId, CollecteStatut statut);

    @Query("""
            select count(c)
            from CollecteBordChamp c
            where c.campagne.id = :campagneId
              and c.statut in :statuts
              and (:comptoirId is null or c.comptoir.id = :comptoirId)
            """)
    long countByCampagneIdAndStatutInAndComptoirId(@Param("campagneId") Long campagneId,
                                                   @Param("statuts") Collection<CollecteStatut> statuts,
                                                   @Param("comptoirId") Long comptoirId);

    long countByCampagneIdAndStatutNot(Long campagneId, CollecteStatut statut);

    long countByStatut(CollecteStatut statut);

    @Query("""
            select c.speculation.code, c.speculation.nom, coalesce(sum(c.quantiteKg), 0)
            from CollecteBordChamp c
            where c.campagne.id = :campagneId and c.statut <> :statutRejete
              and (:comptoirId is null or c.comptoir.id = :comptoirId)
            group by c.speculation.code, c.speculation.nom
            order by coalesce(sum(c.quantiteKg), 0) desc
            """)
    List<Object[]> sumQuantiteParSpeculation(@Param("campagneId") Long campagneId,
                                             @Param("statutRejete") CollecteStatut statutRejete,
                                             @Param("comptoirId") Long comptoirId);

    @Query(value = """
            select r.name, coalesce(sum(c.quantite_kg), 0)
            from collectes_bord_champ c
              join comptoirs co on co.id = c.comptoir_id
              join locality_hierarchies lh on lh.descendant_id = co.locality_id
              join localities r on r.id = lh.ancestor_id and r.type = 'REGION'
            where c.campagne_id = :campagneId and c.statut <> :statutRejete
            group by r.name
            order by coalesce(sum(c.quantite_kg), 0) desc
            """, nativeQuery = true)
    List<Object[]> sumQuantiteParRegion(@Param("campagneId") Long campagneId,
                                        @Param("statutRejete") String statutRejete);

    @Query(value = """
            select r.name, coalesce(sum(c.quantite_kg), 0)
            from collectes_bord_champ c
              join comptoirs co on co.id = c.comptoir_id
              join locality_hierarchies lh on lh.descendant_id = co.locality_id
              join localities r on r.id = lh.ancestor_id and r.type = 'REGION'
            where c.campagne_id = :campagneId and c.statut <> :statutRejete
              and co.id = :comptoirId
            group by r.name
            order by coalesce(sum(c.quantite_kg), 0) desc
            """, nativeQuery = true)
    List<Object[]> sumQuantiteParRegionPourComptoir(@Param("campagneId") Long campagneId,
                                                    @Param("statutRejete") String statutRejete,
                                                    @Param("comptoirId") Long comptoirId);

    @Query("""
            select function('to_char', c.dateHeureCollecte, 'YYYY-MM'), coalesce(sum(c.quantiteKg), 0)
            from CollecteBordChamp c
            where c.campagne.id = :campagneId and c.statut <> :statutRejete
              and (:comptoirId is null or c.comptoir.id = :comptoirId)
            group by function('to_char', c.dateHeureCollecte, 'YYYY-MM')
            order by function('to_char', c.dateHeureCollecte, 'YYYY-MM')
            """)
    List<Object[]> evolutionMensuelle(@Param("campagneId") Long campagneId,
                                      @Param("statutRejete") CollecteStatut statutRejete,
                                      @Param("comptoirId") Long comptoirId);

    List<CollecteBordChamp> findTop10ByOrderByDateHeureCollecteDesc();

    @EntityGraph(attributePaths = {
            "campagne", "comptoir", "producteur", "speculation", "controleQualite", "fichePesee", "bordereauReception"
    })
    @Query("""
            select c from CollecteBordChamp c
            where c.agent.id = :agentId
              and (:dateDebut is null or c.dateHeureCollecte >= :dateDebut)
              and (:dateFin is null or c.dateHeureCollecte <= :dateFin)
              and (:statut is null or c.statut = :statut)
            order by c.dateHeureCollecte desc
            """)
    Page<CollecteBordChamp> searchAgentCollectes(@Param("agentId") Long agentId,
                                                @Param("dateDebut") LocalDateTime dateDebut,
                                                @Param("dateFin") LocalDateTime dateFin,
                                                @Param("statut") CollecteStatut statut,
                                                Pageable pageable);

    @Query("""
            select c from CollecteBordChamp c
            where (c.agent.id = :agentId or (:comptoirId is not null and c.comptoir.id = :comptoirId))
              and c.dateHeureCollecte >= :debutJournee
              and c.dateHeureCollecte <= :finJournee
            """)
    List<CollecteBordChamp> findCollectesJournee(@Param("agentId") Long agentId,
                                                @Param("comptoirId") Long comptoirId,
                                                @Param("debutJournee") LocalDateTime debutJournee,
                                                @Param("finJournee") LocalDateTime finJournee);
}
