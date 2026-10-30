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
@RequestMapping("/magasins")
@RequiredArgsConstructor
public class MagasinController {

    private final ReferentielService referentielService;

    @GetMapping
    public ResponseEntity<ApiResponse<List<ReferentielDtos.MagasinResponse>>> lister(
            @RequestParam(defaultValue = "false") boolean actifsSeulement) {
        return ResponseEntity.ok(ApiResponse.ok(
                referentielService.listerMagasins(actifsSeulement)));
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'DGSA', 'SGS')")
    public ResponseEntity<ApiResponse<ReferentielDtos.MagasinResponse>> creer(
            @Valid @RequestBody ReferentielDtos.MagasinRequest request,
            @AuthenticationPrincipal CustomUserDetails principal) {
        return ResponseEntity.ok(ApiResponse.ok(
                referentielService.creerMagasin(request,
                        referentielService.utilisateurCourant(principal.getId())),
                "Magasin central créé."));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'DGSA', 'SGS')")
    public ResponseEntity<ApiResponse<ReferentielDtos.MagasinResponse>> modifier(
            @PathVariable Long id,
            @Valid @RequestBody ReferentielDtos.MagasinRequest request,
            @AuthenticationPrincipal CustomUserDetails principal) {
        return ResponseEntity.ok(ApiResponse.ok(
                referentielService.modifierMagasin(id, request,
                        referentielService.utilisateurCourant(principal.getId())),
                "Magasin central modifié."));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'DGSA', 'SGS')")
    public ResponseEntity<ApiResponse<Void>> supprimer(
            @PathVariable Long id,
            @AuthenticationPrincipal CustomUserDetails principal) {
        referentielService.supprimerMagasin(id,
                referentielService.utilisateurCourant(principal.getId()));
        return ResponseEntity.ok(ApiResponse.<Void>ok(null, "Magasin central supprimé."));
    }
}
