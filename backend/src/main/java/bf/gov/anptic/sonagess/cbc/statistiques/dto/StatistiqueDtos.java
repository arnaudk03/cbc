package bf.gov.anptic.sonagess.cbc.statistiques.dto;

import java.time.LocalDate;
import java.util.List;

public final class StatistiqueDtos {

        private StatistiqueDtos() {
        }

        public record FiltreStatistique(
                        Long campagneId,
                        Long comptoirId,
                        String regionId,
                        Long speculationId,
                        LocalDate dateDebut,
                        LocalDate dateFin) {
        }

        public record CampagneInfo(
                        Long id,
                        String code,
                        String libelle,
                        String statut,
                        LocalDate dateDebut,
                        LocalDate dateFin,
                        Double objectifTotalTonnes) {
        }

        public record KpisStatistiques(
                        Double objectifTonnes,
                        Double collecteTonnes,
                        Double tauxRealisation,
                        Double montantTotalNetCfa,
                        Double montantDecaisseCfa,
                        Double tauxDecaissement,
                        long producteursUniques,
                        long comptoirsActifs,
                        long lotsCollectes,
                        long brbcEmis,
                        long brbcValides,
                        Double tauxConformite) {
        }

        public record SpeculationStat(
                        Long speculationId,
                        String code,
                        String nom,
                        Double tonnage,
                        Double partPourcent,
                        Double montantCfa,
                        Double prixMoyenKg,
                        Double humiditeMoyenne,
                        Double seuilHumiditeMax,
                        Double tauxConformite,
                        long nombreLots) {
        }

        public record RegionStat(
                        String regionId,
                        String region,
                        Double tonnage,
                        Double partPourcent,
                        Double montantCfa,
                        long nombreComptoirs,
                        long nombreLots) {
        }

        public record ComptoirStat(
                        Long comptoirId,
                        String code,
                        String nom,
                        String region,
                        Double tonnage,
                        Double partPourcent,
                        Double montantCfa,
                        long nombreLots,
                        long nombreProducteurs) {
        }

        public record OperateurStat(
                        String operateur,
                        long nombrePaiements,
                        Double montantTotalCfa,
                        Double montantConfirmeCfa,
                        Double partPourcent,
                        Double tauxSucces) {
        }

        public record MotifRejetStat(
                        String motif,
                        long occurrences) {
        }

        public record QualiteStat(
                        long lotsControles,
                        long lotsConformes,
                        long lotsNonConformes,
                        Double tauxConformite,
                        Double humiditeMoyenne,
                        Double impuretesMoyennes,
                        Double grainsAvariesMoyens,
                        List<MotifRejetStat> motifsRejet) {
        }

        public record EvolutionPoint(
                        String periode,
                        Double tonnage,
                        Double montantCfa,
                        long nombreCollectes) {
        }

        public record StatistiquesResponse(
                        CampagneInfo campagne,
                        KpisStatistiques kpis,
                        List<SpeculationStat> speculations,
                        List<RegionStat> regions,
                        List<ComptoirStat> comptoirs,
                        List<OperateurStat> operateurs,
                        QualiteStat qualite,
                        List<EvolutionPoint> evolution) {
        }

}
