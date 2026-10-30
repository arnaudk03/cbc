package bf.gov.anptic.sonagess.cbc.auth.dto;

import jakarta.validation.constraints.NotBlank;

public record LoginRequest(
        @NotBlank(message = "L'identifiant (email ou matricule) est obligatoire")
        String identifiant,

        @NotBlank(message = "Le mot de passe est obligatoire")
        String motDePasse
) {
}
