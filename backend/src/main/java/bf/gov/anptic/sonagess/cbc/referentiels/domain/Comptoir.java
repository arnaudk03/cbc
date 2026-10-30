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
@Table(name = "comptoirs")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Comptoir {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 50)
    private String code;

    @Column(nullable = false, length = 150)
    private String nom;

    @Column(length = 50)
    @Builder.Default
    private String type = "FIXE";

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "locality_id", nullable = false)
    private Locality locality;

    @Column(name = "latitude_gps")
    private Double latitudeGps;

    @Column(name = "longitude_gps")
    private Double longitudeGps;

    @Column(name = "capacite_stockage_kg")
    @Builder.Default
    private Double capaciteStockageKg = 50000.0;

    @Builder.Default
    private Boolean actif = true;

    @CreationTimestamp
    @Column(name = "cree_le", updatable = false)
    private LocalDateTime creeLe;
}
