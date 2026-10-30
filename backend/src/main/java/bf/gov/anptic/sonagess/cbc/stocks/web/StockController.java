package bf.gov.anptic.sonagess.cbc.stocks.web;

import bf.gov.anptic.sonagess.cbc.stocks.service.StockService;
import bf.gov.anptic.sonagess.cbc.common.ApiResponse;
import bf.gov.anptic.sonagess.cbc.security.CustomUserDetails;
import bf.gov.anptic.sonagess.cbc.stocks.dto.StockDtos;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
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

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/stocks")
@RequiredArgsConstructor
public class StockController {

    private final StockService stockService;

    @PostMapping("/fsj")
    @PreAuthorize("hasAnyRole('ADMIN', 'CHEF_COMPTOIR', 'AGENT_COLLECTEUR')")
    public ResponseEntity<ApiResponse<StockDtos.FsjResponse>> enregistrerFsj(
            @Valid @RequestBody StockDtos.FsjRequest request,
            @AuthenticationPrincipal CustomUserDetails principal) {
        return ResponseEntity.ok(ApiResponse.ok(
                stockService.enregistrerFsj(request, principal.getId()),
                "Fiche de stock journalière enregistrée."));
    }

    @GetMapping("/fsj")
    public ResponseEntity<ApiResponse<List<StockDtos.FsjResponse>>> listerFsj(
            @RequestParam Long comptoirId,
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        return ResponseEntity.ok(ApiResponse.ok(stockService.listerFsj(comptoirId, date)));
    }

    @GetMapping("/comptoirs/{comptoirId}/disponible")
    public ResponseEntity<ApiResponse<StockDtos.StockDisponibleResponse>> stockDisponible(
            @PathVariable Long comptoirId) {
        return ResponseEntity.ok(ApiResponse.ok(stockService.stockDisponible(comptoirId)));
    }
}
