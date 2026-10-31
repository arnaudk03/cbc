package bf.gov.anptic.sonagess.cbc.equipements.domain;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToOne;
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
@Table(name = "equipements")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Equipement {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 50)
    private String code;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private TypeEquipement type;

    @Column(nullable = false, length = 150)
    private String designation;

    @Column(name = "numero_serie", unique = true, length = 100)
    private String numeroSerie;

    @Column(length = 100)
    private String marque;

    @Column(length = 100)
    private String modele;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    @Builder.Default
    private EtatEquipement etat = EtatEquipement.BON;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    @Builder.Default
    private StatutEquipement statut = StatutEquipement.DISPONIBLE;

    @Column(name = "date_acquisition")
    private LocalDate dateAcquisition;

    @Column(name = "valeur_cfa")
    private Double valeurCfa;

    @Column(columnDefinition = "TEXT")
    private String observation;

    @CreationTimestamp
    @Column(name = "cree_le", updatable = false)
    private LocalDateTime creeLe;

    @OneToOne(mappedBy = "equipement", fetch = FetchType.LAZY,
            cascade = CascadeType.ALL, orphanRemoval = true)
    private TabletteDevice tablette;
}
