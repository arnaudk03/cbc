package bf.gov.anptic.sonagess.cbc.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Getter
@Setter
@ConfigurationProperties(prefix = "application.security.jwt")
public class JwtProperties {

    private String secretKey;
    private long accessTokenExpiration = 86400000L;
    private long refreshTokenExpiration = 604800000L;
}
