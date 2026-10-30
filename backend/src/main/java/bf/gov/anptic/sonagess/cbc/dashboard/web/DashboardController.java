package bf.gov.anptic.sonagess.cbc.dashboard.web;

import bf.gov.anptic.sonagess.cbc.dashboard.service.DashboardService;
import bf.gov.anptic.sonagess.cbc.common.ApiResponse;
import bf.gov.anptic.sonagess.cbc.dashboard.dto.DashboardDtos;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/dashboard")
@RequiredArgsConstructor
public class DashboardController {

    private final DashboardService dashboardService;

    @GetMapping
    public ResponseEntity<ApiResponse<DashboardDtos.DashboardResponse>> tableauDeBord(
            @RequestParam(required = false) Long campagneId) {
        return ResponseEntity.ok(ApiResponse.ok(dashboardService.tableauDeBord(campagneId)));
    }
}
