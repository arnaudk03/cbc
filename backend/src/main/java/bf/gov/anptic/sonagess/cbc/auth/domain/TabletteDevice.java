package bf.gov.anptic.sonagess.cbc.auth.domain;

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
@Table(name = "tablette_devices")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TabletteDevice {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "utilisateur_id")
    private Utilisateur utilisateur;

    @Column(name = "numero_serie", nullable = false, unique = true, length = 100)
    private String numeroSerie;

    @Column(unique = true, length = 50)
    private String imei;

    @Column(length = 100)
    private String modele;

    @Column(name = "cle_chiffrement_locale")
    private String cleChiffrementLocale;

    @Column(length = 30)
    @Builder.Default
    private String statut = "ACTIVE";

    @Column(name = "date_derniere_synchro")
    private LocalDateTime dateDerniereSynchro;

    @CreationTimestamp
    @Column(name = "cree_le", updatable = false)
    private LocalDateTime creeLe;
}
