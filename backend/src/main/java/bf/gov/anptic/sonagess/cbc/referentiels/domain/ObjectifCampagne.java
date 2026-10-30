package bf.gov.anptic.sonagess.cbc.referentiels.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
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
@Table(name = "objectifs_campagne")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ObjectifCampagne {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "campagne_id", nullable = false)
    private Campagne campagne;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "speculation_id", nullable = false)
    private Speculation speculation;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "locality_id")
    private Locality locality;

    @Column(name = "objectif_tonnes", nullable = false)
    private Double objectifTonnes;

    @Column(name = "budget_previsionnel_cfa")
    @Builder.Default
    private Double budgetPrevisionnelCfa = 0.0;

    @Column(name = "seuil_alerte_pct")
    @Builder.Default
    private Double seuilAlertePct = 80.0;

    @CreationTimestamp
    @Column(name = "cree_le", updatable = false)
    private LocalDateTime creeLe;
}
