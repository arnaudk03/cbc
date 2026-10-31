package bf.gov.anptic.sonagess.cbc.referentiels.web;

import bf.gov.anptic.sonagess.cbc.auth.dto.UtilisateurDtos;
import bf.gov.anptic.sonagess.cbc.auth.service.UtilisateurService;
import bf.gov.anptic.sonagess.cbc.referentiels.service.ReferentielService;
import bf.gov.anptic.sonagess.cbc.common.ApiResponse;
import bf.gov.anptic.sonagess.cbc.security.CustomUserDetails;
import bf.gov.anptic.sonagess.cbc.referentiels.dto.ReferentielDtos;
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
@RequestMapping("/comptoirs")
@RequiredArgsConstructor
public class ComptoirController {

    private final ReferentielService referentielService;
    private final UtilisateurService utilisateurService;

    @GetMapping
    public ResponseEntity<ApiResponse<List<ReferentielDtos.ComptoirResponse>>> lister(
            @RequestParam(defaultValue = "false") boolean actifsSeulement) {
        return ResponseEntity.ok(ApiResponse.ok(
                referentielService.listerComptoirs(actifsSeulement)));
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'DCT')")
    public ResponseEntity<ApiResponse<ReferentielDtos.ComptoirResponse>> creer(
            @Valid @RequestBody ReferentielDtos.ComptoirRequest request,
            @AuthenticationPrincipal CustomUserDetails principal) {
        return ResponseEntity.ok(ApiResponse.ok(
                referentielService.creerComptoir(request,
                        referentielService.utilisateurCourant(principal.getId())),
                "Comptoir créé."));
    }

    @PatchMapping("/{id}/activation")
    @PreAuthorize("hasAnyRole('ADMIN', 'DCT')")
    public ResponseEntity<ApiResponse<ReferentielDtos.ComptoirResponse>> activer(
            @PathVariable Long id,
            @RequestParam boolean actif,
            @AuthenticationPrincipal CustomUserDetails principal) {
        return ResponseEntity.ok(ApiResponse.ok(
                referentielService.changerActivationComptoir(id, actif,
                        referentielService.utilisateurCourant(principal.getId())),
                actif ? "Comptoir ouvert." : "Comptoir fermé."));
    }

    @PostMapping("/{id}/utilisateurs")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<UtilisateurDtos.AffectationComptoirResponse>> affecterUtilisateurs(
            @PathVariable Long id,
            @Valid @RequestBody UtilisateurDtos.AffectationComptoirRequest request,
            @AuthenticationPrincipal CustomUserDetails principal) {
        return ResponseEntity.ok(ApiResponse.ok(
                utilisateurService.affecterAuComptoir(id, request.utilisateurIds(),
                        referentielService.utilisateurCourant(principal.getId())),
                "Utilisateurs affectés au comptoir."));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'DCT')")
    public ResponseEntity<ApiResponse<ReferentielDtos.ComptoirResponse>> modifier(
            @PathVariable Long id,
            @Valid @RequestBody ReferentielDtos.ComptoirRequest request,
            @AuthenticationPrincipal CustomUserDetails principal) {
        return ResponseEntity.ok(ApiResponse.ok(
                referentielService.modifierComptoir(id, request,
                        referentielService.utilisateurCourant(principal.getId())),
                "Comptoir modifié."));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'DCT')")
    public ResponseEntity<ApiResponse<Void>> supprimer(
            @PathVariable Long id,
            @AuthenticationPrincipal CustomUserDetails principal) {
        referentielService.supprimerComptoir(id,
                referentielService.utilisateurCourant(principal.getId()));
        return ResponseEntity.ok(ApiResponse.<Void>ok(null, "Comptoir supprimé."));
    }
}
