package bf.gov.anptic.sonagess.cbc.collectes.web;

import bf.gov.anptic.sonagess.cbc.collectes.domain.CollecteStatut;
import bf.gov.anptic.sonagess.cbc.collectes.dto.CollecteDtos;
import bf.gov.anptic.sonagess.cbc.collectes.dto.CollecteMobileDtos;
import bf.gov.anptic.sonagess.cbc.collectes.service.CollecteMobileService;
import bf.gov.anptic.sonagess.cbc.common.ApiResponse;
import bf.gov.anptic.sonagess.cbc.common.PageResponse;
import bf.gov.anptic.sonagess.cbc.security.CustomUserDetails;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
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

@RestController
@RequestMapping("/collectes/mobile")
@RequiredArgsConstructor
@PreAuthorize("hasAnyRole('ADMIN', 'AGENT_COLLECTEUR', 'CHEF_COMPTOIR')")
public class CollecteMobileController {

    private final CollecteMobileService collecteMobileService;

    /**
     * Endpoint d'amorçage hors-ligne pour la tablette Flutter.
     * Télécharge la campagne active, le comptoir d'affectation, les spéculations avec leurs barèmes,
     * les prix garantis officiels applicables, la liste des producteurs locaux et le résumé journalier.
     */
    @GetMapping("/bootstrap")
    public ResponseEntity<ApiResponse<CollecteMobileDtos.MobileBootstrapResponse>> bootstrap(
            @AuthenticationPrincipal CustomUserDetails principal) {
        return ResponseEntity.ok(ApiResponse.ok(
                collecteMobileService.bootstrap(principal.getId()),
                "Référentiels hors-ligne chargés pour la tablette mobile."));
    }

    /**
     * Synchronisation par lot (batch sync) des collectes saisies en mode déconnecté sur le terrain.
     * Idempotent : si un clientSyncId a déjà été reçu, il est renvoyé avec statut DEJA_SYNCHRONISE sans doublon.
     */
    @PostMapping("/sync")
    public ResponseEntity<ApiResponse<CollecteMobileDtos.MobileBatchSyncResponse>> synchroniserBatch(
            @Valid @RequestBody CollecteMobileDtos.MobileBatchSyncRequest request,
            @AuthenticationPrincipal CustomUserDetails principal) {
        CollecteMobileDtos.MobileBatchSyncResponse response = collecteMobileService.synchroniserBatch(request, principal.getId());
        String message = String.format("Synchronisation terminée : %d succès, %d échec(s) sur %d bordereau(x).",
                response.totalSucces(), response.totalErreurs(), response.totalRecus());
        return ResponseEntity.ok(ApiResponse.ok(response, message));
    }

    /**
     * Synthèse du jour de l'agent connecté pour l'écran d'accueil Flutter.
     */
    @GetMapping("/resume-journalier")
    public ResponseEntity<ApiResponse<CollecteMobileDtos.ResumeJournalierMobileDto>> resumeJournalier(
            @AuthenticationPrincipal CustomUserDetails principal) {
        return ResponseEntity.ok(ApiResponse.ok(
                collecteMobileService.resumeJournalier(principal.getId()),
                "Synthèse journalière mobile calculée."));
    }

    /**
     * Consultation des dossiers de collecte de l'agent avec filtrage par date et statut de validation.
     */
    @GetMapping("/mes-collectes")
    public ResponseEntity<ApiResponse<PageResponse<CollecteDtos.CollecteResponse>>> mesCollectes(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
            @RequestParam(required = false) CollecteStatut statut,
            @PageableDefault(size = 20, sort = "dateHeureCollecte", direction = Sort.Direction.DESC) Pageable pageable,
            @AuthenticationPrincipal CustomUserDetails principal) {
        return ResponseEntity.ok(ApiResponse.ok(
                collecteMobileService.mesCollectes(principal.getId(), date, statut, pageable)));
    }

    /**
     * Génère la structure de données du ticket de pesée pour impression Bluetooth ESC/POS directe (58mm/80mm).
     */
    @GetMapping("/brbc/{bordereauId}/ticket")
    public ResponseEntity<ApiResponse<CollecteMobileDtos.TicketBrbcDto>> ticket(
            @PathVariable Long bordereauId,
            @AuthenticationPrincipal CustomUserDetails principal) {
        return ResponseEntity.ok(ApiResponse.ok(
                collecteMobileService.genererTicket(bordereauId, principal.getId()),
                "Ticket de pesée généré pour impression thermique mobile."));
    }

    /**
     * Enrôlement direct d'un producteur depuis le terrain (quand une connexion réseau est disponible).
     */
    @PostMapping("/producteurs")
    public ResponseEntity<ApiResponse<CollecteMobileDtos.ProducteurMobileDto>> creerProducteur(
            @Valid @RequestBody CollecteMobileDtos.ProducteurOfflineRequest request,
            @AuthenticationPrincipal CustomUserDetails principal) {
        return ResponseEntity.ok(ApiResponse.ok(
                collecteMobileService.creerProducteurDirect(request, principal.getId()),
                "Producteur enrôlé avec succès depuis le terminal mobile."));
    }
}

