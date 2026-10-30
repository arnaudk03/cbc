package bf.gov.anptic.sonagess.cbc.stocks.dto;

import bf.gov.anptic.sonagess.cbc.stocks.domain.BebcLigneEnlevement;
import bf.gov.anptic.sonagess.cbc.stocks.domain.BordereauEnlevementBebc;
import bf.gov.anptic.sonagess.cbc.stocks.domain.BordereauReceptionMagasin;
import bf.gov.anptic.sonagess.cbc.stocks.domain.FicheStockJournaliereFsj;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

public final class StockDtos {

    private StockDtos() {
    }

    public record FsjRequest(
            @NotNull(message = "Le comptoir est obligatoire") Long comptoirId,
            @NotNull(message = "La date de la fiche est obligatoire") LocalDate dateFiche,
            @NotBlank(message = "La nature de l'opération est obligatoire") String natureOperation,
            @PositiveOrZero Integer entreeSacs,
            @PositiveOrZero Double entreePoidsKg,
            @PositiveOrZero Double entreeValeurCfa,
            @PositiveOrZero Integer sortieSacs,
            @PositiveOrZero Double sortiePoidsKg,
            @PositiveOrZero Double sortieValeurCfa
    ) {
    }

    public record FsjResponse(
            Long id,
            Long comptoirId,
            String comptoirNom,
            LocalDate dateFiche,
            String natureOperation,
            Integer entreeSacs,
            Double entreePoidsKg,
            Double entreeValeurCfa,
            Integer sortieSacs,
            Double sortiePoidsKg,
            Double sortieValeurCfa,
            Integer stockFinalSacs,
            Double stockFinalPoidsKg,
            Double stockFinalValeurCfa,
            LocalDateTime creeLe
    ) {
        public static FsjResponse from(FicheStockJournaliereFsj fiche) {
            return new FsjResponse(
                    fiche.getId(),
                    fiche.getComptoir().getId(),
                    fiche.getComptoir().getNom(),
                    fiche.getDateFiche(),
                    fiche.getNatureOperation(),
                    fiche.getEntreeSacs(),
                    fiche.getEntreePoidsKg(),
                    fiche.getEntreeValeurCfa(),
                    fiche.getSortieSacs(),
                    fiche.getSortiePoidsKg(),
                    fiche.getSortieValeurCfa(),
                    fiche.getStockFinalSacs(),
                    fiche.getStockFinalPoidsKg(),
                    fiche.getStockFinalValeurCfa(),
                    fiche.getCreeLe()
            );
        }
    }

    public record SpeculationDisponibleResponse(
            Long speculationId,
            String speculationCode,
            String speculationNom,
            Double quantiteCollecteeKg,
            Double quantiteEnleveeKg,
            Double quantiteDisponibleKg
    ) {
    }

    public record StockDisponibleResponse(
            Long comptoirId,
            String comptoirNom,
            Double tonnageCollecteValideKg,
            Double tonnageEnleveKg,
            Double tonnageDisponibleKg,
            List<SpeculationDisponibleResponse> speculations
    ) {
    }

    public record TransfertLigneRequest(
            @NotNull(message = "La spéculation est obligatoire") Long speculationId,
            @NotNull(message = "Le nombre de sacs est obligatoire")
            @Positive(message = "Le nombre de sacs doit être positif") Integer nombreSacs,
            @NotNull(message = "Le colisage est obligatoire")
            @Positive(message = "Le colisage doit être strictement positif") Double colisageKg
    ) {
    }

    public record TransfertRequest(
            @NotNull(message = "Le comptoir de départ est obligatoire") Long comptoirId,
            @NotNull(message = "Le magasin de destination est obligatoire") Long magasinDestinationId,
            String direction,
            String localite,
            String magasinDepart,
            String beneficiaire,
            String objetEnlevement,
            @NotEmpty(message = "Au moins une spéculation est obligatoire")
            @Valid List<TransfertLigneRequest> lignes,
            @NotBlank(message = "Le transporteur est obligatoire") String nomTransporteur,
            @NotBlank(message = "Le numéro du véhicule est obligatoire") String numeroVehicule,
            LocalDate dateArriveePrevue
    ) {
    }

