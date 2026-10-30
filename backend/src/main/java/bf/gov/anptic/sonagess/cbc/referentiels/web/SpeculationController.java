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
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/speculations")
@RequiredArgsConstructor
public class SpeculationController {

    private final ReferentielService referentielService;

    @GetMapping
    public ResponseEntity<ApiResponse<List<ReferentielDtos.SpeculationResponse>>> lister(
            @RequestParam(defaultValue = "true") boolean activesSeulement) {
        return ResponseEntity.ok(ApiResponse.ok(
                referentielService.listerSpeculations(activesSeulement)));
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'DCT')")
    public ResponseEntity<ApiResponse<ReferentielDtos.SpeculationResponse>> creer(
            @Valid @RequestBody ReferentielDtos.SpeculationRequest request,
            @AuthenticationPrincipal CustomUserDetails principal) {
        return ResponseEntity.ok(ApiResponse.ok(
                referentielService.creerSpeculation(request,
                        referentielService.utilisateurCourant(principal.getId())),
                "Spéculation créée."));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'DCT')")
    public ResponseEntity<ApiResponse<ReferentielDtos.SpeculationResponse>> modifier(
            @PathVariable Long id,
            @Valid @RequestBody ReferentielDtos.SpeculationRequest request,
            @AuthenticationPrincipal CustomUserDetails principal) {
        return ResponseEntity.ok(ApiResponse.ok(
                referentielService.modifierSpeculation(id, request,
                        referentielService.utilisateurCourant(principal.getId())),
                "Spéculation modifiée."));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'DCT')")
    public ResponseEntity<ApiResponse<Void>> supprimer(
            @PathVariable Long id,
            @AuthenticationPrincipal CustomUserDetails principal) {
        referentielService.supprimerSpeculation(id,
                referentielService.utilisateurCourant(principal.getId()));
        return ResponseEntity.ok(ApiResponse.<Void>ok(null, "Spéculation supprimée."));
    }
}
