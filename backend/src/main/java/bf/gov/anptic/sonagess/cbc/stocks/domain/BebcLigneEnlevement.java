package bf.gov.anptic.sonagess.cbc.stocks.domain;

import bf.gov.anptic.sonagess.cbc.referentiels.domain.Speculation;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "bebc_lignes_enlevement",
        uniqueConstraints = @UniqueConstraint(
                name = "uk_bebc_ligne_speculation",
                columnNames = {"bordereau_enlevement_id", "speculation_id"}))
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BebcLigneEnlevement {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "bordereau_enlevement_id", nullable = false)
    private BordereauEnlevementBebc bordereauEnlevement;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "speculation_id", nullable = false)
    private Speculation speculation;

    @Column(name = "nombre_sacs", nullable = false)
    private Integer nombreSacs;

    @Column(name = "colisage_kg", nullable = false)
    private Double colisageKg;

    @Column(name = "quantite_kg", nullable = false)
    private Double quantiteKg;

    @Column(name = "nombre_sacs_recus")
    private Integer nombreSacsRecus;

    @Column(name = "quantite_recue_kg")
    private Double quantiteRecueKg;
}
