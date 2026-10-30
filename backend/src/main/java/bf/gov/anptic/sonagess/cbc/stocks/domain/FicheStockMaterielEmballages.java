package bf.gov.anptic.sonagess.cbc.stocks.domain;

import bf.gov.anptic.sonagess.cbc.referentiels.domain.Comptoir;
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
@Table(name = "fiches_stock_materiel_emballages")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FicheStockMaterielEmballages {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "comptoir_id", nullable = false)
    private Comptoir comptoir;

    @Column(name = "designation_materiel", nullable = false, length = 100)
    private String designationMateriel;

    @Column(name = "reference_doc", length = 50)
    private String referenceDoc;

    @Column(name = "entrees_emballages")
    @Builder.Default
    private Integer entreesEmballages = 0;

    @Column(name = "sorties_emballages")
    @Builder.Default
    private Integer sortiesEmballages = 0;

    @Column(name = "stock_emballages", nullable = false)
    private Integer stockEmballages;

    @CreationTimestamp
    @Column(name = "cree_le", updatable = false)
    private LocalDateTime creeLe;
}
