package bf.gov.anptic.sonagess.cbc.audit.web;

import bf.gov.anptic.sonagess.cbc.audit.service.AuditService;
import bf.gov.anptic.sonagess.cbc.common.ApiResponse;
import bf.gov.anptic.sonagess.cbc.common.PageResponse;
import bf.gov.anptic.sonagess.cbc.audit.dto.AuditDtos;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/audit")
@RequiredArgsConstructor
@PreAuthorize("hasAnyRole('ADMIN', 'DCT', 'CONSULTATION')")
public class AuditController {

    private final AuditService auditService;

    @GetMapping
    public ResponseEntity<ApiResponse<PageResponse<AuditDtos.AuditResponse>>> lister(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "50") int size) {
        return ResponseEntity.ok(ApiResponse.ok(PageResponse.from(
                auditService.lister(PageRequest.of(page, size)),
                AuditDtos.AuditResponse::from)));
    }
}
