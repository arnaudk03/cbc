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

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "producteurs")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Producteur {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "nom_prenom", nullable = false, length = 150)
    private String nomPrenom;

    @Column(name = "telephone_mobile_money", nullable = false, length = 30)
    private String telephoneMobileMoney;

    @Column(name = "operateur_mobile", nullable = false, length = 30)
    private String operateurMobile;

    @Column(name = "cnib_numero", nullable = false, length = 50)
    private String cnibNumero;

    @Column(name = "cnib_date_delivrance")
    private LocalDate cnibDateDelivrance;

    @Column(name = "cnib_lieu_delivrance", length = 100)
    private String cnibLieuDelivrance;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "locality_id")
    private Locality locality;

    @Column(name = "adresse_precision", columnDefinition = "TEXT")
    private String adressePrecision;

    @CreationTimestamp
    @Column(name = "cree_le", updatable = false)
    private LocalDateTime creeLe;
}
