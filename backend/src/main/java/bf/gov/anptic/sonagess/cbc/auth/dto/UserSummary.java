package bf.gov.anptic.sonagess.cbc.auth.dto;

import bf.gov.anptic.sonagess.cbc.auth.domain.Permission;
import bf.gov.anptic.sonagess.cbc.auth.domain.Role;
import bf.gov.anptic.sonagess.cbc.auth.domain.Utilisateur;

import java.util.List;
import java.util.TreeSet;

public record UserSummary(
        Long id,
        String matricule,
        String nom,
        String prenom,
        String email,
        String telephone,
        Long comptoirId,
        String comptoirCode,
        String comptoirNom,
        List<String> roles,
        List<String> permissions
) {
    public static UserSummary from(Utilisateur utilisateur) {
        return new UserSummary(
                utilisateur.getId(),
                utilisateur.getMatricule(),
                utilisateur.getNom(),
                utilisateur.getPrenom(),
                utilisateur.getEmail(),
                utilisateur.getTelephone(),
                utilisateur.getComptoir() != null ? utilisateur.getComptoir().getId() : null,
                utilisateur.getComptoir() != null ? utilisateur.getComptoir().getCode() : null,
                utilisateur.getComptoir() != null ? utilisateur.getComptoir().getNom() : null,
                utilisateur.getRoles().stream()
                        .map(Role::getCode)
                        .sorted()
                        .toList(),
                utilisateur.getRoles().stream()
                        .flatMap(role -> role.getPermissions().stream())
                        .map(Permission::getCode)
                        .collect(java.util.stream.Collectors.toCollection(TreeSet::new))
                        .stream()
                        .toList()
        );
    }
}
