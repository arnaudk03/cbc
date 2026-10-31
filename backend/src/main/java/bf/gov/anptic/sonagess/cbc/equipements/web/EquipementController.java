package bf.gov.anptic.sonagess.cbc.equipements.web;

import bf.gov.anptic.sonagess.cbc.common.ApiResponse;
import bf.gov.anptic.sonagess.cbc.equipements.dto.EquipementDtos;
import bf.gov.anptic.sonagess.cbc.equipements.service.EquipementService;
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
@RequestMapping("/equipements")
@RequiredArgsConstructor
public class EquipementController {

    private final EquipementService equipementService;

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'DCT', 'DGSA', 'SGS', 'DPL', 'CHEF_COMPTOIR', "
            + "'AGENT_COLLECTEUR', 'CONSULTATION')")
    public ResponseEntity<ApiResponse<List<EquipementDtos.EquipementResponse>>> lister(
            @RequestParam(required = false) Long comptoirId,
            @RequestParam(defaultValue = "false") boolean disponibles) {
        return ResponseEntity.ok(ApiResponse.ok(
                equipementService.lister(comptoirId, disponibles)));
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'DCT')")
    public ResponseEntity<ApiResponse<EquipementDtos.EquipementResponse>> creer(
            @Valid @RequestBody EquipementDtos.EquipementRequest request,
            @AuthenticationPrincipal CustomUserDetails principal) {
        return ResponseEntity.ok(ApiResponse.ok(
                equipementService.creer(request, principal.getId()),
                "Équipement créé."));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'DCT')")
    public ResponseEntity<ApiResponse<EquipementDtos.EquipementResponse>> modifier(
            @PathVariable Long id,
            @Valid @RequestBody EquipementDtos.EquipementRequest request,
            @AuthenticationPrincipal CustomUserDetails principal) {
        return ResponseEntity.ok(ApiResponse.ok(
                equipementService.modifier(id, request, principal.getId()),
                "Équipement modifié."));
    }

    @PostMapping("/{id}/affectation")
    @PreAuthorize("hasAnyRole('ADMIN', 'DCT')")
    public ResponseEntity<ApiResponse<EquipementDtos.EquipementResponse>> affecter(
            @PathVariable Long id,
            @Valid @RequestBody EquipementDtos.AffectationEquipementRequest request,
            @AuthenticationPrincipal CustomUserDetails principal) {
        return ResponseEntity.ok(ApiResponse.ok(
                equipementService.affecter(id, request, principal.getId()),
                "Équipement affecté au comptoir."));
    }

    @PostMapping("/affectations")
    @PreAuthorize("hasAnyRole('ADMIN', 'DCT')")
    public ResponseEntity<ApiResponse<List<EquipementDtos.EquipementResponse>>> affecterEnLot(
            @Valid @RequestBody EquipementDtos.AffectationEquipementBatchRequest request,
            @AuthenticationPrincipal CustomUserDetails principal) {
        return ResponseEntity.ok(ApiResponse.ok(
                equipementService.affecterEnLot(request, principal.getId()),
                "Équipements affectés au comptoir."));
    }

    @DeleteMapping("/{id}/affectation")
    @PreAuthorize("hasAnyRole('ADMIN', 'DCT')")
    public ResponseEntity<ApiResponse<EquipementDtos.EquipementResponse>> retirer(
            @PathVariable Long id,
            @RequestParam(required = false) String motif,
            @AuthenticationPrincipal CustomUserDetails principal) {
        return ResponseEntity.ok(ApiResponse.ok(
                equipementService.retirer(id, motif, principal.getId()),
                "Équipement retiré du comptoir."));
    }

    @PatchMapping("/{id}/statut")
    @PreAuthorize("hasAnyRole('ADMIN', 'DCT')")
    public ResponseEntity<ApiResponse<EquipementDtos.EquipementResponse>> changerStatut(
            @PathVariable Long id,
            @Valid @RequestBody EquipementDtos.StatutEquipementRequest request,
            @AuthenticationPrincipal CustomUserDetails principal) {
        return ResponseEntity.ok(ApiResponse.ok(
                equipementService.changerStatut(id, request.statut(), principal.getId()),
                "Statut de l'équipement mis à jour."));
    }

    @PostMapping("/{id}/synchronisation")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<EquipementDtos.EquipementResponse>> synchroniser(
            @PathVariable Long id,
            @AuthenticationPrincipal CustomUserDetails principal) {
        return ResponseEntity.ok(ApiResponse.ok(
                equipementService.synchroniserTablette(id, principal.getId()),
                "Tablette synchronisée."));
    }
}
