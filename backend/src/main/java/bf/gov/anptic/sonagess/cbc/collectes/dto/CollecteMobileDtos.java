package bf.gov.anptic.sonagess.cbc.collectes.dto;

import bf.gov.anptic.sonagess.cbc.paiements.domain.ModePaiement;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

public final class CollecteMobileDtos {

    private CollecteMobileDtos() {
    }

    // ------------------------------------------------------------------------
    // 1. BOOTSTRAP HORS-LIGNE (SYNC INITIALE AVANT DÉPART SUR LE TERRAIN)
    // ------------------------------------------------------------------------
    public record MobileBootstrapResponse(
            AgentMobileDto agent,
            CampagneMobileDto campagne,
            ComptoirMobileDto comptoir,
            List<SpeculationMobileDto> speculations,
            List<PrixGarantieMobileDto> prixGarantis,
            List<ProducteurMobileDto> producteurs,
            List<String> modesPaiement,
            List<String> operateursMobileMoney,
            ResumeJournalierMobileDto resumeAujourdhui,
            LocalDateTime horodatageServeur
    ) {}

    public record AgentMobileDto(
            Long id,
            String matricule,
            String nomComplet,
            String telephone,
            String email,
            Long comptoirId,
            String comptoirNom,
            String comptoirCode,
            List<String> roles
    ) {}

    public record CampagneMobileDto(
            Long id,
            String code,
            String libelle,
            LocalDate dateDebut,
            LocalDate dateFin,
            String statut
    ) {}

    public record ComptoirMobileDto(
            Long id,
            String code,
            String nom,
            String localityId,
            String localiteNom,
            String regionNom,
            String provinceNom,
            Double capaciteStockageKg,
            Boolean actif
    ) {}

    public record SpeculationMobileDto(
            Long id,
            String code,
            String nom,
            Double seuilHumiditeMax,
            Double seuilImpureteMax,
            Boolean active
    ) {}

    public record PrixGarantieMobileDto(
            Long id,
            Long speculationId,
            String speculationCode,
            Double prixUnitaireKg,
            LocalDate dateEffet,
            String statut
    ) {}

    public record ProducteurMobileDto(
            Long id,
            String nomPrenom,
            String telephoneMobileMoney,
            String operateurMobile,
            String cnibNumero,
            LocalDate cnibDateDelivrance,
            String cnibLieuDelivrance,
            String localityId,
            String localityNom,
            String adressePrecision
    ) {}

    // ------------------------------------------------------------------------
    // 2. SYNCHRONISATION PAR LOT (BATCH OFFLINE SYNC FLUTTER)
    // ------------------------------------------------------------------------
    public record MobileBatchSyncRequest(
            String tabletteImei,
            String tabletteNumeroSerie,
            List<ProducteurOfflineRequest> nouveauxProducteurs,
            @NotEmpty(message = "Le lot de synchronisation doit contenir au moins un bordereau")
            @Valid List<BordereauOfflineRequest> bordereaux
    ) {}

    public record ProducteurOfflineRequest(
            @NotBlank(message = "L'identifiant temporaire mobile du producteur est obligatoire")
            String clientTempId,
            @NotBlank(message = "Le nom et prénom sont obligatoires")
            String nomPrenom,
            @NotBlank(message = "Le téléphone Mobile Money est obligatoire")
            String telephoneMobileMoney,
            @NotBlank(message = "L'opérateur mobile est obligatoire")
            String operateurMobile,
            @NotBlank(message = "Le numéro CNIB est obligatoire")
            String cnibNumero,
            LocalDate cnibDateDelivrance,
            String cnibLieuDelivrance,
            String localityId,
            String adressePrecision
    ) {}

    public record BordereauOfflineRequest(
            @NotBlank(message = "L'UUID de synchronisation client (clientSyncId) est obligatoire")
            String clientSyncId,
            String referenceLocale,
            @NotNull(message = "La campagne est obligatoire")
            Long campagneId,
            Long comptoirId,
            Long producteurId,
            String clientTempProducteurId,
            @NotNull(message = "Le mode de paiement est obligatoire")
            ModePaiement modePaiement,
            LocalDateTime dateHeureCollecte,
            Double latitudeGps,
            Double longitudeGps,
            String observationsReceptionnaire,
            String signatureProducteurToken,
            String signatureAgentToken,
            @NotEmpty(message = "Au moins une spéculation est requise sur le bordereau")
            @Valid List<LigneCollecteOfflineRequest> lignes
    ) {}

