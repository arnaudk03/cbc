package bf.gov.anptic.sonagess.cbc.auth.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "two_factor_auth")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TwoFactorAuth {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "utilisateur_id", unique = true)
    private Utilisateur utilisateur;

    @Builder.Default
    private Boolean active = false;

    @Column(name = "secret_key_totp")
    private String secretKeyTotp;

    @Column(name = "dernier_code_otp", length = 10)
    private String dernierCodeOtp;

    @Column(name = "date_expiration_otp")
    private LocalDateTime dateExpirationOtp;

    @Column(name = "type_2fa", length = 20)
    @Builder.Default
    private String type2FA = "SMS";
}
