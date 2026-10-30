package bf.gov.anptic.sonagess.cbc.audit.service;

import bf.gov.anptic.sonagess.cbc.audit.domain.JournalAudit;
import bf.gov.anptic.sonagess.cbc.auth.domain.Utilisateur;
import bf.gov.anptic.sonagess.cbc.audit.repository.JournalAuditRepository;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuditService {

    private final JournalAuditRepository journalAuditRepository;

    @Transactional(readOnly = true)
    public Page<JournalAudit> lister(Pageable pageable) {
        return journalAuditRepository.findAllByOrderByDateActionDesc(pageable);
    }

    public void succes(Utilisateur utilisateur, String action, String entite, Long entiteId, String details) {
        journaliser(utilisateur, action, entite, entiteId, "SUCCES", details);
    }

    public void echec(Utilisateur utilisateur, String action, String entite, Long entiteId, String details) {
        journaliser(utilisateur, action, entite, entiteId, "ECHEC", details);
    }

    public void journaliser(Utilisateur utilisateur, String action, String entite, Long entiteId,
                            String resultat, String details) {
        String adresseIp = null;
        String userAgent = null;
        ServletRequestAttributes attributes =
                (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        if (attributes != null) {
            HttpServletRequest request = attributes.getRequest();
            adresseIp = extraireAdresseIp(request);
            userAgent = request.getHeader("User-Agent");
        }

        JournalAudit audit = JournalAudit.builder()
                .utilisateur(utilisateur)
                .matriculeUtilisateur(utilisateur != null ? utilisateur.getMatricule() : null)
                .action(action)
                .entiteConcernee(entite)
                .entiteId(entiteId)
                .adresseIp(adresseIp)
                .userAgent(userAgent)
                .resultat(resultat)
                .details(details)
                .build();

        try {
            journalAuditRepository.save(audit);
        } catch (Exception ex) {
            log.error("Impossible d'enregistrer le journal d'audit pour l'action {}", action, ex);
        }
    }

    private String extraireAdresseIp(HttpServletRequest request) {
        String forwarded = request.getHeader("X-Forwarded-For");
        if (forwarded != null && !forwarded.isBlank()) {
            return forwarded.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }
}
