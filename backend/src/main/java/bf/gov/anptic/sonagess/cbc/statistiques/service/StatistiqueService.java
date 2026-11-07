package bf.gov.anptic.sonagess.cbc.statistiques.service;

import bf.gov.anptic.sonagess.cbc.auth.domain.Utilisateur;
import bf.gov.anptic.sonagess.cbc.auth.repository.UtilisateurRepository;
import bf.gov.anptic.sonagess.cbc.common.BusinessException;
import bf.gov.anptic.sonagess.cbc.common.ResourceNotFoundException;
import bf.gov.anptic.sonagess.cbc.referentiels.domain.Campagne;
import bf.gov.anptic.sonagess.cbc.referentiels.domain.CampagneStatut;
import bf.gov.anptic.sonagess.cbc.referentiels.repository.CampagneRepository;
import bf.gov.anptic.sonagess.cbc.security.Roles;
import bf.gov.anptic.sonagess.cbc.statistiques.dto.StatistiqueDtos;
import jakarta.persistence.EntityManager;
import jakarta.persistence.Query;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class StatistiqueService {

    private final EntityManager entityManager;
    private final CampagneRepository campagneRepository;
    private final UtilisateurRepository utilisateurRepository;

    @Transactional(readOnly = true)
    public StatistiqueDtos.StatistiquesResponse getStatistiques(
            StatistiqueDtos.FiltreStatistique filtre, Long utilisateurId) {

        Utilisateur utilisateur = utilisateurRepository.findById(utilisateurId)
                .orElseThrow(() -> new ResourceNotFoundException("Utilisateur introuvable."));

        Long comptoirImpose = idComptoirImpose(utilisateur);
        Long comptoirEffectif = comptoirImpose != null ? comptoirImpose : filtre.comptoirId();

        Campagne campagne = resoudreCampagne(filtre.campagneId());
        if (campagne == null) {
            return responseVide();
        }

        LocalDateTime dateDebut = filtre.dateDebut() != null ? filtre.dateDebut().atStartOfDay() : null;
        LocalDateTime dateFin = filtre.dateFin() != null ? filtre.dateFin().atTime(LocalTime.MAX) : null;

        Map<String, Object> paramsCommuns = new HashMap<>();
        paramsCommuns.put("campagneId", campagne.getId());
        if (comptoirEffectif != null)
            paramsCommuns.put("comptoirId", comptoirEffectif);
        if (filtre.speculationId() != null)
            paramsCommuns.put("speculationId", filtre.speculationId());
        if (filtre.regionId() != null && !filtre.regionId().isBlank()) {
            paramsCommuns.put("regionId", filtre.regionId().trim());
        }
        if (dateDebut != null)
            paramsCommuns.put("dateDebut", dateDebut);
        if (dateFin != null)
            paramsCommuns.put("dateFin", dateFin);

        String clauseWhereBase = construireWhereBase(paramsCommuns);

        StatistiqueDtos.KpisStatistiques kpis = calculerKpis(campagne, clauseWhereBase, paramsCommuns);
        List<StatistiqueDtos.SpeculationStat> speculations = calculerSpeculations(clauseWhereBase, paramsCommuns,
                kpis.collecteTonnes());
        List<StatistiqueDtos.RegionStat> regions = calculerRegions(clauseWhereBase, paramsCommuns,
                kpis.collecteTonnes());
        List<StatistiqueDtos.ComptoirStat> comptoirs = calculerComptoirs(clauseWhereBase, paramsCommuns,
                kpis.collecteTonnes());
        List<StatistiqueDtos.OperateurStat> operateurs = calculerOperateurs(clauseWhereBase, paramsCommuns);
        StatistiqueDtos.QualiteStat qualite = calculerQualite(clauseWhereBase, paramsCommuns);
        List<StatistiqueDtos.EvolutionPoint> evolution = calculerEvolution(clauseWhereBase, paramsCommuns);

        StatistiqueDtos.CampagneInfo campagneInfo = new StatistiqueDtos.CampagneInfo(
                campagne.getId(),
                campagne.getCode(),
                campagne.getLibelle(),
                campagne.getStatut().name(),
                campagne.getDateDebut(),
                campagne.getDateFin(),
                campagne.getObjectifTotalTonnes() != null ? campagne.getObjectifTotalTonnes() : 0.0);

        return new StatistiqueDtos.StatistiquesResponse(
                campagneInfo,
                kpis,
                speculations,
                regions,
                comptoirs,
                operateurs,
                qualite,
                evolution);
    }

    private StatistiqueDtos.KpisStatistiques calculerKpis(Campagne campagne, String whereBase,
            Map<String, Object> params) {
        String sqlBase = """
                SELECT COALESCE(SUM(c.quantite_kg), 0) AS total_kg,
                       COALESCE(SUM(c.montant_net_cfa), 0) AS total_montant,
                       COUNT(DISTINCT c.producteur_id) AS nb_producteurs,
                       COUNT(DISTINCT c.comptoir_id) AS nb_comptoirs,
                       COUNT(c.id) AS nb_lots,
                       COUNT(DISTINCT c.bordereau_id) AS nb_brbc
                FROM collectes_bord_champ c
                """ + whereBase + " AND c.statut <> 'REJETEE' ";

        Query qBase = entityManager.createNativeQuery(sqlBase);
        injecterParametres(qBase, params);
        Object[] resBase = (Object[]) qBase.getSingleResult();

        double totalKg = ((Number) resBase[0]).doubleValue();
        double totalMontantNet = ((Number) resBase[1]).doubleValue();
        long nbProducteurs = ((Number) resBase[2]).longValue();
        long nbComptoirs = ((Number) resBase[3]).longValue();
        long nbLots = ((Number) resBase[4]).longValue();
        long nbBrbc = ((Number) resBase[5]).longValue();

        double collecteTonnes = arrondi(totalKg / 1000.0);
        double objectifTonnes = campagne.getObjectifTotalTonnes() != null ? campagne.getObjectifTotalTonnes() : 0.0;
        double tauxRealisation = objectifTonnes > 0 ? arrondi((collecteTonnes / objectifTonnes) * 100.0) : 0.0;

        String sqlBrbcValides = """
                SELECT COUNT(DISTINCT b.id)
                FROM bordereaux_reception_brbc b
                JOIN collectes_bord_champ c ON c.bordereau_id = b.id
                """ + whereBase + " AND b.statut_validation = 'VALIDE_DCT' ";

        Query qBrbc = entityManager.createNativeQuery(sqlBrbcValides);
        injecterParametres(qBrbc, params);
        long brbcValides = ((Number) qBrbc.getSingleResult()).longValue();

        String sqlPaiement = """
                SELECT COALESCE(SUM(op.montant_paye_cfa), 0)
                FROM ordres_paiement op
                WHERE op.statut = 'CONFIRME'
                  AND op.bordereau_reception_id IN (
                      SELECT DISTINCT c.bordereau_id
                      FROM collectes_bord_champ c
                      """ + whereBase + """
                  )
                """;

        Query qPaiement = entityManager.createNativeQuery(sqlPaiement);
        injecterParametres(qPaiement, params);
        double montantDecaisse = ((Number) qPaiement.getSingleResult()).doubleValue();
        double tauxDecaissement = totalMontantNet > 0 ? arrondi((montantDecaisse / totalMontantNet) * 100.0) : 0.0;

        String sqlConformite = """
                SELECT COUNT(cq.id),
                       COUNT(CASE WHEN cq.conforme = true THEN 1 END)
                FROM controles_qualite cq
                JOIN collectes_bord_champ c ON c.id = cq.collecte_id
                """ + whereBase;

        Query qConf = entityManager.createNativeQuery(sqlConformite);
        injecterParametres(qConf, params);
        Object[] resConf = (Object[]) qConf.getSingleResult();
        long totalControles = ((Number) resConf[0]).longValue();
        long nbConformes = ((Number) resConf[1]).longValue();
        double tauxConformite = totalControles > 0 ? arrondi(((double) nbConformes / totalControles) * 100.0) : 100.0;

        return new StatistiqueDtos.KpisStatistiques(
                objectifTonnes,
                collecteTonnes,
                tauxRealisation,
                arrondi(totalMontantNet),
                arrondi(montantDecaisse),
                tauxDecaissement,
                nbProducteurs,
                nbComptoirs,
                nbLots,
                nbBrbc,
                brbcValides,
                tauxConformite);
    }

    @SuppressWarnings("unchecked")
    private List<StatistiqueDtos.SpeculationStat> calculerSpeculations(String whereBase, Map<String, Object> params,
            double totalTonnageGlobal) {
        String sql = """
                SELECT s.id, s.code, s.nom,
                       COALESCE(SUM(c.quantite_kg), 0) AS total_kg,
                       COALESCE(SUM(c.montant_net_cfa), 0) AS total_montant,
                       COUNT(c.id) AS nb_lots,
                       COALESCE(AVG(cq.taux_humidite), 0) AS avg_hum,
                       COALESCE(s.seuil_humidite_max, 14.0) AS seuil_max,
                       COUNT(CASE WHEN cq.conforme = true THEN 1 END) AS nb_conformes,
                       COUNT(cq.id) AS nb_controles
                FROM collectes_bord_champ c
                JOIN speculations s ON s.id = c.speculation_id
                LEFT JOIN controles_qualite cq ON cq.collecte_id = c.id
                """ + whereBase + " AND c.statut <> 'REJETEE' " + """
                GROUP BY s.id, s.code, s.nom, s.seuil_humidite_max
                ORDER BY total_kg DESC
                """;

        Query query = entityManager.createNativeQuery(sql);
        injecterParametres(query, params);
        List<Object[]> rows = query.getResultList();

        List<StatistiqueDtos.SpeculationStat> list = new ArrayList<>();
        for (Object[] r : rows) {
            Long id = ((Number) r[0]).longValue();
            String code = (String) r[1];
            String nom = (String) r[2];
            double kg = ((Number) r[3]).doubleValue();
            double montant = ((Number) r[4]).doubleValue();
            long lots = ((Number) r[5]).longValue();
            double hum = ((Number) r[6]).doubleValue();
            double seuilMax = ((Number) r[7]).doubleValue();
            long conformes = ((Number) r[8]).longValue();
            long controles = ((Number) r[9]).longValue();

            double tonnage = arrondi(kg / 1000.0);
            double part = totalTonnageGlobal > 0 ? arrondi((tonnage / totalTonnageGlobal) * 100.0) : 0.0;
            double prixMoyen = kg > 0 ? arrondi(montant / kg) : 0.0;
            double tauxConf = controles > 0 ? arrondi(((double) conformes / controles) * 100.0) : 100.0;

            list.add(new StatistiqueDtos.SpeculationStat(
                    id, code, nom, tonnage, part, arrondi(montant),
                    prixMoyen, arrondi(hum), seuilMax, tauxConf, lots));
        }
        return list;
    }

    @SuppressWarnings("unchecked")
    private List<StatistiqueDtos.RegionStat> calculerRegions(String whereBase, Map<String, Object> params,
            double totalTonnageGlobal) {
        String sql = """
                SELECT CAST(r.id AS VARCHAR) AS region_id,
                       r.name AS region_nom,
                       COALESCE(SUM(c.quantite_kg), 0) AS total_kg,
                       COALESCE(SUM(c.montant_net_cfa), 0) AS total_montant,
                       COUNT(DISTINCT c.comptoir_id) AS nb_comptoirs,
                       COUNT(c.id) AS nb_lots
                FROM collectes_bord_champ c
                JOIN comptoirs co ON co.id = c.comptoir_id
                JOIN locality_hierarchies lh ON lh.descendant_id = co.locality_id
                JOIN localities r ON r.id = lh.ancestor_id AND r.type = 'REGION'
                """ + whereBase + " AND c.statut <> 'REJETEE' " + """
                GROUP BY r.id, r.name
                ORDER BY total_kg DESC
                """;

        Query query = entityManager.createNativeQuery(sql);
        injecterParametres(query, params);
        List<Object[]> rows = query.getResultList();

        List<StatistiqueDtos.RegionStat> list = new ArrayList<>();
        for (Object[] row : rows) {
            String regId = (String) row[0];
            String regNom = (String) row[1];
            double kg = ((Number) row[2]).doubleValue();
            double montant = ((Number) row[3]).doubleValue();
            long nbComptoirs = ((Number) row[4]).longValue();
            long nbLots = ((Number) row[5]).longValue();

            double tonnage = arrondi(kg / 1000.0);
            double part = totalTonnageGlobal > 0 ? arrondi((tonnage / totalTonnageGlobal) * 100.0) : 0.0;

            list.add(new StatistiqueDtos.RegionStat(
                    regId, regNom, tonnage, part, arrondi(montant), nbComptoirs, nbLots));
        }
        return list;
    }

    @SuppressWarnings("unchecked")
    private List<StatistiqueDtos.ComptoirStat> calculerComptoirs(String whereBase, Map<String, Object> params,
            double totalTonnageGlobal) {
        String sql = """
                SELECT co.id, co.code, co.nom,
                       COALESCE(r.name, '—') AS region_nom,
                       COALESCE(SUM(c.quantite_kg), 0) AS total_kg,
                       COALESCE(SUM(c.montant_net_cfa), 0) AS total_montant,
                       COUNT(c.id) AS nb_lots,
                       COUNT(DISTINCT c.producteur_id) AS nb_producteurs
                FROM collectes_bord_champ c
                JOIN comptoirs co ON co.id = c.comptoir_id
                LEFT JOIN locality_hierarchies lh ON lh.descendant_id = co.locality_id
                LEFT JOIN localities r ON r.id = lh.ancestor_id AND r.type = 'REGION'
                """ + whereBase + " AND c.statut <> 'REJETEE' " + """
                GROUP BY co.id, co.code, co.nom, r.name
                ORDER BY total_kg DESC
                LIMIT 50
                """;

        Query query = entityManager.createNativeQuery(sql);
        injecterParametres(query, params);
        List<Object[]> rows = query.getResultList();

        List<StatistiqueDtos.ComptoirStat> list = new ArrayList<>();
        for (Object[] row : rows) {
            Long id = ((Number) row[0]).longValue();
            String code = (String) row[1];
            String nom = (String) row[2];
            String region = (String) row[3];
            double kg = ((Number) row[4]).doubleValue();
            double montant = ((Number) row[5]).doubleValue();
            long lots = ((Number) row[6]).longValue();
            long producteurs = ((Number) row[7]).longValue();

            double tonnage = arrondi(kg / 1000.0);
            double part = totalTonnageGlobal > 0 ? arrondi((tonnage / totalTonnageGlobal) * 100.0) : 0.0;

            list.add(new StatistiqueDtos.ComptoirStat(
                    id, code, nom, region, tonnage, part, arrondi(montant), lots, producteurs));
        }
        return list;
    }

    @SuppressWarnings("unchecked")
    private List<StatistiqueDtos.OperateurStat> calculerOperateurs(String whereBase, Map<String, Object> params) {
        String sql = """
                SELECT op.operateur,
                       COUNT(op.id) AS nb_paiements,
                       COALESCE(SUM(op.montant_paye_cfa), 0) AS montant_total,
                       COALESCE(SUM(CASE WHEN op.statut = 'CONFIRME' THEN op.montant_paye_cfa ELSE 0 END), 0) AS montant_confirme,
                       COUNT(CASE WHEN op.statut = 'CONFIRME' THEN 1 END) AS nb_confirmes
                FROM ordres_paiement op
                WHERE op.bordereau_reception_id IN (
                    SELECT DISTINCT c.bordereau_id
                    FROM collectes_bord_champ c
                    """
                + whereBase + """
                        )
                        GROUP BY op.operateur
                        ORDER BY montant_total DESC
                        """;

        Query query = entityManager.createNativeQuery(sql);
        injecterParametres(query, params);
        List<Object[]> rows = query.getResultList();

        double grandTotal = rows.stream()
                .mapToDouble(r -> ((Number) r[2]).doubleValue())
                .sum();

        List<StatistiqueDtos.OperateurStat> list = new ArrayList<>();
        for (Object[] row : rows) {
            String op = (String) row[0];
            long nb = ((Number) row[1]).longValue();
            double total = ((Number) row[2]).doubleValue();
            double confirme = ((Number) row[3]).doubleValue();
            long nbConfirmes = ((Number) row[4]).longValue();

            double part = grandTotal > 0 ? arrondi((total / grandTotal) * 100.0) : 0.0;
            double tauxSucces = nb > 0 ? arrondi(((double) nbConfirmes / nb) * 100.0) : 0.0;

            list.add(new StatistiqueDtos.OperateurStat(
                    op, nb, arrondi(total), arrondi(confirme), part, tauxSucces));
        }
        return list;
    }

    @SuppressWarnings("unchecked")
    private StatistiqueDtos.QualiteStat calculerQualite(String whereBase, Map<String, Object> params) {
        String sql = """
                SELECT COUNT(cq.id) AS total,
                       COUNT(CASE WHEN cq.conforme = true THEN 1 END) AS conformes,
                       COUNT(CASE WHEN cq.conforme = false THEN 1 END) AS non_conformes,
                       COALESCE(AVG(cq.taux_humidite), 0) AS avg_hum,
                       COALESCE(AVG(cq.taux_impuretes), 0) AS avg_imp,
                       COALESCE(AVG(cq.grains_avaries), 0) AS avg_avaries
                FROM controles_qualite cq
                JOIN collectes_bord_champ c ON c.id = cq.collecte_id
                """ + whereBase;

        Query query = entityManager.createNativeQuery(sql);
        injecterParametres(query, params);
        Object[] r = (Object[]) query.getSingleResult();

        long total = ((Number) r[0]).longValue();
        long conformes = ((Number) r[1]).longValue();
        long nonConformes = ((Number) r[2]).longValue();
        double avgHum = ((Number) r[3]).doubleValue();
        double avgImp = ((Number) r[4]).doubleValue();
        double avgAvaries = ((Number) r[5]).doubleValue();
        double tauxConf = total > 0 ? arrondi(((double) conformes / total) * 100.0) : 100.0;

        String sqlMotifs = """
                SELECT cq.motif_rejet, COUNT(cq.id) AS occ
                FROM controles_qualite cq
                JOIN collectes_bord_champ c ON c.id = cq.collecte_id
                """ + whereBase + """
                  AND cq.conforme = false
                  AND cq.motif_rejet IS NOT NULL
                  AND TRIM(cq.motif_rejet) <> ''
                GROUP BY cq.motif_rejet
                ORDER BY occ DESC
                LIMIT 5
                """;

        Query qMotifs = entityManager.createNativeQuery(sqlMotifs);
        injecterParametres(qMotifs, params);
        List<Object[]> rowsMotifs = qMotifs.getResultList();

        List<StatistiqueDtos.MotifRejetStat> motifs = new ArrayList<>();
        for (Object[] row : rowsMotifs) {
            String m = (String) row[0];
            long occ = ((Number) row[1]).longValue();
            motifs.add(new StatistiqueDtos.MotifRejetStat(m, occ));
        }

        return new StatistiqueDtos.QualiteStat(
                total, conformes, nonConformes, tauxConf,
                arrondi(avgHum), arrondi(avgImp), arrondi(avgAvaries), motifs);
    }

    @SuppressWarnings("unchecked")
    private List<StatistiqueDtos.EvolutionPoint> calculerEvolution(String whereBase, Map<String, Object> params) {
        String sql = """
                SELECT TO_CHAR(c.date_heure_collecte, 'YYYY-MM') AS periode,
                       COALESCE(SUM(c.quantite_kg), 0) AS total_kg,
                       COALESCE(SUM(c.montant_net_cfa), 0) AS total_montant,
                       COUNT(c.id) AS nb_lots
                FROM collectes_bord_champ c
                """ + whereBase + " AND c.statut <> 'REJETEE' " + """
                GROUP BY TO_CHAR(c.date_heure_collecte, 'YYYY-MM')
                ORDER BY periode ASC
                """;

        Query query = entityManager.createNativeQuery(sql);
        injecterParametres(query, params);
        List<Object[]> rows = query.getResultList();

        List<StatistiqueDtos.EvolutionPoint> points = new ArrayList<>();
        for (Object[] r : rows) {
            String periode = (String) r[0];
            double kg = ((Number) r[1]).doubleValue();
            double montant = ((Number) r[2]).doubleValue();
            long lots = ((Number) r[3]).longValue();
            points.add(new StatistiqueDtos.EvolutionPoint(
                    periode, arrondi(kg / 1000.0), arrondi(montant), lots));
        }
        return points;
    }

    /**
     * Génère un fichier CSV UTF-8 prêt à l'export tableur
     */
    @Transactional(readOnly = true)
    public byte[] exporterCsv(StatistiqueDtos.FiltreStatistique filtre, Long utilisateurId) {
        StatistiqueDtos.StatistiquesResponse stats = getStatistiques(filtre, utilisateurId);

        StringBuilder sb = new StringBuilder();
        // BOM UTF-8 pour Excel
        sb.append('\uFEFF');

        sb.append("RAPPORT STATISTIQUE DE COLLECTE BORD CHAMP - SONAGESS BURKINA FASO\n");
        sb.append("Campagne;").append(stats.campagne().code()).append(" - ").append(stats.campagne().libelle())
                .append("\n");
        sb.append("Date d'extraction;").append(LocalDate.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")))
                .append("\n\n");

        sb.append("=== INDICATEURS GLOBAUX (KPIS) ===\n");
        sb.append("Indicateur;Valeur;Unite\n");
        sb.append("Objectif de campagne;").append(stats.kpis().objectifTonnes()).append(";Tonnes\n");
        sb.append("Collecte realisee;").append(stats.kpis().collecteTonnes()).append(";Tonnes\n");
        sb.append("Taux de realisation;").append(stats.kpis().tauxRealisation()).append(";%\n");
        sb.append("Montant net engage;").append(stats.kpis().montantTotalNetCfa()).append(";FCFA\n");
        sb.append("Montant decaisse Mobile Money;").append(stats.kpis().montantDecaisseCfa()).append(";FCFA\n");
        sb.append("Taux de decaissement;").append(stats.kpis().tauxDecaissement()).append(";%\n");
        sb.append("Producteurs uniques;").append(stats.kpis().producteursUniques()).append(";Producteurs\n");
        sb.append("Comptoirs actifs;").append(stats.kpis().comptoirsActifs()).append(";Comptoirs\n");
        sb.append("Lots de pesee collectes;").append(stats.kpis().lotsCollectes()).append(";Lots\n");
        sb.append("Bordereaux BRBC emis;").append(stats.kpis().brbcEmis()).append(";Bordereaux\n");
        sb.append("Bordereaux BRBC valides DCT;").append(stats.kpis().brbcValides()).append(";Bordereaux\n");
        sb.append("Taux de conformite qualite;").append(stats.kpis().tauxConformite()).append(";%\n\n");

        sb.append("=== REPARTITION PAR SPECULATION ===\n");
        sb.append(
                "Code;Speculation;Tonnage (T);Part (%);Montant (FCFA);Prix Moyen (FCFA/kg);Humidite Moy. (%);Seuil Max (%);Taux Conformite (%)\n");
        for (StatistiqueDtos.SpeculationStat s : stats.speculations()) {
            sb.append(s.code()).append(";")
                    .append(s.nom()).append(";")
                    .append(s.tonnage()).append(";")
                    .append(s.partPourcent()).append(";")
                    .append(s.montantCfa()).append(";")
                    .append(s.prixMoyenKg()).append(";")
                    .append(s.humiditeMoyenne()).append(";")
                    .append(s.seuilHumiditeMax()).append(";")
                    .append(s.tauxConformite()).append("\n");
        }
        sb.append("\n");

        sb.append("=== REPARTITION PAR REGION ===\n");
        sb.append("Region;Tonnage (T);Part (%);Montant (FCFA);Nombre Comptoirs;Nombre Lots\n");
        for (StatistiqueDtos.RegionStat r : stats.regions()) {
            sb.append(r.region()).append(";")
                    .append(r.tonnage()).append(";")
                    .append(r.partPourcent()).append(";")
                    .append(r.montantCfa()).append(";")
                    .append(r.nombreComptoirs()).append(";")
                    .append(r.nombreLots()).append("\n");
        }
        sb.append("\n");

        sb.append("=== DECAISSEMENTS PAR OPERATEUR MOBILE MONEY ===\n");
        sb.append("Operateur;Nombre Paiements;Montant Total (FCFA);Montant Confirme (FCFA);Part (%);Taux Succes (%)\n");
        for (StatistiqueDtos.OperateurStat op : stats.operateurs()) {
            sb.append(op.operateur()).append(";")
                    .append(op.nombrePaiements()).append(";")
                    .append(op.montantTotalCfa()).append(";")
                    .append(op.montantConfirmeCfa()).append(";")
                    .append(op.partPourcent()).append(";")
                    .append(op.tauxSucces()).append("\n");
        }

        return sb.toString().getBytes(StandardCharsets.UTF_8);
    }

    private String construireWhereBase(Map<String, Object> params) {
        StringBuilder sb = new StringBuilder(" WHERE c.campagne_id = :campagneId ");
        if (params.containsKey("comptoirId")) {
            sb.append(" AND c.comptoir_id = :comptoirId ");
        }
        if (params.containsKey("speculationId")) {
            sb.append(" AND c.speculation_id = :speculationId ");
        }
        if (params.containsKey("regionId")) {
            sb.append("""
                     AND c.comptoir_id IN (
                         SELECT co2.id
                         FROM comptoirs co2
                         JOIN locality_hierarchies lh2 ON lh2.descendant_id = co2.locality_id
                         WHERE lh2.ancestor_id = CAST(:regionId AS uuid)
                     )
                    """);
        }
        if (params.containsKey("dateDebut")) {
            sb.append(" AND c.date_heure_collecte >= :dateDebut ");
        }
        if (params.containsKey("dateFin")) {
            sb.append(" AND c.date_heure_collecte <= :dateFin ");
        }
        return sb.toString();
    }

    private void injecterParametres(Query query, Map<String, Object> params) {
        params.forEach(query::setParameter);
    }

    private Campagne resoudreCampagne(Long campagneId) {
        if (campagneId != null) {
            return campagneRepository.findById(campagneId)
                    .orElseThrow(() -> new ResourceNotFoundException("Campagne introuvable."));
        }
        return campagneRepository.findFirstByStatutOrderByDateDebutDesc(CampagneStatut.OUVERTE)
                .orElseGet(() -> campagneRepository.findAll().stream().findFirst().orElse(null));
    }

    private Long idComptoirImpose(Utilisateur utilisateur) {
        if (!utilisateur.hasRole(Roles.AGENT_COLLECTEUR) && !utilisateur.hasRole(Roles.CHEF_COMPTOIR)) {
            return null;
        }
        if (utilisateur.getComptoir() == null) {
            throw new BusinessException(
                    "Aucun comptoir n'est affecté à votre compte. Contactez l'administrateur.",
                    HttpStatus.FORBIDDEN);
        }
        return utilisateur.getComptoir().getId();
    }

    private StatistiqueDtos.StatistiquesResponse responseVide() {
        return new StatistiqueDtos.StatistiquesResponse(
                null,
                new StatistiqueDtos.KpisStatistiques(0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0, 0, 0, 0, 0, 100.0),
                List.of(), List.of(), List.of(), List.of(),
                new StatistiqueDtos.QualiteStat(0, 0, 0, 100.0, 0.0, 0.0, 0.0, List.of()),
                List.of()
        );
    }

    private double arrondi(double valeur) {
        return Math.round(valeur * 100.0) / 100.0;
    }
}
