package bf.gov.anptic.sonagess.cbc.audit.domain;

import bf.gov.anptic.sonagess.cbc.auth.domain.Utilisateur;
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
@Table(name = "journal_audit")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class JournalAudit {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "utilisateur_id")
    private Utilisateur utilisateur;

    @Column(name = "matricule_utilisateur", length = 50)
    private String matriculeUtilisateur;

    @Column(nullable = false, length = 100)
    private String action;

    @Column(name = "entite_concernee", length = 100)
    private String entiteConcernee;

    @Column(name = "entite_id")
    private Long entiteId;

    @CreationTimestamp
    @Column(name = "date_action", updatable = false)
    private LocalDateTime dateAction;

    @Column(name = "adresse_ip", length = 45)
    private String adresseIp;

    @Column(name = "user_agent", columnDefinition = "TEXT")
    private String userAgent;

    @Column(length = 20)
    @Builder.Default
    private String resultat = "SUCCES";

    @Column(columnDefinition = "TEXT")
    private String details;
}
