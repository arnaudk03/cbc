package bf.gov.anptic.sonagess.cbc.equipements.dto;

import bf.gov.anptic.sonagess.cbc.equipements.domain.AffectationEquipement;
import bf.gov.anptic.sonagess.cbc.equipements.domain.Equipement;
import bf.gov.anptic.sonagess.cbc.equipements.domain.EtatEquipement;
import bf.gov.anptic.sonagess.cbc.equipements.domain.StatutEquipement;
import bf.gov.anptic.sonagess.cbc.equipements.domain.TabletteDevice;
import bf.gov.anptic.sonagess.cbc.equipements.domain.TypeEquipement;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

public final class EquipementDtos {

    private EquipementDtos() {
    }

    public record EquipementRequest(
            @NotNull(message = "Le type d'équipement est obligatoire") TypeEquipement type,
            @NotBlank(message = "La désignation est obligatoire") String designation,
            String code,
            String numeroSerie,
            String marque,
            String modele,
            EtatEquipement etat,
            LocalDate dateAcquisition,
            @PositiveOrZero(message = "La valeur doit être positive ou nulle") Double valeurCfa,
            String observation,
            String imei
    ) {
    }

    public record AffectationEquipementRequest(
            @NotNull(message = "Le comptoir est obligatoire") Long comptoirId,
            Long utilisateurId,
            String motif
    ) {
    }

    public record AffectationEquipementBatchRequest(
            @NotEmpty(message = "Sélectionnez au moins un équipement") List<Long> equipementIds,
            @NotNull(message = "Le comptoir est obligatoire") Long comptoirId,
            Long utilisateurId,
            String motif
    ) {
    }

    public record StatutEquipementRequest(
            @NotNull(message = "Le statut est obligatoire") StatutEquipement statut
    ) {
    }

    public record AffectationEquipementResponse(
            Long id,
            Long comptoirId,
            String comptoirCode,
            String comptoirNom,
            Long utilisateurId,
            String utilisateurNomComplet,
            LocalDateTime dateDebut
    ) {
        public static AffectationEquipementResponse from(AffectationEquipement affectation) {
            if (affectation == null) {
                return null;
            }
            return new AffectationEquipementResponse(
                    affectation.getId(),
                    affectation.getComptoir().getId(),
                    affectation.getComptoir().getCode(),
                    affectation.getComptoir().getNom(),
                    affectation.getUtilisateur() != null ? affectation.getUtilisateur().getId() : null,
                    affectation.getUtilisateur() != null
                            ? affectation.getUtilisateur().getNomComplet() : null,
                    affectation.getDateDebut()
            );
        }
    }

    public record TabletteResponse(
            Long id,
            String imei,
            LocalDateTime dateDerniereSynchro
    ) {
        public static TabletteResponse from(TabletteDevice tablette) {
            if (tablette == null) {
                return null;
            }
            return new TabletteResponse(
                    tablette.getId(),
                    tablette.getImei(),
                    tablette.getDateDerniereSynchro()
            );
        }
    }

    public record EquipementResponse(
            Long id,
            String code,
            TypeEquipement type,
            String designation,
            String numeroSerie,
            String marque,
            String modele,
            EtatEquipement etat,
            StatutEquipement statut,
            LocalDate dateAcquisition,
            Double valeurCfa,
            String observation,
            LocalDateTime creeLe,
            AffectationEquipementResponse affectation,
            TabletteResponse tablette
    ) {
        public static EquipementResponse from(Equipement equipement,
                                              AffectationEquipement affectation) {
            return new EquipementResponse(
                    equipement.getId(),
                    equipement.getCode(),
                    equipement.getType(),
                    equipement.getDesignation(),
                    equipement.getNumeroSerie(),
                    equipement.getMarque(),
                    equipement.getModele(),
                    equipement.getEtat(),
                    equipement.getStatut(),
                    equipement.getDateAcquisition(),
                    equipement.getValeurCfa(),
                    equipement.getObservation(),
                    equipement.getCreeLe(),
                    AffectationEquipementResponse.from(affectation),
                    TabletteResponse.from(equipement.getTablette())
            );
        }
    }
}
