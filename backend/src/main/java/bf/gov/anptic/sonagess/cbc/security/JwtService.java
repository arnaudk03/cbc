package bf.gov.anptic.sonagess.cbc.security;

import bf.gov.anptic.sonagess.cbc.config.JwtProperties;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.util.Date;
import java.util.List;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class JwtService {

    public static final String TYPE_ACCESS = "ACCESS";
    public static final String TYPE_2FA = "2FA";

    private final JwtProperties jwtProperties;
    private SecretKey secretKey;

    @PostConstruct
    void init() {
        byte[] keyBytes = Decoders.BASE64.decode(jwtProperties.getSecretKey());
        if (keyBytes.length < 32) {
            throw new IllegalStateException("La clé JWT doit contenir au moins 256 bits");
        }
        this.secretKey = Keys.hmacShaKeyFor(keyBytes);
    }

    public String generateAccessToken(CustomUserDetails user) {
        Date now = new Date();
        Date expiry = new Date(now.getTime() + jwtProperties.getAccessTokenExpiration());
        return Jwts.builder()
                .subject(user.getMatricule())
                .claim("uid", user.getId().toString())
                .claim("type", TYPE_ACCESS)
                .claim("email", user.getEmail())
                .claim("nom", user.getNomComplet())
                .claim("roles", List.copyOf(user.getRoles()))
                .issuedAt(now)
                .expiration(expiry)
                .signWith(secretKey)
                .compact();
    }

    public String generateChallengeToken(Long userId, String matricule) {
        Date now = new Date();
        Date expiry = new Date(now.getTime() + 5 * 60 * 1000L);
        return Jwts.builder()
                .subject(matricule)
                .claim("uid", userId.toString())
                .claim("type", TYPE_2FA)
                .issuedAt(now)
                .expiration(expiry)
                .signWith(secretKey)
                .compact();
    }

    public Optional<Claims> parseAccessToken(String token) {
        return parse(token, TYPE_ACCESS);
    }

    public Optional<Claims> parseChallengeToken(String token) {
        return parse(token, TYPE_2FA);
    }

    private Optional<Claims> parse(String token, String expectedType) {
        try {
            Claims claims = Jwts.parser()
                    .verifyWith(secretKey)
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();
            String type = claims.get("type", String.class);
            if (!expectedType.equals(type)) {
                return Optional.empty();
            }
            return Optional.of(claims);
        } catch (JwtException | IllegalArgumentException ex) {
            log.debug("Jeton JWT invalide : {}", ex.getMessage());
            return Optional.empty();
        }
    }

    public Long extractUserId(Claims claims) {
        String value = claims.get("uid", String.class);
        return value == null ? null : Long.valueOf(value);
    }

    public long getAccessTokenExpirationSeconds() {
        return jwtProperties.getAccessTokenExpiration() / 1000;
    }

    public long getRefreshTokenExpirationMillis() {
        return jwtProperties.getRefreshTokenExpiration();
    }
}
