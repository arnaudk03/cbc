package bf.gov.anptic.sonagess.cbc.referentiels.web;

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
@RequestMapping("/prix-garantis")
@RequiredArgsConstructor
public class PrixGarantieController {

    private final ReferentielService referentielService;

    @GetMapping
    public ResponseEntity<ApiResponse<List<ReferentielDtos.PrixGarantieResponse>>> lister(
            @RequestParam Long campagneId,
            @RequestParam(required = false) Long speculationId) {
        return ResponseEntity.ok(ApiResponse.ok(
                referentielService.listerPrix(campagneId, speculationId)));
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'DCT', 'DFC_DIRECTEUR')")
    public ResponseEntity<ApiResponse<ReferentielDtos.PrixGarantieResponse>> creer(
            @Valid @RequestBody ReferentielDtos.PrixGarantieRequest request,
            @AuthenticationPrincipal CustomUserDetails principal) {
        return ResponseEntity.ok(ApiResponse.ok(
                referentielService.creerPrix(request,
                        referentielService.utilisateurCourant(principal.getId())),
                "Prix garanti soumis à approbation."));
    }

    @PatchMapping("/{id}/approbation")
    @PreAuthorize("hasAnyRole('ADMIN', 'DCT')")
    public ResponseEntity<ApiResponse<ReferentielDtos.PrixGarantieResponse>> approuver(
            @PathVariable Long id,
            @AuthenticationPrincipal CustomUserDetails principal) {
        return ResponseEntity.ok(ApiResponse.ok(
                referentielService.approuverPrix(id,
                        referentielService.utilisateurCourant(principal.getId())),
                "Prix garanti approuvé."));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'DCT', 'DFC_DIRECTEUR')")
    public ResponseEntity<ApiResponse<ReferentielDtos.PrixGarantieResponse>> modifier(
            @PathVariable Long id,
            @Valid @RequestBody ReferentielDtos.PrixGarantieRequest request,
            @AuthenticationPrincipal CustomUserDetails principal) {
        return ResponseEntity.ok(ApiResponse.ok(
                referentielService.modifierPrix(id, request,
                        referentielService.utilisateurCourant(principal.getId())),
                "Prix garanti modifié et soumis à approbation."));
    }

    @PatchMapping("/{id}/rejet")
    @PreAuthorize("hasAnyRole('ADMIN', 'DCT')")
    public ResponseEntity<ApiResponse<ReferentielDtos.PrixGarantieResponse>> rejeter(
            @PathVariable Long id,
            @AuthenticationPrincipal CustomUserDetails principal) {
        return ResponseEntity.ok(ApiResponse.ok(
                referentielService.rejeterPrix(id,
                        referentielService.utilisateurCourant(principal.getId())),
                "Prix garanti désactivé."));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'DCT')")
    public ResponseEntity<ApiResponse<Void>> supprimer(
            @PathVariable Long id,
            @AuthenticationPrincipal CustomUserDetails principal) {
        referentielService.supprimerPrix(id,
                referentielService.utilisateurCourant(principal.getId()));
        return ResponseEntity.ok(ApiResponse.<Void>ok(null, "Prix garanti supprimé."));
    }
}
