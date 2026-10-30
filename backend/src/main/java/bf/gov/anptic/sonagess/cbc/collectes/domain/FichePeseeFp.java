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
@Table(name = "fiches_pesee_fp")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FichePeseeFp {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @JsonIgnore
    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "collecte_id", nullable = false, unique = true)
    private CollecteBordChamp collecte;

    @Column(name = "numero_fiche", nullable = false, unique = true, length = 50)
    private String numeroFiche;

    @Column(name = "direction_regionale", length = 100)
    private String directionRegionale;

    @Column(name = "direction_provinciale", length = 100)
    private String directionProvinciale;

    @Column(name = "site_comptoir", length = 100)
    private String siteComptoir;

    @Column(name = "nombre_sacs", nullable = false)
    private Integer nombreSacs;

    @Column(name = "poids_brut_total_kg", nullable = false)
    private Double poidsBrutTotalKg;

    @Column(name = "tare_emballage_kg", nullable = false)
    private Double tareEmballageKg;

    @Column(name = "poids_net_total_kg", nullable = false)
    private Double poidsNetTotalKg;

    @CreationTimestamp
    @Column(name = "cree_le", updatable = false)
    private LocalDateTime creeLe;
}
