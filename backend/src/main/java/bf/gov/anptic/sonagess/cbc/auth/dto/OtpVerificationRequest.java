package bf.gov.anptic.sonagess.cbc.auth.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public record OtpVerificationRequest(
        @NotBlank(message = "Le jeton de challenge est obligatoire")
        String challengeToken,

        @NotBlank(message = "Le code OTP est obligatoire")
        @Pattern(regexp = "\\d{6}", message = "Le code OTP doit contenir 6 chiffres")
        String code
) {
}
