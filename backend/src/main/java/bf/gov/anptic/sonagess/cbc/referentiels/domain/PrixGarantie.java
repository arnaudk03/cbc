package bf.gov.anptic.sonagess.cbc.referentiels.domain;

import bf.gov.anptic.sonagess.cbc.auth.domain.Utilisateur;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
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
@Table(name = "prix_garanties")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PrixGarantie {

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

    @Column(name = "prix_unitaire_kg", nullable = false)
    private Double prixUnitaireKg;

    @Column(name = "date_effet", nullable = false)
    private LocalDate dateEffet;

    @Enumerated(EnumType.STRING)
    @Column(length = 30)
    @Builder.Default
    private StatutPrixGarantie statut = StatutPrixGarantie.EN_ATTENTE_APPROBATION;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "approuve_par")
    private Utilisateur approuvePar;

    @Column(name = "date_approbation")
    private LocalDateTime dateApprobation;

    @CreationTimestamp
    @Column(name = "cree_le", updatable = false)
    private LocalDateTime creeLe;
}
