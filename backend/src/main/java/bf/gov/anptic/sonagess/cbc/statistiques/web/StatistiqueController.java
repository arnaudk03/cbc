package bf.gov.anptic.sonagess.cbc.statistiques.web;

import bf.gov.anptic.sonagess.cbc.common.ApiResponse;
import bf.gov.anptic.sonagess.cbc.security.CustomUserDetails;
import bf.gov.anptic.sonagess.cbc.statistiques.dto.StatistiqueDtos;
import bf.gov.anptic.sonagess.cbc.statistiques.service.StatistiqueService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;

@RestController
@RequestMapping("/statistiques")
@RequiredArgsConstructor
public class StatistiqueController {

    private final StatistiqueService statistiqueService;

    @GetMapping
    public ResponseEntity<ApiResponse<StatistiqueDtos.StatistiquesResponse>> getStatistiques(
            @RequestParam(required = false) Long campagneId,
            @RequestParam(required = false) Long comptoirId,
            @RequestParam(required = false) String regionId,
            @RequestParam(required = false) Long speculationId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dateDebut,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dateFin,
            @AuthenticationPrincipal CustomUserDetails principal) {

        StatistiqueDtos.FiltreStatistique filtre = new StatistiqueDtos.FiltreStatistique(
                campagneId, comptoirId, regionId, speculationId, dateDebut, dateFin);

        return ResponseEntity.ok(ApiResponse.ok(
                statistiqueService.getStatistiques(filtre, principal.getId())));
    }

    @GetMapping("/export-csv")
    public ResponseEntity<byte[]> exporterCsv(
            @RequestParam(required = false) Long campagneId,
            @RequestParam(required = false) Long comptoirId,
            @RequestParam(required = false) String regionId,
            @RequestParam(required = false) Long speculationId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dateDebut,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dateFin,
            @AuthenticationPrincipal CustomUserDetails principal) {

        StatistiqueDtos.FiltreStatistique filtre = new StatistiqueDtos.FiltreStatistique(
                campagneId, comptoirId, regionId, speculationId, dateDebut, dateFin);

        byte[] csvBytes = statistiqueService.exporterCsv(filtre, principal.getId());

        String filename = "statistiques-sonagess-" + LocalDate.now() + ".csv";

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filename + "\"")
                .contentType(MediaType.parseMediaType("text/csv; charset=UTF-8"))
                .body(csvBytes);
    }
}
