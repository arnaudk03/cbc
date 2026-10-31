package bf.gov.anptic.sonagess.cbc.stocks.domain;

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

@Entity
@Table(name = "lignes_fiche_stock_materiel")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LigneFicheStockMateriel {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "fiche_id", nullable = false)
    private FicheStockMateriel fiche;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "materiel_id", nullable = false)
    private MaterielEmballage materiel;

    @Column(name = "stock_initial", nullable = false)
    @Builder.Default
    private Integer stockInitial = 0;

    @Column(nullable = false)
    @Builder.Default
    private Integer entrees = 0;

    @Column(nullable = false)
    @Builder.Default
    private Integer sorties = 0;

    @Column(name = "stock_physique")
    private Integer stockPhysique;

    private Integer ecart;

    @Column(name = "stock_final", nullable = false)
    private Integer stockFinal;

    @Column(name = "reference_doc", length = 50)
    private String referenceDoc;

    @Column(length = 255)
    private String observation;
}
