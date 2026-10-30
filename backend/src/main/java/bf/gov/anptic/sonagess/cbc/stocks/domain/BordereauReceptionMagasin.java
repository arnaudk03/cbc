package bf.gov.anptic.sonagess.cbc.stocks.domain;

import bf.gov.anptic.sonagess.cbc.auth.domain.Utilisateur;
import bf.gov.anptic.sonagess.cbc.referentiels.domain.MagasinCentral;
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
@Table(name = "bordereaux_reception_magasin")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BordereauReceptionMagasin {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "bordereau_enlevement_id", nullable = false, unique = true)
    private BordereauEnlevementBebc bordereauEnlevement;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "magasin_central_id", nullable = false)
    private MagasinCentral magasinCentral;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "magasinier_id", nullable = false)
    private Utilisateur magasinier;

    @Column(name = "numero_reception", nullable = false, unique = true, length = 50)
    private String numeroReception;

    @Column(name = "date_dechargement")
    private LocalDateTime dateDechargement;

    @Column(name = "nombre_sacs_recus", nullable = false)
    private Integer nombreSacsRecus;

    @Column(name = "tonnage_decharge_kg", nullable = false)
    private Double tonnageDechargeKg;

    @Column(name = "ecart_poids_kg")
    @Builder.Default
    private Double ecartPoidsKg = 0.0;

    @Enumerated(EnumType.STRING)
    @Column(name = "statut_conformite", length = 30)
    @Builder.Default
    private StatutConformiteReception statutConformite = StatutConformiteReception.CONFORME;

    @CreationTimestamp
    @Column(name = "cree_le", updatable = false)
    private LocalDateTime creeLe;
}
