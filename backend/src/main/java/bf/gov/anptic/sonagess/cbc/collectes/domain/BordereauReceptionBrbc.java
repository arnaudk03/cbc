package bf.gov.anptic.sonagess.cbc.collectes.domain;

import bf.gov.anptic.sonagess.cbc.paiements.domain.ModePaiement;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OrderBy;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "bordereaux_reception_brbc")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BordereauReceptionBrbc {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToMany(mappedBy = "bordereauReception", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("id ASC")
    @Builder.Default
    private List<CollecteBordChamp> collectes = new ArrayList<>();

    @Column(name = "numero_brbc", nullable = false, unique = true, length = 50)
    private String numeroBrbc;

    @Column(name = "date_emission", nullable = false)
    private LocalDateTime dateEmission;

    @Column(name = "quantite_kg", nullable = false)
    private Double quantiteKg;

    @Column(name = "prix_unitaire_applique", nullable = false)
    private Double prixUnitaireApplique;

    @Column(name = "montant_net_payer_cfa", nullable = false)
    private Double montantNetPayerCfa;

    @Column(name = "montant_en_lettres", nullable = false, columnDefinition = "TEXT")
    private String montantEnLettres;

    @Enumerated(EnumType.STRING)
    @Column(name = "mode_paiement", length = 30)
    @Builder.Default
    private ModePaiement modePaiement = ModePaiement.MOBILE_MONEY;

    @Column(name = "observations_receptionnaire", columnDefinition = "TEXT")
    private String observationsReceptionnaire;

    @Column(name = "signature_producteur_token")
    private String signatureProducteurToken;

    @Column(name = "signature_agent_token")
    private String signatureAgentToken;

    @Enumerated(EnumType.STRING)
    @Column(name = "statut_validation", length = 30)
    @Builder.Default
    private StatutValidationBrbc statutValidation = StatutValidationBrbc.EN_ATTENTE_EXAMEN;

    @CreationTimestamp
    @Column(name = "cree_le", updatable = false)
    private LocalDateTime creeLe;

    public CollecteBordChamp getCollectePrincipale() {
        return collectes == null || collectes.isEmpty() ? null : collectes.get(0);
    }
}
