package uk.ac.bangor.cs.dyp24nbv;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.PropertySource;

@SpringBootApplication
//@PropertySource("classpath:application-secrets.properties")
public class AisqlApplication {

	public static void main(String[] args) {
		SpringApplication.run(AisqlApplication.class, args);
	}
}