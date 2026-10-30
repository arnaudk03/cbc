package bf.gov.anptic.sonagess.cbc.paiements.web;

import bf.gov.anptic.sonagess.cbc.paiements.service.PaiementService;
import bf.gov.anptic.sonagess.cbc.common.ApiResponse;
import bf.gov.anptic.sonagess.cbc.paiements.domain.OrdrePaiementStatut;
import bf.gov.anptic.sonagess.cbc.security.CustomUserDetails;
import bf.gov.anptic.sonagess.cbc.paiements.dto.PaiementDtos;
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
@RequestMapping("/paiements")
@RequiredArgsConstructor
public class PaiementController {

    private final PaiementService paiementService;

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'SFB_INITIATEUR')")
    public ResponseEntity<ApiResponse<PaiementDtos.PaiementResponse>> initier(
            @Valid @RequestBody PaiementDtos.InitierPaiementRequest request,
            @AuthenticationPrincipal CustomUserDetails principal) {
        return ResponseEntity.ok(ApiResponse.ok(
                paiementService.initier(request, principal.getId()),
                "Ordre de paiement initié."));
    }

    @PostMapping("/{id}/otp")
    @PreAuthorize("hasAnyRole('ADMIN', 'SFB_CHEF')")
    public ResponseEntity<ApiResponse<PaiementDtos.OtpChallengeResponse>> demanderOtp(
            @PathVariable Long id,
            @AuthenticationPrincipal CustomUserDetails principal) {
        return ResponseEntity.ok(ApiResponse.ok(
                paiementService.demanderOtp(id, principal.getId()),
                "Challenge OTP généré."));
    }

    @PostMapping("/{id}/validation")
    @PreAuthorize("hasAnyRole('ADMIN', 'SFB_CHEF')")
    public ResponseEntity<ApiResponse<PaiementDtos.PaiementResponse>> valider(
            @PathVariable Long id,
            @Valid @RequestBody PaiementDtos.ValiderPaiementRequest request,
            @AuthenticationPrincipal CustomUserDetails principal) {
        return ResponseEntity.ok(ApiResponse.ok(
                paiementService.valider(id, request.codeOtp(), principal.getId()),
                "Paiement validé et transmis à l'opérateur."));
    }

    @PostMapping("/{id}/rejet")
    @PreAuthorize("hasAnyRole('ADMIN', 'SFB_CHEF', 'DFC_DIRECTEUR')")
    public ResponseEntity<ApiResponse<PaiementDtos.PaiementResponse>> rejeter(
            @PathVariable Long id,
            @Valid @RequestBody PaiementDtos.RejetPaiementRequest request,
            @AuthenticationPrincipal CustomUserDetails principal) {
        return ResponseEntity.ok(ApiResponse.ok(
                paiementService.rejeter(id, request.motif(), principal.getId()),
                "Paiement rejeté."));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<PaiementDtos.PaiementResponse>>> lister(
            @RequestParam(required = false) OrdrePaiementStatut statut) {
        return ResponseEntity.ok(ApiResponse.ok(paiementService.lister(statut)));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<PaiementDtos.PaiementResponse>> detail(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.ok(paiementService.detail(id)));
    }
}
