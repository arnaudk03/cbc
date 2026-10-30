package bf.gov.anptic.sonagess.cbc.auth.dto;

import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record AuthResponse(
        boolean requires2fa,
        String challengeToken,
        String otpDemo,
        String accessToken,
        String refreshToken,
        String tokenType,
        Long expiresIn,
        UserSummary utilisateur
) {
    public static AuthResponse deuxFacteurs(String challengeToken, String otpDemo, UserSummary utilisateur) {
        return new AuthResponse(true, challengeToken, otpDemo, null, null, null, null, utilisateur);
    }

    public static AuthResponse connecte(String accessToken, String refreshToken, long expiresIn,
                                        UserSummary utilisateur) {
        return new AuthResponse(false, null, null, accessToken, refreshToken, "Bearer", expiresIn, utilisateur);
    }
}
