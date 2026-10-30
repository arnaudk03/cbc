package bf.gov.anptic.sonagess.cbc.notifications.web;

import bf.gov.anptic.sonagess.cbc.notifications.service.NotificationService;
import bf.gov.anptic.sonagess.cbc.common.ApiResponse;
import bf.gov.anptic.sonagess.cbc.common.PageResponse;
import bf.gov.anptic.sonagess.cbc.security.CustomUserDetails;
import bf.gov.anptic.sonagess.cbc.notifications.dto.NotificationDtos;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/notifications")
@RequiredArgsConstructor
public class NotificationController {

    private final NotificationService notificationService;

    @GetMapping
    public ResponseEntity<ApiResponse<PageResponse<NotificationDtos.NotificationResponse>>> lister(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @AuthenticationPrincipal CustomUserDetails principal) {
        PageResponse<NotificationDtos.NotificationResponse> resultat = PageResponse.from(
                notificationService.listerPourUtilisateur(principal.getId(), PageRequest.of(page, size)),
                NotificationDtos.NotificationResponse::from);
        return ResponseEntity.ok(ApiResponse.ok(resultat));
    }

    @GetMapping("/non-lues")
    public ResponseEntity<ApiResponse<Map<String, Long>>> compterNonLues(
            @AuthenticationPrincipal CustomUserDetails principal) {
        return ResponseEntity.ok(ApiResponse.ok(
                Map.of("total", notificationService.compterNonLues(principal.getId()))));
    }

    @PatchMapping("/{id}/lecture")
    public ResponseEntity<ApiResponse<NotificationDtos.NotificationResponse>> marquerLue(
            @PathVariable Long id,
            @AuthenticationPrincipal CustomUserDetails principal) {
        return ResponseEntity.ok(ApiResponse.ok(
                NotificationDtos.NotificationResponse.from(
                        notificationService.marquerLue(id, principal.getId())),
                "Notification marquée comme lue."));
    }
}
