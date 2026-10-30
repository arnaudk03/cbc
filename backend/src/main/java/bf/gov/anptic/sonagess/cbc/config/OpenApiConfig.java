package bf.gov.anptic.sonagess.cbc.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    private static final String SECURITY_SCHEME_NAME = "BearerAuth";

    @Bean
    public OpenAPI cbcOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Plateforme Collecte Bord Champ (CBC) - SONAGESS")
                        .description("API RESTful pour la gestion dématérialisée de la collecte bord champ, " +
                                "du contrôle qualité, du paiement électronique et de la logistique des céréales au Burkina Faso.")
                        .version("1.0.0")
                        .contact(new Contact()
                                .name("DGTD / DCSI / ANPTIC")
                                .email("contact@anptic.gov.bf"))
                        .license(new License()
                                .name("Propriétaire SONAGESS / État Burkinabè")))
                .addSecurityItem(new SecurityRequirement().addList(SECURITY_SCHEME_NAME))
                .components(new Components()
                        .addSecuritySchemes(SECURITY_SCHEME_NAME, new SecurityScheme()
                                .name(SECURITY_SCHEME_NAME)
                                .type(SecurityScheme.Type.HTTP)
                                .scheme("bearer")
                                .bearerFormat("JWT")
                                .description("Entrez le token JWT obtenu lors de l'authentification (/api/v1/auth/login)")));
    }
}
