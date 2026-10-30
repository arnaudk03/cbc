package bf.gov.anptic.sonagess.cbc.collectes.web;

import bf.gov.anptic.sonagess.cbc.collectes.service.CollecteService;
import bf.gov.anptic.sonagess.cbc.common.ApiResponse;
import bf.gov.anptic.sonagess.cbc.common.PageResponse;
import bf.gov.anptic.sonagess.cbc.collectes.domain.CollecteStatut;
import bf.gov.anptic.sonagess.cbc.security.CustomUserDetails;
import bf.gov.anptic.sonagess.cbc.collectes.dto.CollecteDtos;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/collectes")
@RequiredArgsConstructor
public class CollecteController {

    private final CollecteService collecteService;

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'AGENT_COLLECTEUR', 'CHEF_COMPTOIR')")
    public ResponseEntity<ApiResponse<CollecteDtos.CollecteResponse>> enregistrer(
            @Valid @RequestBody CollecteDtos.CreateCollecteRequest request,
            @AuthenticationPrincipal CustomUserDetails principal) {
        return ResponseEntity.ok(ApiResponse.ok(
                collecteService.enregistrer(request, principal.getId()),
                "Collecte enregistrée et synchronisée."));
    }

    @PostMapping("/lot")
    @PreAuthorize("hasAnyRole('ADMIN', 'AGENT_COLLECTEUR', 'CHEF_COMPTOIR')")
    public ResponseEntity<ApiResponse<CollecteDtos.CollecteLotResponse>> enregistrerLot(
            @Valid @RequestBody CollecteDtos.CreateCollecteLotRequest request,
            @AuthenticationPrincipal CustomUserDetails principal) {
        return ResponseEntity.ok(ApiResponse.ok(
                collecteService.enregistrerLot(request, principal.getId()),
                "BRBC multi-spéculations enregistré et synchronisé."));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<PageResponse<CollecteDtos.CollecteResponse>>> lister(
            @RequestParam(required = false) CollecteStatut statut,
            @RequestParam(required = false) Long campagneId,
            @RequestParam(required = false) Long comptoirId,
            @RequestParam(required = false) Long speculationId,
            @PageableDefault(size = 20, sort = "dateHeureCollecte", direction = Sort.Direction.DESC)
            Pageable pageable,
            @AuthenticationPrincipal CustomUserDetails principal) {
        return ResponseEntity.ok(ApiResponse.ok(
                collecteService.lister(statut, campagneId, comptoirId, speculationId, pageable,
                        principal.getId())));
    }

    @GetMapping("/brbc/{bordereauId}")
    public ResponseEntity<ApiResponse<List<CollecteDtos.CollecteResponse>>> listerParBordereau(
            @PathVariable Long bordereauId,
            @AuthenticationPrincipal CustomUserDetails principal) {
        return ResponseEntity.ok(ApiResponse.ok(
                collecteService.listerParBordereau(bordereauId, principal.getId())));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<CollecteDtos.CollecteResponse>> detail(
            @PathVariable Long id,
            @AuthenticationPrincipal CustomUserDetails principal) {
        return ResponseEntity.ok(ApiResponse.ok(collecteService.detail(id, principal.getId())));
    }

    @PostMapping("/{id}/examen")
    @PreAuthorize("hasAnyRole('ADMIN', 'SCP')")
    public ResponseEntity<ApiResponse<CollecteDtos.CollecteResponse>> examiner(
            @PathVariable Long id,
            @Valid @RequestBody CollecteDtos.ExamenRequest request,
            @AuthenticationPrincipal CustomUserDetails principal) {
        return ResponseEntity.ok(ApiResponse.ok(
                collecteService.examiner(id, request, principal.getId()),
                request.approuve() ? "Dossier transmis à la DCT." : "Dossier rejeté."));
    }

    @PostMapping("/{id}/validation")
    @PreAuthorize("hasAnyRole('ADMIN', 'DCT')")
    public ResponseEntity<ApiResponse<CollecteDtos.CollecteResponse>> valider(
            @PathVariable Long id,
            @Valid @RequestBody CollecteDtos.ValidationRequest request,
            @AuthenticationPrincipal CustomUserDetails principal) {
        return ResponseEntity.ok(ApiResponse.ok(
                collecteService.valider(id, request, principal.getId()),
                request.approuve() ? "BRBC validé et transmis à la DFC." : "Dossier rejeté."));
    }
}
