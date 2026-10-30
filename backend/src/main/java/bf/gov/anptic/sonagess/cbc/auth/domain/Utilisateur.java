package bf.gov.anptic.sonagess.cbc.auth.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import bf.gov.anptic.sonagess.cbc.referentiels.domain.Comptoir;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "utilisateurs")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Utilisateur {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 50)
    private String matricule;

    @Column(nullable = false, length = 100)
    private String nom;

    @Column(nullable = false, length = 100)
    private String prenom;

    @Column(nullable = false, unique = true, length = 150)
    private String email;

    @Column(nullable = false, unique = true, length = 30)
    private String telephone;

    @Column(name = "mot_de_passe_hash", nullable = false)
    private String motDePasseHash;

    @Builder.Default
    private Boolean actif = true;

    @Column(name = "compte_bloque")
    @Builder.Default
    private Boolean compteBloque = false;

    @Column(name = "tentatives_echouees")
    @Builder.Default
    private Integer tentativesEchouees = 0;

    @Column(name = "date_derniere_connexion")
    private LocalDateTime dateDerniereConnexion;

    @CreationTimestamp
    @Column(name = "cree_le", updatable = false)
    private LocalDateTime creeLe;

    @UpdateTimestamp
    @Column(name = "modifie_le")
    private LocalDateTime modifieLe;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "comptoir_id")
    private Comptoir comptoir;

    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(
            name = "utilisateur_roles",
            joinColumns = @JoinColumn(name = "utilisateur_id"),
            inverseJoinColumns = @JoinColumn(name = "role_id")
    )
    @Builder.Default
    private Set<Role> roles = new HashSet<>();

    public boolean hasRole(String roleCode) {
        return roles.stream().anyMatch(role -> role.getCode().equalsIgnoreCase(roleCode));
    }

    public String getNomComplet() {
        return prenom + " " + nom;
    }
}
