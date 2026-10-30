package bf.gov.anptic.sonagess.cbc.auth.service;

import bf.gov.anptic.sonagess.cbc.audit.service.AuditService;
import bf.gov.anptic.sonagess.cbc.auth.dto.AuthResponse;
import bf.gov.anptic.sonagess.cbc.auth.dto.LoginRequest;
import bf.gov.anptic.sonagess.cbc.auth.dto.OtpVerificationRequest;
import bf.gov.anptic.sonagess.cbc.auth.dto.RefreshTokenRequest;
import bf.gov.anptic.sonagess.cbc.auth.dto.UserSummary;
import bf.gov.anptic.sonagess.cbc.common.BusinessException;
import bf.gov.anptic.sonagess.cbc.config.OtpProperties;
import bf.gov.anptic.sonagess.cbc.auth.domain.RefreshToken;
import bf.gov.anptic.sonagess.cbc.auth.domain.Utilisateur;
import bf.gov.anptic.sonagess.cbc.auth.repository.RefreshTokenRepository;
import bf.gov.anptic.sonagess.cbc.auth.repository.UtilisateurRepository;
import bf.gov.anptic.sonagess.cbc.security.CustomUserDetails;
import bf.gov.anptic.sonagess.cbc.security.JwtService;
import bf.gov.anptic.sonagess.cbc.security.TwoFactorService;
import io.jsonwebtoken.Claims;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.Base64;
import java.util.HexFormat;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthService {

    private final UtilisateurRepository utilisateurRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final TwoFactorService twoFactorService;
    private final AuditService auditService;
    private final OtpProperties otpProperties;

    @Value("${application.security.max-login-attempts:3}")
    private int maxLoginAttempts;

    private final SecureRandom secureRandom = new SecureRandom();

    public AuthResponse login(LoginRequest request, HttpServletRequest httpRequest) {
        Utilisateur utilisateur = utilisateurRepository.findByIdentifiant(request.identifiant().trim())
                .orElse(null);

        if (utilisateur == null) {
            auditService.echec(null, "AUTHENTIFICATION", "Utilisateur", null,
                    "Identifiant inconnu : " + request.identifiant());
            throw new BadCredentialsException("Identifiants invalides.");
        }

        if (!Boolean.TRUE.equals(utilisateur.getActif())) {
            auditService.echec(utilisateur, "AUTHENTIFICATION", "Utilisateur", utilisateur.getId(),
                    "Compte désactivé");
            throw new BusinessException("Ce compte est désactivé. Contactez l'administrateur.",
                    HttpStatus.FORBIDDEN);
        }

        if (Boolean.TRUE.equals(utilisateur.getCompteBloque())) {
            auditService.echec(utilisateur, "AUTHENTIFICATION", "Utilisateur", utilisateur.getId(),
                    "Compte verrouillé");
            throw new BusinessException("Compte verrouillé après plusieurs échecs. Contactez l'administrateur.",
                    HttpStatus.FORBIDDEN);
        }

        if (!passwordEncoder.matches(request.motDePasse(), utilisateur.getMotDePasseHash())) {
            int tentatives = Optional.ofNullable(utilisateur.getTentativesEchouees()).orElse(0) + 1;
            utilisateur.setTentativesEchouees(tentatives);
            boolean bloque = tentatives >= maxLoginAttempts;
            if (bloque) {
                utilisateur.setCompteBloque(true);
            }
            utilisateurRepository.save(utilisateur);
            auditService.echec(utilisateur, "AUTHENTIFICATION", "Utilisateur", utilisateur.getId(),
                    "Mot de passe incorrect - tentative " + tentatives);
            if (bloque) {
                throw new BadCredentialsException(
                        "Compte verrouillé après " + maxLoginAttempts + " tentatives échouées.");
            }
            throw new BadCredentialsException("Identifiants invalides. Tentative " + tentatives
                    + " sur " + maxLoginAttempts + ".");
        }

        utilisateur.setTentativesEchouees(0);
        utilisateur.setDateDerniereConnexion(LocalDateTime.now());
        utilisateurRepository.save(utilisateur);

        if (twoFactorService.estActive(utilisateur)) {
            String code = twoFactorService.envoyerOtp(utilisateur, "connexion à la plateforme");
            String challengeToken = jwtService.generateChallengeToken(utilisateur.getId(),
                    utilisateur.getMatricule());
            auditService.succes(utilisateur, "AUTHENTIFICATION_2FA_DEMANDEE", "Utilisateur",
                    utilisateur.getId(), "Code OTP envoyé par SMS");
            return AuthResponse.deuxFacteurs(challengeToken,
                    otpProperties.isDemoMode() ? code : null,
                    UserSummary.from(utilisateur));
        }

        return genererJeton(utilisateur, httpRequest, true);
    }

    public AuthResponse verifierOtp(OtpVerificationRequest request, HttpServletRequest httpRequest) {
        Claims claims = jwtService.parseChallengeToken(request.challengeToken())
                .orElseThrow(() -> new BusinessException("Jeton de challenge invalide ou expiré.",
                        HttpStatus.UNAUTHORIZED));
        Long userId = jwtService.extractUserId(claims);
        Utilisateur utilisateur = utilisateurRepository.findById(userId)
                .orElseThrow(() -> new BusinessException("Utilisateur introuvable.", HttpStatus.UNAUTHORIZED));

        twoFactorService.verifierOtp(utilisateur, request.code());

        utilisateur.setTentativesEchouees(0);
        utilisateur.setDateDerniereConnexion(LocalDateTime.now());
        utilisateurRepository.save(utilisateur);
        auditService.succes(utilisateur, "AUTHENTIFICATION_REUSSIE", "Utilisateur", utilisateur.getId(),
                "Validation 2FA réussie");

        return genererJeton(utilisateur, httpRequest, false);
    }

    @Transactional
    public AuthResponse rafraichir(RefreshTokenRequest request, HttpServletRequest httpRequest) {
        RefreshToken refreshToken = refreshTokenRepository.findByTokenHash(hasher(request.refreshToken()))
                .orElseThrow(() -> new BusinessException("Refresh token invalide.", HttpStatus.UNAUTHORIZED));

        if (Boolean.TRUE.equals(refreshToken.getRevoque())
                || refreshToken.getDateExpiration().isBefore(LocalDateTime.now())) {
            throw new BusinessException("Session expirée. Veuillez vous reconnecter.",
                    HttpStatus.UNAUTHORIZED);
        }

        Utilisateur utilisateur = refreshToken.getUtilisateur();
        if (!Boolean.TRUE.equals(utilisateur.getActif())
                || Boolean.TRUE.equals(utilisateur.getCompteBloque())) {
            throw new BusinessException("Compte désactivé ou verrouillé.", HttpStatus.FORBIDDEN);
        }

        refreshToken.setRevoque(true);
        refreshTokenRepository.save(refreshToken);

        return genererJeton(utilisateur, httpRequest, false);
    }

    @Transactional
    public void deconnexion(RefreshTokenRequest request, CustomUserDetails principal) {
        refreshTokenRepository.findByTokenHash(hasher(request.refreshToken()))
                .filter(token -> token.getUtilisateur().getId().equals(principal.getId()))
                .ifPresent(token -> {
                    token.setRevoque(true);
                    refreshTokenRepository.save(token);
                });
        auditService.succes(utilisateurRepository.findById(principal.getId()).orElse(null),
                "DECONNEXION", "Utilisateur", principal.getId(), "Refresh token révoqué");
    }

    @Transactional(readOnly = true)
    public UserSummary profil(Long userId) {
        Utilisateur utilisateur = utilisateurRepository.findById(userId)
                .orElseThrow(() -> new BusinessException("Utilisateur introuvable.", HttpStatus.NOT_FOUND));
        return UserSummary.from(utilisateur);
    }

    private AuthResponse genererJeton(Utilisateur utilisateur, HttpServletRequest httpRequest,
                                      boolean journaliserConnexion) {
        CustomUserDetails userDetails = new CustomUserDetails(utilisateur);
        String accessToken = jwtService.generateAccessToken(userDetails);
        String refreshTokenBrut = genererRefreshTokenBrut();

        RefreshToken refreshToken = RefreshToken.builder()
                .utilisateur(utilisateur)
                .tokenHash(hasher(refreshTokenBrut))
                .dateExpiration(LocalDateTime.now().plusNanos(
                        jwtService.getRefreshTokenExpirationMillis() * 1_000_000))
                .revoque(false)
                .adresseIpCreation(extraireIp(httpRequest))
                .build();
        refreshTokenRepository.save(refreshToken);

        if (journaliserConnexion) {
            auditService.succes(utilisateur, "AUTHENTIFICATION_REUSSIE", "Utilisateur",
                    utilisateur.getId(), "Connexion sans second facteur");
        }

        return AuthResponse.connecte(accessToken, refreshTokenBrut,
                jwtService.getAccessTokenExpirationSeconds(), UserSummary.from(utilisateur));
    }

    private String genererRefreshTokenBrut() {
        byte[] bytes = new byte[64];
        secureRandom.nextBytes(bytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }

    private String hasher(String valeur) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            return HexFormat.of().formatHex(digest.digest(valeur.getBytes(StandardCharsets.UTF_8)));
        } catch (NoSuchAlgorithmException ex) {
            throw new IllegalStateException("Algorithme SHA-256 indisponible", ex);
        }
    }

    private String extraireIp(HttpServletRequest request) {
        if (request == null) {
            return null;
        }
        String forwarded = request.getHeader("X-Forwarded-For");
        if (forwarded != null && !forwarded.isBlank()) {
            return forwarded.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }
}
