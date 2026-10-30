package bf.gov.anptic.sonagess.cbc.stocks.domain;

import bf.gov.anptic.sonagess.cbc.referentiels.domain.Comptoir;
import bf.gov.anptic.sonagess.cbc.referentiels.domain.MagasinCentral;
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
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "bordereaux_enlevement_bebc")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BordereauEnlevementBebc {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "numero_bebc", nullable = false, unique = true, length = 50)
    private String numeroBebc;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "comptoir_depart_id", nullable = false)
    private Comptoir comptoirDepart;

    @Column(length = 100)
    private String direction;

    @Column(length = 100)
    private String localite;

    @Column(name = "magasin_depart", length = 100)
    private String magasinDepart;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "magasin_central_destination_id")
    private MagasinCentral magasinDestination;

    @Column(length = 150)
    private String beneficiaire;

    @Column(name = "objet_enlevement", length = 150)
    private String objetEnlevement;

    @Column(length = 100)
    private String colisage;

    @Column(name = "nombre_sacs", nullable = false)
    private Integer nombreSacs;

    @Column(name = "tonnage_enleve_kg", nullable = false)
    private Double tonnageEnleveKg;

    @OneToMany(mappedBy = "bordereauEnlevement", cascade = CascadeType.ALL,
            orphanRemoval = true, fetch = FetchType.LAZY)
    @Builder.Default
    private List<BebcLigneEnlevement> lignes = new ArrayList<>();

    public void ajouterLigne(BebcLigneEnlevement ligne) {
        ligne.setBordereauEnlevement(this);
        this.lignes.add(ligne);
    }

    @Column(name = "nom_transporteur", nullable = false, length = 150)
    private String nomTransporteur;

    @Column(name = "numero_vehicule", nullable = false, length = 50)
    private String numeroVehicule;

    @Column(name = "signature_transporteur_token")
    private String signatureTransporteurToken;

    @Enumerated(EnumType.STRING)
    @Column(length = 30)
    @Builder.Default
    private StatutBebc statut = StatutBebc.EN_TRANSIT;

    @Column(name = "date_enlevement")
    private LocalDateTime dateEnlevement;

    @Column(name = "date_arrivee_prevue")
    private LocalDate dateArriveePrevue;

    @CreationTimestamp
    @Column(name = "cree_le", updatable = false)
    private LocalDateTime creeLe;
}
