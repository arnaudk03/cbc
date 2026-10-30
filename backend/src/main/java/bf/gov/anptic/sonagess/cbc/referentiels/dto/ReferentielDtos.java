package bf.gov.anptic.sonagess.cbc.referentiels.dto;

import bf.gov.anptic.sonagess.cbc.referentiels.domain.Campagne;
import bf.gov.anptic.sonagess.cbc.referentiels.domain.Comptoir;
import bf.gov.anptic.sonagess.cbc.referentiels.domain.Locality;
import bf.gov.anptic.sonagess.cbc.referentiels.domain.MagasinCentral;
import bf.gov.anptic.sonagess.cbc.referentiels.domain.ObjectifCampagne;
import bf.gov.anptic.sonagess.cbc.referentiels.domain.PrixGarantie;
import bf.gov.anptic.sonagess.cbc.referentiels.domain.Producteur;
import bf.gov.anptic.sonagess.cbc.referentiels.domain.Speculation;
import bf.gov.anptic.sonagess.cbc.referentiels.domain.CampagneStatut;
import bf.gov.anptic.sonagess.cbc.referentiels.domain.LocalityType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

public final class ReferentielDtos {

    private ReferentielDtos() {
    }

    public record CampagneRequest(
            @NotBlank(message = "Le code est obligatoire") String code,
            @NotBlank(message = "Le libellé est obligatoire") String libelle,
            @NotNull(message = "La date de début est obligatoire") LocalDate dateDebut,
            @NotNull(message = "La date de fin est obligatoire") LocalDate dateFin,
            @PositiveOrZero(message = "L'objectif doit être positif ou nul") Double objectifTotalTonnes,
            CampagneStatut statut
    ) {
    }

    public record CampagneResponse(
            Long id,
            String code,
            String libelle,
            LocalDate dateDebut,
            LocalDate dateFin,
            Double objectifTotalTonnes,
            String statut
    ) {
        public static CampagneResponse from(Campagne campagne) {
            return new CampagneResponse(campagne.getId(), campagne.getCode(), campagne.getLibelle(),
                    campagne.getDateDebut(), campagne.getDateFin(), campagne.getObjectifTotalTonnes(),
                    campagne.getStatut().name());
        }
    }

    public record ObjectifRequest(
            @NotNull(message = "La spéculation est obligatoire") Long speculationId,
            UUID localityId,
            @NotNull(message = "L'objectif est obligatoire")
            @Positive(message = "L'objectif doit être strictement positif") Double objectifTonnes,
            @PositiveOrZero Double budgetPrevisionnelCfa,
            @Positive Double seuilAlertePct
    ) {
    }

    public record ObjectifResponse(
            Long id,
            Long campagneId,
            Long speculationId,
            String speculationCode,
            String speculationNom,
            UUID localityId,
            String localityNom,
            Double objectifTonnes,
            Double budgetPrevisionnelCfa,
            Double seuilAlertePct
    ) {
        public static ObjectifResponse from(ObjectifCampagne objectif) {
            return new ObjectifResponse(
                    objectif.getId(),
                    objectif.getCampagne().getId(),
                    objectif.getSpeculation().getId(),
                    objectif.getSpeculation().getCode(),
                    objectif.getSpeculation().getNom(),
                    objectif.getLocality() != null ? objectif.getLocality().getId() : null,
                    objectif.getLocality() != null ? objectif.getLocality().getName() : null,
                    objectif.getObjectifTonnes(),
                    objectif.getBudgetPrevisionnelCfa(),
                    objectif.getSeuilAlertePct()
            );
        }
    }

    public record SpeculationRequest(
            @NotBlank(message = "Le code est obligatoire") String code,
            @NotBlank(message = "Le nom est obligatoire") String nom,
            @NotNull(message = "Le seuil d'humidité est obligatoire")
            @PositiveOrZero Double seuilHumiditeMax,
            @NotNull(message = "Le seuil d'impureté est obligatoire")
            @PositiveOrZero Double seuilImpureteMax,
            Boolean active
    ) {
    }

    public record SpeculationResponse(
            Long id,
            String code,
            String nom,
            Double seuilHumiditeMax,
            Double seuilImpureteMax,
            Boolean active
    ) {
        public static SpeculationResponse from(Speculation speculation) {
            return new SpeculationResponse(speculation.getId(), speculation.getCode(), speculation.getNom(),
                    speculation.getSeuilHumiditeMax(), speculation.getSeuilImpureteMax(), speculation.getActive());
        }
    }

    public record PrixGarantieRequest(
            @NotNull(message = "La campagne est obligatoire") Long campagneId,
            @NotNull(message = "La spéculation est obligatoire") Long speculationId,
            UUID localityId,
            @NotNull(message = "Le prix unitaire est obligatoire")
            @Positive(message = "Le prix unitaire doit être strictement positif") Double prixUnitaireKg,
            @NotNull(message = "La date d'effet est obligatoire") LocalDate dateEffet
    ) {
    }

