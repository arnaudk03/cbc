package bf.gov.anptic.sonagess.cbc.referentiels.web;

import bf.gov.anptic.sonagess.cbc.referentiels.service.ReferentielService;
import bf.gov.anptic.sonagess.cbc.common.ApiResponse;
import bf.gov.anptic.sonagess.cbc.referentiels.domain.LocalityType;
import bf.gov.anptic.sonagess.cbc.referentiels.dto.ReferentielDtos;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/localites")
@RequiredArgsConstructor
public class LocalityController {

    private final ReferentielService referentielService;

    @GetMapping
    public ResponseEntity<ApiResponse<List<ReferentielDtos.LocalityResponse>>> lister(
            @RequestParam(required = false) LocalityType type,
            @RequestParam(required = false) UUID parentId) {
        return ResponseEntity.ok(ApiResponse.ok(
                referentielService.listerLocalites(type, parentId)));
    }
}
