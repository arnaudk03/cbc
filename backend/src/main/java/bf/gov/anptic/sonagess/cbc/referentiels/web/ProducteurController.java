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
@RequestMapping("/producteurs")
@RequiredArgsConstructor
public class ProducteurController {

    private final ReferentielService referentielService;

    @GetMapping
    public ResponseEntity<ApiResponse<List<ReferentielDtos.ProducteurResponse>>> rechercher(
            @RequestParam(required = false) String q) {
        return ResponseEntity.ok(ApiResponse.ok(
                referentielService.rechercherProducteurs(q)));
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'DCT', 'CHEF_COMPTOIR', 'AGENT_COLLECTEUR')")
    public ResponseEntity<ApiResponse<ReferentielDtos.ProducteurResponse>> creer(
            @Valid @RequestBody ReferentielDtos.ProducteurRequest request,
            @AuthenticationPrincipal CustomUserDetails principal) {
        return ResponseEntity.ok(ApiResponse.ok(
                referentielService.creerProducteur(request,
                        referentielService.utilisateurCourant(principal.getId())),
                "Producteur enregistré."));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'DCT', 'CHEF_COMPTOIR', 'AGENT_COLLECTEUR')")
    public ResponseEntity<ApiResponse<ReferentielDtos.ProducteurResponse>> modifier(
            @PathVariable Long id,
            @Valid @RequestBody ReferentielDtos.ProducteurRequest request,
            @AuthenticationPrincipal CustomUserDetails principal) {
        return ResponseEntity.ok(ApiResponse.ok(
                referentielService.modifierProducteur(id, request,
                        referentielService.utilisateurCourant(principal.getId())),
                "Producteur modifié."));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'DCT', 'CHEF_COMPTOIR', 'AGENT_COLLECTEUR')")
    public ResponseEntity<ApiResponse<Void>> supprimer(
            @PathVariable Long id,
            @AuthenticationPrincipal CustomUserDetails principal) {
        referentielService.supprimerProducteur(id,
                referentielService.utilisateurCourant(principal.getId()));
        return ResponseEntity.ok(ApiResponse.<Void>ok(null, "Producteur supprimé."));
    }
}
