package bf.gov.anptic.sonagess.cbc.auth.web;

import bf.gov.anptic.sonagess.cbc.auth.dto.UtilisateurDtos;
import bf.gov.anptic.sonagess.cbc.auth.service.UtilisateurService;
import bf.gov.anptic.sonagess.cbc.common.ApiResponse;
import bf.gov.anptic.sonagess.cbc.security.CustomUserDetails;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/utilisateurs")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class UtilisateurController {

    private final UtilisateurService utilisateurService;

    @GetMapping
    public ResponseEntity<ApiResponse<List<UtilisateurDtos.UtilisateurResponse>>> lister() {
        return ResponseEntity.ok(ApiResponse.ok(utilisateurService.lister()));
    }

    @GetMapping("/roles")
    public ResponseEntity<ApiResponse<List<UtilisateurDtos.RoleOption>>> roles() {
        return ResponseEntity.ok(ApiResponse.ok(utilisateurService.listerRoles()));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<UtilisateurDtos.UtilisateurResponse>> creer(
            @Valid @RequestBody UtilisateurDtos.UtilisateurRequest request,
            @AuthenticationPrincipal CustomUserDetails principal) {
        return ResponseEntity.ok(ApiResponse.ok(
                utilisateurService.creer(request, utilisateurService.acteur(principal.getId())),
                "Utilisateur créé."));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<UtilisateurDtos.UtilisateurResponse>> modifier(
            @PathVariable Long id,
            @Valid @RequestBody UtilisateurDtos.UtilisateurRequest request,
            @AuthenticationPrincipal CustomUserDetails principal) {
        return ResponseEntity.ok(ApiResponse.ok(
                utilisateurService.modifier(id, request, utilisateurService.acteur(principal.getId())),
                "Utilisateur modifié."));
    }

    @PatchMapping("/{id}/activation")
    public ResponseEntity<ApiResponse<UtilisateurDtos.UtilisateurResponse>> activation(
            @PathVariable Long id,
            @RequestParam boolean actif,
            @AuthenticationPrincipal CustomUserDetails principal) {
        return ResponseEntity.ok(ApiResponse.ok(
                utilisateurService.changerActivation(id, actif,
                        utilisateurService.acteur(principal.getId())),
                actif ? "Utilisateur activé." : "Utilisateur désactivé."));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> supprimer(
            @PathVariable Long id,
            @AuthenticationPrincipal CustomUserDetails principal) {
        utilisateurService.supprimer(id, utilisateurService.acteur(principal.getId()));
        return ResponseEntity.ok(ApiResponse.<Void>ok(null, "Utilisateur désactivé."));
    }
}
