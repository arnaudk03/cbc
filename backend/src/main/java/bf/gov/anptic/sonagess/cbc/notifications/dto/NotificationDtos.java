package bf.gov.anptic.sonagess.cbc.notifications.dto;

import bf.gov.anptic.sonagess.cbc.notifications.domain.Notification;

import java.time.LocalDateTime;

public final class NotificationDtos {

    private NotificationDtos() {
    }

    public record NotificationResponse(
            Long id,
            String canal,
            String typeNotification,
            String destinataireContact,
            String titre,
            String message,
            String statut,
            Long collecteId,
            Long ordrePaiementId,
            Long bordereauEnlevementId,
            LocalDateTime dateCreation,
            LocalDateTime dateEnvoi,
            LocalDateTime dateLecture
    ) {
        public static NotificationResponse from(Notification notification) {
            return new NotificationResponse(
                    notification.getId(),
                    notification.getCanal().name(),
                    notification.getTypeNotification().name(),
                    notification.getDestinataireContact(),
                    notification.getTitre(),
                    notification.getMessage(),
                    notification.getStatut().name(),
                    notification.getCollecte() != null ? notification.getCollecte().getId() : null,
                    notification.getOrdrePaiement() != null ? notification.getOrdrePaiement().getId() : null,
                    notification.getBordereauEnlevement() != null
                            ? notification.getBordereauEnlevement().getId() : null,
                    notification.getDateCreation(),
                    notification.getDateEnvoi(),
                    notification.getDateLecture()
            );
        }
    }
}
