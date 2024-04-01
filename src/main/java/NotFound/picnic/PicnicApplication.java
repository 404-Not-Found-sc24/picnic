package NotFound.picnic;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;

@SpringBootApplication(exclude = SecurityAutoConfiguration.class)
public class PicnicApplication {

	public static void main(String[] args) {
		SpringApplication.run(PicnicApplication.class, args);
		// ci/cd test
	}

}
