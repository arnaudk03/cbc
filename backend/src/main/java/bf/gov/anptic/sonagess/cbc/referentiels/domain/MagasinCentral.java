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
@Table(name = "magasins_centraux")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MagasinCentral {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "code_magasin", nullable = false, unique = true, length = 50)
    private String codeMagasin;

    @Column(nullable = false, length = 150)
    private String nom;

    @Column(nullable = false, length = 100)
    private String ville;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "locality_id")
    private Locality locality;

    @Column(name = "capacite_totale_tonnes", nullable = false)
    private Double capaciteTotaleTonnes;

    @Column(name = "stock_actuel_tonnes")
    @Builder.Default
    private Double stockActuelTonnes = 0.0;

    @Builder.Default
    private Boolean actif = true;

    @CreationTimestamp
    @Column(name = "cree_le", updatable = false)
    private LocalDateTime creeLe;
}
