package bf.gov.anptic.sonagess.cbc.stocks.dto;

import bf.gov.anptic.sonagess.cbc.stocks.domain.CategorieMaterielEmballage;
import bf.gov.anptic.sonagess.cbc.stocks.domain.FicheStockMateriel;
import bf.gov.anptic.sonagess.cbc.stocks.domain.LigneFicheStockMateriel;
import bf.gov.anptic.sonagess.cbc.stocks.domain.MaterielEmballage;
import bf.gov.anptic.sonagess.cbc.stocks.domain.StatutFicheStockMateriel;
import bf.gov.anptic.sonagess.cbc.stocks.domain.UniteMateriel;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

public final class EmballageDtos {

    private EmballageDtos() {
    }

    public record MaterielRequest(
            String code,
            @NotBlank(message = "Le libellé est obligatoire") String libelle,
            CategorieMaterielEmballage categorie,
            UniteMateriel unite,
            @PositiveOrZero(message = "Le seuil doit être positif ou nul") Integer seuilAlerte,
            Boolean actif
    ) {
    }

    public record MaterielResponse(
            Long id,
            String code,
            String libelle,
            CategorieMaterielEmballage categorie,
            UniteMateriel unite,
            Integer seuilAlerte,
            Boolean actif
    ) {
        public static MaterielResponse from(MaterielEmballage materiel) {
            return new MaterielResponse(
                    materiel.getId(),
                    materiel.getCode(),
                    materiel.getLibelle(),
                    materiel.getCategorie(),
                    materiel.getUnite(),
                    materiel.getSeuilAlerte(),
                    materiel.getActif()
            );
        }
    }

    public record LigneFicheRequest(
            @NotNull(message = "Le matériel est obligatoire") Long materielId,
            @PositiveOrZero(message = "Les entrées doivent être positives ou nulles") Integer entrees,
            @PositiveOrZero(message = "Les sorties doivent être positives ou nulles") Integer sorties,
            @PositiveOrZero(message = "Le stock physique doit être positif ou nul") Integer stockPhysique,
            String referenceDoc,
            String observation
    ) {
    }

    public record FicheRequest(
            @NotNull(message = "Le comptoir est obligatoire") Long comptoirId,
            @NotNull(message = "La date de la fiche est obligatoire") LocalDate dateFiche,
            String observations,
            @NotEmpty(message = "Ajoutez au moins une ligne de matériel")
            @Valid List<LigneFicheRequest> lignes
    ) {
    }

    public record LigneFicheResponse(
            Long id,
            Long materielId,
            String materielCode,
            String materielLibelle,
            CategorieMaterielEmballage categorie,
            UniteMateriel unite,
            Integer seuilAlerte,
            Integer stockInitial,
            Integer entrees,
            Integer sorties,
            Integer stockTheorique,
            Integer stockPhysique,
            Integer ecart,
            Integer stockFinal,
            String referenceDoc,
            String observation,
            boolean alerte
    ) {
        public static LigneFicheResponse from(LigneFicheStockMateriel ligne) {
            if (ligne == null) {
                return null;
            }
            MaterielEmballage materiel = ligne.getMateriel();
            int theorique = ligne.getStockInitial() + ligne.getEntrees() - ligne.getSorties();
            int finalStock = ligne.getStockFinal() != null ? ligne.getStockFinal() : theorique;
            int seuil = materiel.getSeuilAlerte() != null ? materiel.getSeuilAlerte() : 0;
            return new LigneFicheResponse(
                    ligne.getId(),
                    materiel.getId(),
                    materiel.getCode(),
                    materiel.getLibelle(),
                    materiel.getCategorie(),
                    materiel.getUnite(),
                    seuil,
                    ligne.getStockInitial(),
                    ligne.getEntrees(),
                    ligne.getSorties(),
                    theorique,
                    ligne.getStockPhysique(),
                    ligne.getEcart(),
                    finalStock,
                    ligne.getReferenceDoc(),
                    ligne.getObservation(),
                    finalStock <= seuil
            );
        }
    }

    public record FicheResponse(
            Long id,
            String numeroFiche,
            Long comptoirId,
            String comptoirCode,
            String comptoirNom,
            LocalDate dateFiche,
            StatutFicheStockMateriel statut,
            String observations,
            String creePar,
            String validePar,
            LocalDateTime creeLe,
            LocalDateTime valideLe,
            int nombreLignes,
            List<LigneFicheResponse> lignes
    ) {
        public static FicheResponse from(FicheStockMateriel fiche) {
            List<LigneFicheStockMateriel> lignes = fiche.getLignes() != null
                    ? fiche.getLignes() : List.of();
            return new FicheResponse(
                    fiche.getId(),
                    fiche.getNumeroFiche(),
                    fiche.getComptoir().getId(),
                    fiche.getComptoir().getCode(),
                    fiche.getComptoir().getNom(),
                    fiche.getDateFiche(),
                    fiche.getStatut(),
                    fiche.getObservations(),
                    fiche.getCreePar() != null ? fiche.getCreePar().getNomComplet() : null,
                    fiche.getValidePar() != null ? fiche.getValidePar().getNomComplet() : null,
                    fiche.getCreeLe(),
                    fiche.getValideLe(),
                    lignes.size(),
                    lignes.stream().map(LigneFicheResponse::from).toList()
            );
        }
    }

    public record StockMaterielResponse(
            Long materielId,
            String code,
            String libelle,
            CategorieMaterielEmballage categorie,
            UniteMateriel unite,
            Integer seuilAlerte,
            Integer stockFinal,
            boolean alerte
    ) {
    }
}
