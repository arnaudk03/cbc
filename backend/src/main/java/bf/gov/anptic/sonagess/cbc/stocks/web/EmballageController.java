package bf.gov.anptic.sonagess.cbc.stocks.web;

import bf.gov.anptic.sonagess.cbc.common.ApiResponse;
import bf.gov.anptic.sonagess.cbc.security.CustomUserDetails;
import bf.gov.anptic.sonagess.cbc.stocks.dto.EmballageDtos;
import bf.gov.anptic.sonagess.cbc.stocks.service.EmballageService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/stocks/emballages")
@RequiredArgsConstructor
public class EmballageController {

    private final EmballageService emballageService;

    @GetMapping("/materiels")
    @PreAuthorize("hasAnyRole('ADMIN', 'DCT', 'DGSA', 'SGS', 'DPL', 'CHEF_COMPTOIR', "
            + "'AGENT_COLLECTEUR', 'CONSULTATION')")
    public ResponseEntity<ApiResponse<List<EmballageDtos.MaterielResponse>>> listerMateriels(
            @RequestParam(defaultValue = "true") boolean actifsSeulement) {
        return ResponseEntity.ok(ApiResponse.ok(
                emballageService.listerMateriels(actifsSeulement)));
    }

    @PostMapping("/materiels")
    @PreAuthorize("hasAnyRole('ADMIN', 'DCT')")
    public ResponseEntity<ApiResponse<EmballageDtos.MaterielResponse>> creerMateriel(
            @Valid @RequestBody EmballageDtos.MaterielRequest request,
            @AuthenticationPrincipal CustomUserDetails principal) {
        return ResponseEntity.ok(ApiResponse.ok(
                emballageService.creerMateriel(request, principal.getId()),
                "Matériel d'emballage créé."));
    }

    @PutMapping("/materiels/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'DCT')")
    public ResponseEntity<ApiResponse<EmballageDtos.MaterielResponse>> modifierMateriel(
            @PathVariable Long id,
            @Valid @RequestBody EmballageDtos.MaterielRequest request,
            @AuthenticationPrincipal CustomUserDetails principal) {
        return ResponseEntity.ok(ApiResponse.ok(
                emballageService.modifierMateriel(id, request, principal.getId()),
                "Matériel d'emballage modifié."));
    }

    @GetMapping("/stock")
    @PreAuthorize("hasAnyRole('ADMIN', 'DCT', 'DGSA', 'SGS', 'DPL', 'CHEF_COMPTOIR', "
            + "'AGENT_COLLECTEUR', 'CONSULTATION')")
    public ResponseEntity<ApiResponse<List<EmballageDtos.StockMaterielResponse>>> stockCourant(
            @RequestParam Long comptoirId) {
        return ResponseEntity.ok(ApiResponse.ok(emballageService.stockCourant(comptoirId)));
    }

    @GetMapping("/fiches")
    @PreAuthorize("hasAnyRole('ADMIN', 'DCT', 'DGSA', 'SGS', 'DPL', 'CHEF_COMPTOIR', "
            + "'AGENT_COLLECTEUR', 'CONSULTATION')")
    public ResponseEntity<ApiResponse<List<EmballageDtos.FicheResponse>>> listerFiches(
            @RequestParam Long comptoirId,
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        return ResponseEntity.ok(ApiResponse.ok(
                emballageService.listerFiches(comptoirId, date)));
    }

    @GetMapping("/fiches/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'DCT', 'DGSA', 'SGS', 'DPL', 'CHEF_COMPTOIR', "
            + "'AGENT_COLLECTEUR', 'CONSULTATION')")
    public ResponseEntity<ApiResponse<EmballageDtos.FicheResponse>> detailFiche(
            @PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.ok(emballageService.detailFiche(id)));
    }

    @PostMapping("/fiches")
    @PreAuthorize("hasAnyRole('ADMIN', 'DCT', 'CHEF_COMPTOIR')")
    public ResponseEntity<ApiResponse<EmballageDtos.FicheResponse>> creerFiche(
            @Valid @RequestBody EmballageDtos.FicheRequest request,
            @AuthenticationPrincipal CustomUserDetails principal) {
        return ResponseEntity.ok(ApiResponse.ok(
                emballageService.creerFiche(request, principal.getId()),
                "Fiche de stock du matériel d'emballage enregistrée."));
    }

    @PostMapping("/fiches/{id}/validation")
    @PreAuthorize("hasAnyRole('ADMIN', 'DCT')")
    public ResponseEntity<ApiResponse<EmballageDtos.FicheResponse>> validerFiche(
            @PathVariable Long id,
            @AuthenticationPrincipal CustomUserDetails principal) {
        return ResponseEntity.ok(ApiResponse.ok(
                emballageService.validerFiche(id, principal.getId()),
                "Fiche de stock validée."));
    }
}
