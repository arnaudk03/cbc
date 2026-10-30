package bf.gov.anptic.sonagess.cbc;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;
import org.springframework.scheduling.annotation.EnableAsync;

@SpringBootApplication
@ConfigurationPropertiesScan
@EnableAsync
public class CbcBackendApplication {

	public static void main(String[] args) {
		SpringApplication.run(CbcBackendApplication.class, args);
	}

}
