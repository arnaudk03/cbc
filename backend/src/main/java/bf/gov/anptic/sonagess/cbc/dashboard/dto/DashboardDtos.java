package bf.gov.anptic.sonagess.cbc.dashboard.dto;

import java.time.LocalDate;
import java.util.List;

public final class DashboardDtos {

    private DashboardDtos() {
    }

    public record CampagneInfo(
            Long id,
            String code,
            String libelle,
            String statut,
            LocalDate dateDebut,
            LocalDate dateFin
    ) {
    }

    public record Kpis(
            Double objectifTonnes,
            Double collecteTonnes,
            Double tauxRealisation,
            long brbcValides,
            Double montantDecaisseCfa,
            long comptoirsActifs,
            long collectesEnAttente,
            long paiementsEnAttente
    ) {
    }

    public record SpeculationStat(
            String code,
            String nom,
            Double tonnage
    ) {
    }

    public record RegionStat(
            String region,
            Double tonnage
    ) {
    }

    public record EvolutionPoint(
            String periode,
            Double tonnage
    ) {
    }

    public record DashboardResponse(
            CampagneInfo campagne,
            Kpis kpis,
            List<SpeculationStat> speculations,
            List<RegionStat> regions,
            List<EvolutionPoint> evolution
    ) {
    }

}
