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

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "fiches_stock_journalieres_fsj")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FicheStockJournaliereFsj {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "comptoir_id", nullable = false)
    private Comptoir comptoir;

    @Column(name = "date_fiche", nullable = false)
    private LocalDate dateFiche;

    @Column(name = "nature_operation", nullable = false, length = 50)
    private String natureOperation;

    @Column(name = "entree_sacs")
    @Builder.Default
    private Integer entreeSacs = 0;

    @Column(name = "entree_poids_kg")
    @Builder.Default
    private Double entreePoidsKg = 0.0;

    @Column(name = "entree_valeur_cfa")
    @Builder.Default
    private Double entreeValeurCfa = 0.0;

    @Column(name = "sortie_sacs")
    @Builder.Default
    private Integer sortieSacs = 0;

    @Column(name = "sortie_poids_kg")
    @Builder.Default
    private Double sortiePoidsKg = 0.0;

    @Column(name = "sortie_valeur_cfa")
    @Builder.Default
    private Double sortieValeurCfa = 0.0;

    @Column(name = "stock_final_sacs", nullable = false)
    private Integer stockFinalSacs;

    @Column(name = "stock_final_poids_kg", nullable = false)
    private Double stockFinalPoidsKg;

    @Column(name = "stock_final_valeur_cfa", nullable = false)
    private Double stockFinalValeurCfa;

    @CreationTimestamp
    @Column(name = "cree_le", updatable = false)
    private LocalDateTime creeLe;
}
