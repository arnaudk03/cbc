package bf.gov.anptic.sonagess.cbc.auth.web;

import bf.gov.anptic.sonagess.cbc.auth.service.AuthService;
import bf.gov.anptic.sonagess.cbc.auth.dto.AuthResponse;
import bf.gov.anptic.sonagess.cbc.auth.dto.LoginRequest;
import bf.gov.anptic.sonagess.cbc.auth.dto.OtpVerificationRequest;
import bf.gov.anptic.sonagess.cbc.auth.dto.RefreshTokenRequest;
import bf.gov.anptic.sonagess.cbc.auth.dto.UserSummary;
import bf.gov.anptic.sonagess.cbc.common.ApiResponse;
import bf.gov.anptic.sonagess.cbc.security.CustomUserDetails;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/login")
    public ResponseEntity<ApiResponse<AuthResponse>> login(@Valid @RequestBody LoginRequest request,
                                                           HttpServletRequest httpRequest) {
        AuthResponse response = authService.login(request, httpRequest);
        String message = response.requires2fa()
                ? "Un code de validation à second facteur a été envoyé."
                : "Connexion réussie.";
        return ResponseEntity.ok(ApiResponse.ok(response, message));
    }

    @PostMapping("/verify-otp")
    public ResponseEntity<ApiResponse<AuthResponse>> verifierOtp(
            @Valid @RequestBody OtpVerificationRequest request,
            HttpServletRequest httpRequest) {
        return ResponseEntity.ok(ApiResponse.ok(
                authService.verifierOtp(request, httpRequest),
                "Authentification à second facteur validée."));
    }

    @PostMapping("/refresh")
    public ResponseEntity<ApiResponse<AuthResponse>> rafraichir(
            @Valid @RequestBody RefreshTokenRequest request,
            HttpServletRequest httpRequest) {
        return ResponseEntity.ok(ApiResponse.ok(
                authService.rafraichir(request, httpRequest),
                "Session rafraîchie."));
    }

    @PostMapping("/logout")
    public ResponseEntity<ApiResponse<Void>> deconnexion(
            @Valid @RequestBody RefreshTokenRequest request,
            @AuthenticationPrincipal CustomUserDetails principal) {
        authService.deconnexion(request, principal);
        return ResponseEntity.ok(ApiResponse.ok(null, "Déconnexion réussie."));
    }

    @GetMapping("/me")
    public ResponseEntity<ApiResponse<UserSummary>> profil(
            @AuthenticationPrincipal CustomUserDetails principal) {
        return ResponseEntity.ok(ApiResponse.ok(authService.profil(principal.getId())));
    }
}
