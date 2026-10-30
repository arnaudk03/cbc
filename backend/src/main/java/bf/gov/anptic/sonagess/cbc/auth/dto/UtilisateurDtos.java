package bf.gov.anptic.sonagess.cbc.auth.dto;

import bf.gov.anptic.sonagess.cbc.auth.domain.Role;
import bf.gov.anptic.sonagess.cbc.auth.domain.Utilisateur;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;

import java.time.LocalDateTime;
import java.util.List;

public final class UtilisateurDtos {

    private UtilisateurDtos() {
    }

    public record UtilisateurRequest(
            @NotBlank(message = "Le matricule est obligatoire") String matricule,
            @NotBlank(message = "Le nom est obligatoire") String nom,
            @NotBlank(message = "Le prénom est obligatoire") String prenom,
            @NotBlank(message = "L'adresse e-mail est obligatoire")
            @Email(message = "L'adresse e-mail est invalide") String email,
            @NotBlank(message = "Le téléphone est obligatoire") String telephone,
            String motDePasse,
            @NotEmpty(message = "Au moins un rôle est obligatoire") List<String> roles,
            Boolean actif,
            Long comptoirId
    ) {
    }

    public record UtilisateurResponse(
            Long id,
            String matricule,
            String nom,
            String prenom,
            String email,
            String telephone,
            String nomComplet,
            boolean actif,
            boolean compteBloque,
            Long comptoirId,
            String comptoirCode,
            String comptoirNom,
            List<String> roles,
            LocalDateTime dateDerniereConnexion,
            LocalDateTime creeLe
    ) {
        public static UtilisateurResponse from(Utilisateur utilisateur) {
            return new UtilisateurResponse(
                    utilisateur.getId(),
                    utilisateur.getMatricule(),
                    utilisateur.getNom(),
                    utilisateur.getPrenom(),
                    utilisateur.getEmail(),
                    utilisateur.getTelephone(),
                    utilisateur.getNomComplet(),
                    Boolean.TRUE.equals(utilisateur.getActif()),
                    Boolean.TRUE.equals(utilisateur.getCompteBloque()),
                    utilisateur.getComptoir() != null ? utilisateur.getComptoir().getId() : null,
                    utilisateur.getComptoir() != null ? utilisateur.getComptoir().getCode() : null,
                    utilisateur.getComptoir() != null ? utilisateur.getComptoir().getNom() : null,
                    utilisateur.getRoles().stream().map(Role::getCode).sorted().toList(),
                    utilisateur.getDateDerniereConnexion(),
                    utilisateur.getCreeLe()
            );
        }
    }

    public record RoleOption(String code, String libelle) {
        public static RoleOption from(Role role) {
            return new RoleOption(role.getCode(), role.getLibelle());
        }
    }
}