    public record LigneCollecteOfflineRequest(
            String clientLigneId,
            @NotNull(message = "La spéculation est obligatoire")
            Long speculationId,
            @NotNull(message = "Le nombre de sacs est obligatoire")
            @Positive(message = "Le nombre de sacs doit être > 0")
            Integer nombreSacs,
            @NotNull(message = "Le poids brut est obligatoire")
            @Positive(message = "Le poids brut doit être > 0")
            Double poidsBrutTotalKg,
            @NotNull(message = "La tare est obligatoire")
            @PositiveOrZero(message = "La tare doit être positive ou nulle")
            Double tareEmballageKg,
            @NotNull(message = "La quantité nette déclarée est obligatoire")
            @Positive(message = "La quantité nette déclarée doit être > 0")
            Double quantiteKg,
            @NotNull(message = "Le taux d'humidité est obligatoire")
            @PositiveOrZero
            Double tauxHumidite,
            @NotNull(message = "Le taux d'impuretés est obligatoire")
            @PositiveOrZero
            Double tauxImpuretes,
            @PositiveOrZero
            Double grainsAvaries,
            @NotNull(message = "La conformité doit être renseignée")
            Boolean conforme,
            @NotBlank(message = "La décision de l'agent est obligatoire")
            String decisionAgent,
            String motifRejet
    ) {}

    public record MobileBatchSyncResponse(
            LocalDateTime horodatageServeur,
            Map<String, Long> producteursMap,
            List<SyncBordereauResult> resultats,
            int totalRecus,
            int totalSucces,
            int totalErreurs
    ) {}

    public record SyncBordereauResult(
            String clientSyncId,
            String referenceLocale,
            StatutSync statut,
            Long bordereauId,
            String numeroBrbc,
            Double quantiteTotaleKg,
            Double montantTotalCfa,
            String montantEnLettres,
            List<SyncLigneResult> lignes,
            String messageErreur
    ) {}

    public record SyncLigneResult(
            String clientLigneId,
            Long collecteId,
            String referenceLot,
            String speculationCode,
            Double quantiteKg,
            Double prixUnitaireKg,
            Double montantNetCfa
    ) {}

    public enum StatutSync {
        SYNCHRONISE,
        DEJA_SYNCHRONISE,
        REJETE
    }

    // ------------------------------------------------------------------------
    // 3. TABLEAU DE BORD & STATISTIQUES MOBILES
    // ------------------------------------------------------------------------
    public record ResumeJournalierMobileDto(
            LocalDate date,
            long nombreBordereaux,
            long nombreLots,
            Double quantiteTotaleKg,
            Double montantTotalCfa,
            List<QuantiteParSpeculationDto> parSpeculation,
            long synchronisees,
            long examineesScp,
            long valideesDct,
            long rejetees
    ) {}

    public record QuantiteParSpeculationDto(
            String code,
            String nom,
            Double quantiteKg,
            Double montantCfa
    ) {}

    // ------------------------------------------------------------------------
    // 4. TICKET D'IMPRESSION BLUETOOTH ESC/POS POUR TABLETTE
    // ------------------------------------------------------------------------
    public record TicketBrbcDto(
            String enteteRepublique,
            String enteteSociete,
            String titreDocument,
            String numeroBrbc,
            String dateEmission,
            String comptoirNom,
            String regionNom,
            String provinceNom,
            String agentMatricule,
            String agentNom,
            String producteurNom,
            String producteurTelephone,
            String producteurCnib,
            String modePaiement,
            List<TicketLigneDto> lignes,
            Double totalPoidsBrutKg,
            Double totalTareKg,
            Double totalPoidsNetKg,
            Double montantTotalCfa,
            String montantEnLettres,
            String observations,
            String mentionsLegales,
            String qrCodePayload
    ) {}

    public record TicketLigneDto(
            String speculationNom,
            String speculationCode,
            String referenceLot,
            int nombreSacs,
            Double poidsBrutKg,
            Double tareKg,
            Double poidsNetKg,
            Double tauxHumidite,
            Double tauxImpuretes,
            Double prixUnitaireKg,
            Double montantNetCfa,
            String conformite
    ) {}
}
