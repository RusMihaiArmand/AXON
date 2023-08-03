package ro.axon.dot;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;

@SpringBootApplication
@EnableCaching
public class DotApplication {

	public static void main(String[] args) {
		SpringApplication.run(DotApplication.class, args);
	}

}
