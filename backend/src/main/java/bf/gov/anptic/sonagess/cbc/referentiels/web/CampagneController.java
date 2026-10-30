package bf.gov.anptic.sonagess.cbc.referentiels.web;

import bf.gov.anptic.sonagess.cbc.referentiels.service.ReferentielService;
import bf.gov.anptic.sonagess.cbc.common.ApiResponse;
import bf.gov.anptic.sonagess.cbc.referentiels.domain.CampagneStatut;
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
@RequestMapping("/campagnes")
@RequiredArgsConstructor
public class CampagneController {

    private final ReferentielService referentielService;

    @GetMapping
    public ResponseEntity<ApiResponse<List<ReferentielDtos.CampagneResponse>>> lister() {
        return ResponseEntity.ok(ApiResponse.ok(referentielService.listerCampagnes()));
    }

    @GetMapping("/active")
    public ResponseEntity<ApiResponse<ReferentielDtos.CampagneResponse>> active() {
        return ResponseEntity.ok(ApiResponse.ok(
                ReferentielDtos.CampagneResponse.from(referentielService.campagneActive())));
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'DCT')")
    public ResponseEntity<ApiResponse<ReferentielDtos.CampagneResponse>> creer(
            @Valid @RequestBody ReferentielDtos.CampagneRequest request,
            @AuthenticationPrincipal CustomUserDetails principal) {
        return ResponseEntity.ok(ApiResponse.ok(
                referentielService.creerCampagne(request,
                        referentielService.utilisateurCourant(principal.getId())),
                "Campagne créée avec succès."));
    }

    @PatchMapping("/{id}/statut")
    @PreAuthorize("hasAnyRole('ADMIN', 'DCT')")
    public ResponseEntity<ApiResponse<ReferentielDtos.CampagneResponse>> changerStatut(
            @PathVariable Long id,
            @RequestParam CampagneStatut statut,
            @AuthenticationPrincipal CustomUserDetails principal) {
        return ResponseEntity.ok(ApiResponse.ok(
                referentielService.changerStatutCampagne(id, statut,
                        referentielService.utilisateurCourant(principal.getId())),
                "Statut de la campagne mis à jour."));
    }

    @GetMapping("/{id}/objectifs")
    public ResponseEntity<ApiResponse<List<ReferentielDtos.ObjectifResponse>>> objectifs(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.ok(referentielService.listerObjectifs(id)));
    }

    @PostMapping("/{id}/objectifs")
    @PreAuthorize("hasAnyRole('ADMIN', 'DCT')")
    public ResponseEntity<ApiResponse<ReferentielDtos.ObjectifResponse>> creerObjectif(
            @PathVariable Long id,
            @Valid @RequestBody ReferentielDtos.ObjectifRequest request,
            @AuthenticationPrincipal CustomUserDetails principal) {
        return ResponseEntity.ok(ApiResponse.ok(
                referentielService.creerObjectif(id, request,
                        referentielService.utilisateurCourant(principal.getId())),
                "Objectif enregistré."));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'DCT')")
    public ResponseEntity<ApiResponse<ReferentielDtos.CampagneResponse>> modifier(
            @PathVariable Long id,
            @Valid @RequestBody ReferentielDtos.CampagneRequest request,
            @AuthenticationPrincipal CustomUserDetails principal) {
        return ResponseEntity.ok(ApiResponse.ok(
                referentielService.modifierCampagne(id, request,
                        referentielService.utilisateurCourant(principal.getId())),
                "Campagne modifiée."));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'DCT')")
    public ResponseEntity<ApiResponse<Void>> supprimer(
            @PathVariable Long id,
            @AuthenticationPrincipal CustomUserDetails principal) {
        referentielService.supprimerCampagne(id,
                referentielService.utilisateurCourant(principal.getId()));
        return ResponseEntity.ok(ApiResponse.<Void>ok(null, "Campagne supprimée."));
    }
}
