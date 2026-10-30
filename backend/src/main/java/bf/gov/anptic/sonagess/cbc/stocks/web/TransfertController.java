package bf.gov.anptic.sonagess.cbc.stocks.web;

import bf.gov.anptic.sonagess.cbc.stocks.service.StockService;
import bf.gov.anptic.sonagess.cbc.common.ApiResponse;
import bf.gov.anptic.sonagess.cbc.stocks.domain.StatutBebc;
import bf.gov.anptic.sonagess.cbc.security.CustomUserDetails;
import bf.gov.anptic.sonagess.cbc.stocks.dto.StockDtos;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
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
@RequestMapping("/transferts")
@RequiredArgsConstructor
public class TransfertController {

    private final StockService stockService;

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'DCT', 'DGSA', 'SGS', 'DPL', 'CHEF_COMPTOIR', 'AGENT_COLLECTEUR')")
    public ResponseEntity<ApiResponse<StockDtos.TransfertResponse>> creer(
            @Valid @RequestBody StockDtos.TransfertRequest request,
            @AuthenticationPrincipal CustomUserDetails principal) {
        return ResponseEntity.ok(ApiResponse.ok(
                stockService.creerTransfert(request, principal.getId()),
                "Bordereau d'enlèvement créé."));
    }

    @PostMapping("/{id}/reception")
    @PreAuthorize("hasAnyRole('ADMIN', 'MAGASINIER', 'SGS')")
    public ResponseEntity<ApiResponse<StockDtos.TransfertResponse>> receptionner(
            @PathVariable Long id,
            @Valid @RequestBody StockDtos.ReceptionRequest request,
            @AuthenticationPrincipal CustomUserDetails principal) {
        return ResponseEntity.ok(ApiResponse.ok(
                stockService.receptionner(id, request, principal.getId()),
                "Réception en magasin enregistrée."));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<StockDtos.TransfertResponse>>> lister(
            @RequestParam(required = false) StatutBebc statut) {
        return ResponseEntity.ok(ApiResponse.ok(stockService.listerTransferts(statut)));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<StockDtos.TransfertResponse>> detail(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.ok(stockService.detailTransfert(id)));
    }
}
