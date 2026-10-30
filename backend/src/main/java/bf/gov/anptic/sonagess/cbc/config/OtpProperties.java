package bf.gov.anptic.sonagess.cbc.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Getter
@Setter
@ConfigurationProperties(prefix = "application.security.otp")
public class OtpProperties {

    private int expirationMinutes = 5;
    private int maxAttempts = 5;
    private boolean demoMode = false;
}
