package bf.gov.anptic.sonagess.cbc.security;

import bf.gov.anptic.sonagess.cbc.common.BusinessException;
import bf.gov.anptic.sonagess.cbc.config.OtpProperties;
import bf.gov.anptic.sonagess.cbc.auth.domain.TwoFactorAuth;
import bf.gov.anptic.sonagess.cbc.auth.domain.Utilisateur;
import bf.gov.anptic.sonagess.cbc.notifications.domain.TypeNotification;
import bf.gov.anptic.sonagess.cbc.auth.repository.TwoFactorAuthRepository;
import bf.gov.anptic.sonagess.cbc.notifications.service.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class TwoFactorService {

    private final TwoFactorAuthRepository twoFactorAuthRepository;
    private final NotificationService notificationService;
    private final OtpProperties otpProperties;

    private final SecureRandom secureRandom = new SecureRandom();

    public boolean estActive(Utilisateur utilisateur) {
        return twoFactorAuthRepository.findByUtilisateurIdAndActiveTrue(utilisateur.getId()).isPresent();
    }

    public String envoyerOtp(Utilisateur utilisateur, String objet) {
        TwoFactorAuth twoFactorAuth = twoFactorAuthRepository
                .findByUtilisateurIdAndActiveTrue(utilisateur.getId())
                .orElseThrow(() -> new BusinessException(
                        "L'authentification à second facteur n'est pas configurée pour ce compte.",
                        HttpStatus.FORBIDDEN));

        String code = String.format("%06d", secureRandom.nextInt(1_000_000));
        twoFactorAuth.setDernierCodeOtp(code);
        twoFactorAuth.setDateExpirationOtp(LocalDateTime.now().plusMinutes(otpProperties.getExpirationMinutes()));
        twoFactorAuthRepository.save(twoFactorAuth);

        notificationService.envoyerSms(
                utilisateur.getTelephone(),
                "Code de validation SONAGESS",
                "Votre code de validation est " + code + ". Il expire dans "
                        + otpProperties.getExpirationMinutes() + " minutes. Objet : " + objet,
                TypeNotification.CHALLENGE_OTP_2FA,
                utilisateur, null, null, null, null
        );

        return code;
    }

    public void verifierOtp(Utilisateur utilisateur, String code) {
        TwoFactorAuth twoFactorAuth = twoFactorAuthRepository
                .findByUtilisateurIdAndActiveTrue(utilisateur.getId())
                .orElseThrow(() -> new BusinessException(
                        "L'authentification à second facteur n'est pas configurée pour ce compte.",
                        HttpStatus.FORBIDDEN));

        if (code == null || twoFactorAuth.getDernierCodeOtp() == null
                || !twoFactorAuth.getDernierCodeOtp().equals(code)) {
            throw new BusinessException("Code OTP invalide.", HttpStatus.UNAUTHORIZED);
        }
        if (twoFactorAuth.getDateExpirationOtp() == null
                || twoFactorAuth.getDateExpirationOtp().isBefore(LocalDateTime.now())) {
            throw new BusinessException("Code OTP expiré. Veuillez en demander un nouveau.",
                    HttpStatus.UNAUTHORIZED);
        }

        twoFactorAuth.setDernierCodeOtp(null);
        twoFactorAuth.setDateExpirationOtp(null);
        twoFactorAuthRepository.save(twoFactorAuth);
    }
}
