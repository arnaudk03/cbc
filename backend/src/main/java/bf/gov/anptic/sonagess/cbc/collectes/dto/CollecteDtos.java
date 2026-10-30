package bf.gov.anptic.sonagess.cbc.collectes.dto;

import bf.gov.anptic.sonagess.cbc.collectes.domain.BordereauReceptionBrbc;
import bf.gov.anptic.sonagess.cbc.collectes.domain.CollecteBordChamp;
import bf.gov.anptic.sonagess.cbc.collectes.domain.ControleQualite;
import bf.gov.anptic.sonagess.cbc.collectes.domain.FichePeseeFp;
import bf.gov.anptic.sonagess.cbc.common.MontantEnLettres;
import bf.gov.anptic.sonagess.cbc.referentiels.domain.Locality;
import bf.gov.anptic.sonagess.cbc.auth.domain.Utilisateur;
import bf.gov.anptic.sonagess.cbc.referentiels.domain.LocalityType;
import bf.gov.anptic.sonagess.cbc.paiements.domain.ModePaiement;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public final class CollecteDtos {

    private CollecteDtos() {
    }

    public record CreateCollecteRequest(
            @NotNull(message = "La campagne est obligatoire") Long campagneId,
            @NotNull(message = "Le comptoir est obligatoire") Long comptoirId,
            @NotNull(message = "Le producteur est obligatoire") Long producteurId,
            @NotNull(message = "La spéculation est obligatoire") Long speculationId,
            @NotNull(message = "La quantité est obligatoire")
            @Positive(message = "La quantité doit être strictement positive") Double quantiteKg,
            @NotNull(message = "Le mode de paiement est obligatoire") ModePaiement modePaiement,
            @NotNull(message = "Le taux d'humidité est obligatoire")
            @PositiveOrZero Double tauxHumidite,
            @NotNull(message = "Le taux d'impuretés est obligatoire")
            @PositiveOrZero Double tauxImpuretes,
            @PositiveOrZero Double grainsAvaries,
            @NotNull(message = "La conformité doit être renseignée") Boolean conforme,
            @NotBlank(message = "La décision de l'agent est obligatoire") String decisionAgent,
            String motifRejet,
            @NotNull(message = "Le nombre de sacs est obligatoire")
            @Positive(message = "Le nombre de sacs doit être strictement positif") Integer nombreSacs,
            @NotNull(message = "Le poids brut est obligatoire")
            @Positive(message = "Le poids brut doit être strictement positif") Double poidsBrutTotalKg,
            @NotNull(message = "La tare est obligatoire")
            @PositiveOrZero(message = "La tare doit être positive ou nulle") Double tareEmballageKg,
            Double latitudeGps,
            Double longitudeGps,
            String observationsReceptionnaire
    ) {
    }

    public record CreateCollecteLotRequest(
            @NotNull(message = "La campagne est obligatoire") Long campagneId,
            @NotNull(message = "Le comptoir est obligatoire") Long comptoirId,
            @NotNull(message = "Le producteur est obligatoire") Long producteurId,
            @NotNull(message = "Le mode de paiement est obligatoire") ModePaiement modePaiement,
            String observationsReceptionnaire,
            Double latitudeGps,
            Double longitudeGps,
            @NotEmpty(message = "Au moins une spéculation est obligatoire")
            @Valid List<LigneCollecteRequest> lignes
    ) {
    }

    public record LigneCollecteRequest(
            @NotNull(message = "La spéculation est obligatoire") Long speculationId,
            @NotNull(message = "La quantité est obligatoire")
            @Positive(message = "La quantité doit être strictement positive") Double quantiteKg,
            @NotNull(message = "Le taux d'humidité est obligatoire")
            @PositiveOrZero Double tauxHumidite,
            @NotNull(message = "Le taux d'impuretés est obligatoire")
            @PositiveOrZero Double tauxImpuretes,
            @PositiveOrZero Double grainsAvaries,
            @NotNull(message = "La conformité doit être renseignée") Boolean conforme,
            @NotBlank(message = "La décision de l'agent est obligatoire") String decisionAgent,
            String motifRejet,
            @NotNull(message = "Le nombre de sacs est obligatoire")
            @Positive(message = "Le nombre de sacs doit être strictement positif") Integer nombreSacs,
            @NotNull(message = "Le poids brut est obligatoire")
            @Positive(message = "Le poids brut doit être strictement positif") Double poidsBrutTotalKg,
            @NotNull(message = "La tare est obligatoire")
            @PositiveOrZero(message = "La tare doit être positive ou nulle") Double tareEmballageKg
    ) {
    }

    public record ExamenRequest(
            @NotNull(message = "La décision est obligatoire") Boolean approuve,
            String motif
    ) {
    }

    public record ValidationRequest(
            @NotNull(message = "La décision est obligatoire") Boolean approuve,
            String motif
    ) {
    }

    public record ControleQualiteDto(
            Long id,
            Double tauxHumidite,
            Double tauxImpuretes,
            Double grainsAvaries,
            Boolean conforme,
            String decisionAgent,
            String motifRejet
    ) {
        public static ControleQualiteDto from(ControleQualite controle) {
            if (controle == null) {
                return null;
            }
            return new ControleQualiteDto(controle.getId(), controle.getTauxHumidite(),
                    controle.getTauxImpuretes(), controle.getGrainsAvaries(), controle.getConforme(),
                    controle.getDecisionAgent(), controle.getMotifRejet());
        }
    }

    public record FichePeseeDto(
            Long id,
            String numeroFiche,
            String directionRegionale,
            String directionProvinciale,
            String siteComptoir,
            Integer nombreSacs,
            Double poidsBrutTotalKg,
            Double tareEmballageKg,
            Double poidsNetTotalKg
    ) {
        public static FichePeseeDto from(FichePeseeFp fiche) {
            if (fiche == null) {
                return null;
            }
            return new FichePeseeDto(fiche.getId(), fiche.getNumeroFiche(),
                    fiche.getDirectionRegionale(), fiche.getDirectionProvinciale(),
                    fiche.getSiteComptoir(), fiche.getNombreSacs(), fiche.getPoidsBrutTotalKg(),
                    fiche.getTareEmballageKg(), fiche.getPoidsNetTotalKg());
        }
    }

    public record BrbcDto(
            Long id,
            String numeroBrbc,
            LocalDateTime dateEmission,
            Double quantiteKg,
            Double prixUnitaireApplique,
            Double montantNetPayerCfa,
            String montantEnLettres,
            String modePaiement,
            String observationsReceptionnaire,
            String statutValidation,
            int nombreLots
    ) {
        public static BrbcDto from(BordereauReceptionBrbc brbc) {
            if (brbc == null) {
                return null;
            }
            String montantEnLettres = brbc.getMontantNetPayerCfa() != null
                    ? MontantEnLettres.pourMontantCfa(BigDecimal.valueOf(brbc.getMontantNetPayerCfa()))
                    : brbc.getMontantEnLettres();
            return new BrbcDto(brbc.getId(), brbc.getNumeroBrbc(), brbc.getDateEmission(),
                    brbc.getQuantiteKg(), brbc.getPrixUnitaireApplique(), brbc.getMontantNetPayerCfa(),
                    montantEnLettres,
                    brbc.getModePaiement() != null ? brbc.getModePaiement().name() : null,
                    brbc.getObservationsReceptionnaire(),
                    brbc.getStatutValidation() != null ? brbc.getStatutValidation().name() : null,
                    brbc.getCollectes() != null ? brbc.getCollectes().size() : 0);
        }
    }

    public record CollecteLotResponse(
            BrbcDto bordereau,
            List<CollecteResponse> collectes
    ) {
    }

    public record CollecteResponse(
            Long id,
            String referenceLot,
            Long campagneId,
            String campagneCode,
            Long comptoirId,
            String comptoirCode,
            String comptoirNom,
            String region,
            Long producteurId,
            String producteurNom,
            String telephoneProducteur,
            String operateurMobile,
            Long speculationId,
            String speculationCode,
            String speculationNom,
            LocalDateTime dateHeureCollecte,
            Double quantiteKg,
            Double prixUnitaireKg,
            Double montantNetCfa,
            String statut,
            Double latitudeGps,
            Double longitudeGps,
            String motifRejet,
            Long examinateurId,
            String examinateur,
            LocalDateTime dateExamen,
            String validateur,
            LocalDateTime dateValidation,
            ControleQualiteDto controleQualite,
            FichePeseeDto fichePesee,
            BrbcDto bordereau
    ) {
        public static CollecteResponse from(CollecteBordChamp collecte) {
            Locality region = collecte.getComptoir().getLocality();
            while (region != null && region.getType() != LocalityType.REGION) {
                region = region.getParent();
            }
            Utilisateur examinateur = collecte.getExaminateur();
            Utilisateur validateur = collecte.getValidateur();
            return new CollecteResponse(
                    collecte.getId(),
                    collecte.getReferenceLot(),
                    collecte.getCampagne().getId(),
                    collecte.getCampagne().getCode(),
                    collecte.getComptoir().getId(),
                    collecte.getComptoir().getCode(),
                    collecte.getComptoir().getNom(),
                    region != null ? region.getName() : null,
                    collecte.getProducteur().getId(),
                    collecte.getProducteur().getNomPrenom(),
                    collecte.getProducteur().getTelephoneMobileMoney(),
                    collecte.getProducteur().getOperateurMobile(),
                    collecte.getSpeculation().getId(),
                    collecte.getSpeculation().getCode(),
                    collecte.getSpeculation().getNom(),
                    collecte.getDateHeureCollecte(),
                    collecte.getQuantiteKg(),
                    collecte.getPrixUnitaireKg(),
                    collecte.getMontantNetCfa(),
                    collecte.getStatut().name(),
                    collecte.getLatitudeGps(),
                    collecte.getLongitudeGps(),
                    collecte.getMotifRejet(),
                    examinateur != null ? examinateur.getId() : null,
                    examinateur != null ? examinateur.getNomComplet() : null,
                    collecte.getDateExamen(),
                    validateur != null ? validateur.getNomComplet() : null,
                    collecte.getDateValidation(),
                    ControleQualiteDto.from(collecte.getControleQualite()),
                    FichePeseeDto.from(collecte.getFichePesee()),
                    BrbcDto.from(collecte.getBordereauReception())
            );
        }
    }
}