    public record ReceptionRequest(
            @NotNull(message = "Le magasin de réception est obligatoire") Long magasinCentralId,
            @NotNull(message = "Le nombre de sacs reçus est obligatoire")
            @PositiveOrZero Integer nombreSacsRecus,
            @NotNull(message = "Le tonnage déchargé est obligatoire")
            @PositiveOrZero Double tonnageDechargeKg
    ) {
    }

    public record ReceptionResponse(
            Long id,
            String numeroReception,
            Long magasinCentralId,
            String magasinNom,
            String magasinier,
            LocalDateTime dateDechargement,
            Integer nombreSacsRecus,
            Double tonnageDechargeKg,
            Double ecartPoidsKg,
            String statutConformite
    ) {
        public static ReceptionResponse from(BordereauReceptionMagasin reception) {
            if (reception == null) {
                return null;
            }
            return new ReceptionResponse(
                    reception.getId(),
                    reception.getNumeroReception(),
                    reception.getMagasinCentral().getId(),
                    reception.getMagasinCentral().getNom(),
                    reception.getMagasinier().getNomComplet(),
                    reception.getDateDechargement(),
                    reception.getNombreSacsRecus(),
                    reception.getTonnageDechargeKg(),
                    reception.getEcartPoidsKg(),
                    reception.getStatutConformite().name()
            );
        }
    }

    public record TransfertLigneResponse(
            Long id,
            Long speculationId,
            String speculationCode,
            String speculationNom,
            Integer nombreSacs,
            Double colisageKg,
            Double quantiteKg
    ) {
        public static TransfertLigneResponse from(BebcLigneEnlevement ligne) {
            return new TransfertLigneResponse(
                    ligne.getId(),
                    ligne.getSpeculation().getId(),
                    ligne.getSpeculation().getCode(),
                    ligne.getSpeculation().getNom(),
                    ligne.getNombreSacs(),
                    ligne.getColisageKg(),
                    ligne.getQuantiteKg()
            );
        }
    }

    public record TransfertResponse(
            Long id,
            String numeroBebc,
            Long comptoirId,
            String comptoirNom,
            Long magasinDestinationId,
            String magasinDestinationNom,
            String direction,
            String localite,
            String magasinDepart,
            String beneficiaire,
            String objetEnlevement,
            Integer nombreSacs,
            Double tonnageEnleveKg,
            List<TransfertLigneResponse> lignes,
            String nomTransporteur,
            String numeroVehicule,
            String statut,
            LocalDateTime dateEnlevement,
            LocalDate dateArriveePrevue,
            LocalDateTime creeLe,
            ReceptionResponse reception
    ) {
        public static TransfertResponse from(BordereauEnlevementBebc bebc,
                                             BordereauReceptionMagasin reception) {
            return new TransfertResponse(
                    bebc.getId(),
                    bebc.getNumeroBebc(),
                    bebc.getComptoirDepart().getId(),
                    bebc.getComptoirDepart().getNom(),
                    bebc.getMagasinDestination() != null ? bebc.getMagasinDestination().getId() : null,
                    bebc.getMagasinDestination() != null ? bebc.getMagasinDestination().getNom() : null,
                    bebc.getDirection(),
                    bebc.getLocalite(),
                    bebc.getMagasinDepart(),
                    bebc.getBeneficiaire(),
                    bebc.getObjetEnlevement(),
                    bebc.getNombreSacs(),
                    bebc.getTonnageEnleveKg(),
                    bebc.getLignes().stream().map(TransfertLigneResponse::from).toList(),
                    bebc.getNomTransporteur(),
                    bebc.getNumeroVehicule(),
                    bebc.getStatut().name(),
                    bebc.getDateEnlevement(),
                    bebc.getDateArriveePrevue(),
                    bebc.getCreeLe(),
                    ReceptionResponse.from(reception)
            );
        }
    }
}
