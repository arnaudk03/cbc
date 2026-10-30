package bf.gov.anptic.sonagess.cbc.auth.repository;

import bf.gov.anptic.sonagess.cbc.auth.domain.RefreshToken;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.Optional;

public interface RefreshTokenRepository extends JpaRepository<RefreshToken, Long> {

    Optional<RefreshToken> findByTokenHash(String tokenHash);

    void deleteByUtilisateurId(Long utilisateurId);

    void deleteByDateExpirationBefore(LocalDateTime date);
}
