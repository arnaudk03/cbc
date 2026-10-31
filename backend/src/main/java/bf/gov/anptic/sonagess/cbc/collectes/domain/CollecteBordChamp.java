package bf.gov.anptic.sonagess.cbc.collectes.domain;

import bf.gov.anptic.sonagess.cbc.auth.domain.Utilisateur;
import bf.gov.anptic.sonagess.cbc.referentiels.domain.Campagne;
import bf.gov.anptic.sonagess.cbc.referentiels.domain.Comptoir;
import bf.gov.anptic.sonagess.cbc.referentiels.domain.Producteur;
import bf.gov.anptic.sonagess.cbc.referentiels.domain.Speculation;
import jakarta.persistence.CascadeType;
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
@Table(name = "collectes_bord_champ")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CollecteBordChamp {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "reference_lot", nullable = false, unique = true, length = 60)
    private String referenceLot;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "campagne_id", nullable = false)
    private Campagne campagne;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "comptoir_id", nullable = false)
    private Comptoir comptoir;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "producteur_id", nullable = false)
    private Producteur producteur;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "speculation_id", nullable = false)
    private Speculation speculation;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "agent_id", nullable = false)
    private Utilisateur agent;

    @Column(name = "date_heure_collecte", nullable = false)
    private LocalDateTime dateHeureCollecte;

    @Column(name = "quantite_kg", nullable = false)
    private Double quantiteKg;

    @Column(name = "prix_unitaire_kg", nullable = false)
    private Double prixUnitaireKg;

    @Column(name = "montant_net_cfa", nullable = false)
    private Double montantNetCfa;

    @Enumerated(EnumType.STRING)
    @Column(length = 30)
    @Builder.Default
    private CollecteStatut statut = CollecteStatut.SYNCHRONISEE;

    @Column(name = "client_ligne_id", length = 100)
    private String clientLigneId;

    @Column(name = "latitude_gps")
    private Double latitudeGps;

    @Column(name = "longitude_gps")
    private Double longitudeGps;

    @Column(name = "motif_rejet", columnDefinition = "TEXT")
    private String motifRejet;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "examinateur_id")
    private Utilisateur examinateur;

    @Column(name = "date_examen")
    private LocalDateTime dateExamen;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "validateur_id")
    private Utilisateur validateur;

    @Column(name = "date_validation")
    private LocalDateTime dateValidation;

    @CreationTimestamp
    @Column(name = "cree_le", updatable = false)
    private LocalDateTime creeLe;

    @OneToOne(mappedBy = "collecte", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private ControleQualite controleQualite;

    @OneToOne(mappedBy = "collecte", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private FichePeseeFp fichePesee;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "bordereau_id", nullable = false)
    private BordereauReceptionBrbc bordereauReception;

    public void rattacherControleQualite(ControleQualite controle) {
        this.controleQualite = controle;
        controle.setCollecte(this);
    }

    public void rattacherFichePesee(FichePeseeFp fiche) {
        this.fichePesee = fiche;
        fiche.setCollecte(this);
    }

    public void rattacherBordereau(BordereauReceptionBrbc bordereau) {
        this.bordereauReception = bordereau;
        if (bordereau != null && !bordereau.getCollectes().contains(this)) {
            bordereau.getCollectes().add(this);
        }
    }
}
