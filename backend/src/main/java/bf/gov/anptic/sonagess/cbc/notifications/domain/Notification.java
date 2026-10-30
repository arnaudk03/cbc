package bf.gov.anptic.sonagess.cbc.notifications.domain;

import bf.gov.anptic.sonagess.cbc.auth.domain.Utilisateur;
import bf.gov.anptic.sonagess.cbc.collectes.domain.CollecteBordChamp;
import bf.gov.anptic.sonagess.cbc.paiements.domain.OrdrePaiement;
import bf.gov.anptic.sonagess.cbc.referentiels.domain.Producteur;
import bf.gov.anptic.sonagess.cbc.stocks.domain.BordereauEnlevementBebc;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "notifications")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Notification {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private CanalNotification canal;

    @Enumerated(EnumType.STRING)
    @Column(name = "type_notification", nullable = false, length = 50)
    private TypeNotification typeNotification;

    @Column(name = "destinataire_contact", nullable = false, length = 150)
    private String destinataireContact;

    @Column(nullable = false, length = 150)
    private String titre;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String message;

    @Enumerated(EnumType.STRING)
    @Column(length = 20)
    @Builder.Default
    private StatutNotification statut = StatutNotification.EN_ATTENTE;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "utilisateur_id")
    private Utilisateur utilisateur;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "producteur_id")
    private Producteur producteur;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "collecte_id")
    private CollecteBordChamp collecte;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ordre_paiement_id")
    private OrdrePaiement ordrePaiement;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "bordereau_enlevement_id")
    private BordereauEnlevementBebc bordereauEnlevement;

    @Column(name = "reference_externe_gateway", length = 150)
    private String referenceExterneGateway;

    @Column(name = "tentatives_envoi")
    @Builder.Default
    private Integer tentativesEnvoi = 0;

    @Column(name = "erreur_message", columnDefinition = "TEXT")
    private String erreurMessage;

    @CreationTimestamp
    @Column(name = "date_creation", updatable = false)
    private LocalDateTime dateCreation;

    @Column(name = "date_envoi")
    private LocalDateTime dateEnvoi;

    @Column(name = "date_lecture")
    private LocalDateTime dateLecture;
}
