package bf.gov.anptic.sonagess.cbc.notifications.service;

import bf.gov.anptic.sonagess.cbc.stocks.domain.BordereauEnlevementBebc;
import bf.gov.anptic.sonagess.cbc.collectes.domain.CollecteBordChamp;
import bf.gov.anptic.sonagess.cbc.notifications.domain.Notification;
import bf.gov.anptic.sonagess.cbc.paiements.domain.OrdrePaiement;
import bf.gov.anptic.sonagess.cbc.referentiels.domain.Producteur;
import bf.gov.anptic.sonagess.cbc.auth.domain.Utilisateur;
import bf.gov.anptic.sonagess.cbc.notifications.domain.CanalNotification;
import bf.gov.anptic.sonagess.cbc.notifications.domain.StatutNotification;
import bf.gov.anptic.sonagess.cbc.common.ResourceNotFoundException;
import bf.gov.anptic.sonagess.cbc.notifications.domain.TypeNotification;
import bf.gov.anptic.sonagess.cbc.notifications.repository.NotificationRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Slf4j
@Service
@RequiredArgsConstructor
public class NotificationService {

    private final NotificationRepository notificationRepository;

    @Transactional
    public Notification envoyerSms(String telephone, String titre, String message,
                                   TypeNotification type, Utilisateur utilisateur,
                                   Producteur producteur, CollecteBordChamp collecte,
                                   OrdrePaiement ordrePaiement, BordereauEnlevementBebc bordereau) {
        Notification notification = construire(CanalNotification.SMS, telephone, titre, message, type,
                utilisateur, producteur, collecte, ordrePaiement, bordereau);
        log.info("[SMS DEMO] destinataire={} | {} | {}", telephone, titre, message);
        notification.setStatut(StatutNotification.ENVOYE);
        notification.setDateEnvoi(LocalDateTime.now());
        notification.setTentativesEnvoi(1);
        notification.setReferenceExterneGateway("SMS-DEMO-" + System.nanoTime());
        return notificationRepository.save(notification);
    }

    @Transactional
    public Notification envoyerInApp(Utilisateur utilisateur, String titre, String message,
                                     TypeNotification type, CollecteBordChamp collecte,
                                     OrdrePaiement ordrePaiement, BordereauEnlevementBebc bordereau) {
        Notification notification = construire(CanalNotification.IN_APP, utilisateur.getEmail(), titre, message,
                type, utilisateur, null, collecte, ordrePaiement, bordereau);
        notification.setStatut(StatutNotification.ENVOYE);
        notification.setDateEnvoi(LocalDateTime.now());
        notification.setTentativesEnvoi(1);
        return notificationRepository.save(notification);
    }

    @Transactional
    public Notification envoyerSmsProducteur(Producteur producteur, String titre, String message,
                                             TypeNotification type, CollecteBordChamp collecte,
                                             OrdrePaiement ordrePaiement) {
        return envoyerSms(producteur.getTelephoneMobileMoney(), titre, message, type,
                null, producteur, collecte, ordrePaiement, null);
    }

    @Transactional(readOnly = true)
    public Page<Notification> listerPourUtilisateur(Long utilisateurId, Pageable pageable) {
        return notificationRepository.findByUtilisateurIdOrderByDateCreationDesc(utilisateurId, pageable);
    }

    @Transactional(readOnly = true)
    public long compterNonLues(Long utilisateurId) {
        return notificationRepository.countByUtilisateurIdAndStatutNot(
                utilisateurId, StatutNotification.LU);
    }

    @Transactional
    public Notification marquerLue(Long notificationId, Long utilisateurId) {
        Notification notification = notificationRepository
                .findByIdAndUtilisateurId(notificationId, utilisateurId)
                .orElseThrow(() -> new ResourceNotFoundException("Notification introuvable."));
        if (notification.getStatut() != StatutNotification.LU) {
            notification.setStatut(StatutNotification.LU);
            notification.setDateLecture(LocalDateTime.now());
            notificationRepository.save(notification);
        }
        return notification;
    }

    private Notification construire(CanalNotification canal, String destinataire, String titre, String message,
                                    TypeNotification type, Utilisateur utilisateur, Producteur producteur,
                                    CollecteBordChamp collecte, OrdrePaiement ordrePaiement,
                                    BordereauEnlevementBebc bordereau) {
        return Notification.builder()
                .canal(canal)
                .typeNotification(type)
                .destinataireContact(destinataire)
                .titre(titre)
                .message(message)
                .statut(StatutNotification.EN_ATTENTE)
                .utilisateur(utilisateur)
                .producteur(producteur)
                .collecte(collecte)
                .ordrePaiement(ordrePaiement)
                .bordereauEnlevement(bordereau)
                .build();
    }
}
