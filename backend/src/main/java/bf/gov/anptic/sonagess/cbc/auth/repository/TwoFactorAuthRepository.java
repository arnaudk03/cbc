package bf.gov.anptic.sonagess.cbc.auth.repository;

import bf.gov.anptic.sonagess.cbc.auth.domain.TwoFactorAuth;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface TwoFactorAuthRepository extends JpaRepository<TwoFactorAuth, Long> {

    Optional<TwoFactorAuth> findByUtilisateurId(Long utilisateurId);

    Optional<TwoFactorAuth> findByUtilisateurIdAndActiveTrue(Long utilisateurId);
}
