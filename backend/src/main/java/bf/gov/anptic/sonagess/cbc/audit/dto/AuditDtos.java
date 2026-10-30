package bf.gov.anptic.sonagess.cbc.audit.dto;

import bf.gov.anptic.sonagess.cbc.audit.domain.JournalAudit;

public final class AuditDtos {

    private AuditDtos() {
    }

    public record AuditResponse(
            Long id,
            String matricule,
            String action,
            String entite,
            Long entiteId,
            String resultat,
            String adresseIp,
            String details,
            java.time.LocalDateTime dateAction
    ) {
        public static AuditResponse from(JournalAudit audit) {
            return new AuditResponse(audit.getId(), audit.getMatriculeUtilisateur(), audit.getAction(),
                    audit.getEntiteConcernee(), audit.getEntiteId(), audit.getResultat(),
                    audit.getAdresseIp(), audit.getDetails(), audit.getDateAction());
        }
    }
}