    public record PrixGarantieResponse(
            Long id,
            Long campagneId,
            String campagneCode,
            Long speculationId,
            String speculationCode,
            String speculationNom,
            UUID localityId,
            String localityNom,
            Double prixUnitaireKg,
            LocalDate dateEffet,
            String statut,
            String approuvePar,
            LocalDateTime dateApprobation
    ) {
        public static PrixGarantieResponse from(PrixGarantie prix) {
            return new PrixGarantieResponse(
                    prix.getId(),
                    prix.getCampagne().getId(),
                    prix.getCampagne().getCode(),
                    prix.getSpeculation().getId(),
                    prix.getSpeculation().getCode(),
                    prix.getSpeculation().getNom(),
                    prix.getLocality() != null ? prix.getLocality().getId() : null,
                    prix.getLocality() != null ? prix.getLocality().getName() : null,
                    prix.getPrixUnitaireKg(),
                    prix.getDateEffet(),
                    prix.getStatut().name(),
                    prix.getApprouvePar() != null ? prix.getApprouvePar().getNomComplet() : null,
                    prix.getDateApprobation()
            );
        }
    }

    public record ComptoirRequest(
            @NotBlank(message = "Le code est obligatoire") String code,
            @NotBlank(message = "Le nom est obligatoire") String nom,
            String type,
            @NotNull(message = "La localité est obligatoire") UUID localityId,
            Double latitudeGps,
            Double longitudeGps,
            @PositiveOrZero Double capaciteStockageKg,
            Boolean actif
    ) {
    }

    public record ComptoirResponse(
            Long id,
            String code,
            String nom,
            String type,
            UUID localityId,
            String localityNom,
            String regionNom,
            Double latitudeGps,
            Double longitudeGps,
            Double capaciteStockageKg,
            Boolean actif
    ) {
        public static ComptoirResponse from(Comptoir comptoir) {
            Locality locality = comptoir.getLocality();
            Locality region = locality;
            while (region != null && region.getType() != LocalityType.REGION) {
                region = region.getParent();
            }
            return new ComptoirResponse(
                    comptoir.getId(),
                    comptoir.getCode(),
                    comptoir.getNom(),
                    comptoir.getType(),
                    locality.getId(),
                    locality.getName(),
                    region != null ? region.getName() : null,
                    comptoir.getLatitudeGps(),
                    comptoir.getLongitudeGps(),
                    comptoir.getCapaciteStockageKg(),
                    comptoir.getActif()
            );
        }
    }

    public record ProducteurRequest(
            @NotBlank(message = "Le nom et prénom sont obligatoires") String nomPrenom,
            @NotBlank(message = "Le téléphone mobile money est obligatoire") String telephoneMobileMoney,
            @NotBlank(message = "L'opérateur mobile est obligatoire") String operateurMobile,
            @NotBlank(message = "Le numéro CNIB est obligatoire") String cnibNumero,
            LocalDate cnibDateDelivrance,
            String cnibLieuDelivrance,
            UUID localityId,
            String adressePrecision
    ) {
    }

    public record ProducteurResponse(
            Long id,
            String nomPrenom,
            String telephoneMobileMoney,
            String operateurMobile,
            String cnibNumero,
            LocalDate cnibDateDelivrance,
            String cnibLieuDelivrance,
            UUID localityId,
            String localityNom,
            String adressePrecision
    ) {
        public static ProducteurResponse from(Producteur producteur) {
            return new ProducteurResponse(
                    producteur.getId(),
                    producteur.getNomPrenom(),
                    producteur.getTelephoneMobileMoney(),
                    producteur.getOperateurMobile(),
                    producteur.getCnibNumero(),
                    producteur.getCnibDateDelivrance(),
                    producteur.getCnibLieuDelivrance(),
                    producteur.getLocality() != null ? producteur.getLocality().getId() : null,
                    producteur.getLocality() != null ? producteur.getLocality().getName() : null,
                    producteur.getAdressePrecision()
            );
        }
    }

    public record LocalityResponse(
            UUID id,
            String code,
            String name,
            String type,
            UUID parentId,
            String parentName,
            Double latitude,
            Double longitude
    ) {
        public static LocalityResponse from(Locality locality) {
            return new LocalityResponse(
                    locality.getId(),
                    locality.getCode(),
                    locality.getName(),
                    locality.getType().name(),
                    locality.getParent() != null ? locality.getParent().getId() : null,
                    locality.getParent() != null ? locality.getParent().getName() : null,
                    locality.getLatitude(),
                    locality.getLongitude()
            );
        }
    }

    public record MagasinRequest(
            @NotBlank(message = "Le code magasin est obligatoire") String codeMagasin,
            @NotBlank(message = "Le nom est obligatoire") String nom,
            @NotBlank(message = "La ville est obligatoire") String ville,
            UUID localityId,
            @NotNull(message = "La capacité est obligatoire")
            @Positive(message = "La capacité doit être strictement positive") Double capaciteTotaleTonnes,
            Boolean actif
    ) {
    }

    public record MagasinResponse(
            Long id,
            String codeMagasin,
            String nom,
            String ville,
            UUID localityId,
            Double capaciteTotaleTonnes,
            Double stockActuelTonnes,
            Boolean actif
    ) {
        public static MagasinResponse from(MagasinCentral magasin) {
            return new MagasinResponse(magasin.getId(), magasin.getCodeMagasin(), magasin.getNom(),
                    magasin.getVille(),
                    magasin.getLocality() != null ? magasin.getLocality().getId() : null,
                    magasin.getCapaciteTotaleTonnes(), magasin.getStockActuelTonnes(),
                    magasin.getActif());
        }
    }
}
