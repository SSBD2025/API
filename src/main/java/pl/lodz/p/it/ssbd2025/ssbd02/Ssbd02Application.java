package pl.lodz.p.it.ssbd2025.ssbd02;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableAsync
@EnableScheduling
public class Ssbd02Application {

	public static void main(String[] args) {
		SpringApplication.run(Ssbd02Application.class, args);
	}

}
