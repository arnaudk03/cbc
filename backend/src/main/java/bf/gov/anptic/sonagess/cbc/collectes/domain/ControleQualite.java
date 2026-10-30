package bf.gov.anptic.sonagess.cbc.collectes.domain;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
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
@Table(name = "controles_qualite")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ControleQualite {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @JsonIgnore
    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "collecte_id", nullable = false, unique = true)
    private CollecteBordChamp collecte;

    @Column(name = "taux_humidite", nullable = false)
    private Double tauxHumidite;

    @Column(name = "taux_impuretes", nullable = false)
    private Double tauxImpuretes;

    @Column(name = "grains_avaries")
    @Builder.Default
    private Double grainsAvaries = 0.0;

    @Column(nullable = false)
    private Boolean conforme;

    @Column(name = "decision_agent", nullable = false, length = 30)
    private String decisionAgent;

    @Column(name = "motif_rejet", columnDefinition = "TEXT")
    private String motifRejet;

    @CreationTimestamp
    @Column(name = "cree_le", updatable = false)
    private LocalDateTime creeLe;
}
