package bf.gov.anptic.sonagess.cbc.paiements.domain;

import bf.gov.anptic.sonagess.cbc.auth.domain.Utilisateur;
import bf.gov.anptic.sonagess.cbc.collectes.domain.BordereauReceptionBrbc;
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
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "ordres_paiement")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrdrePaiement {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "bordereau_reception_id", nullable = false, unique = true)
    private BordereauReceptionBrbc bordereauReception;

    @Column(name = "reference_transaction", nullable = false, unique = true, length = 100)
    private String referenceTransaction;

    @Column(name = "montant_paye_cfa", nullable = false)
    private Double montantPayeCfa;

    @Column(nullable = false, length = 30)
    private String operateur;

    @Column(name = "telephone_destination", nullable = false, length = 30)
    private String telephoneDestination;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "initiateur_id", nullable = false)
    private Utilisateur initiateur;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "validateur_2fa_id")
    private Utilisateur validateur2FA;

    @Column(name = "date_initiation")
    private LocalDateTime dateInitiation;

    @Column(name = "date_validation_2fa")
    private LocalDateTime dateValidation2FA;

    @Column(name = "date_confirmation")
    private LocalDateTime dateConfirmation;

    @Enumerated(EnumType.STRING)
    @Column(length = 30)
    @Builder.Default
    private OrdrePaiementStatut statut = OrdrePaiementStatut.INITIE;

    @Column(name = "reference_operateur", length = 100)
    private String referenceOperateur;

    @Column(name = "message_retour_operateur", columnDefinition = "TEXT")
    private String messageRetourOperateur;

    @Column(name = "motif_rejet", columnDefinition = "TEXT")
    private String motifRejet;

    @CreationTimestamp
    @Column(name = "cree_le", updatable = false)
    private LocalDateTime creeLe;
}
