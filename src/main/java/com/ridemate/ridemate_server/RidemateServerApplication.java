package com.ridemate.ridemate_server;

import com.ridemate.ridemate_server.application.config.DotEnvConfig;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication
@EnableJpaRepositories(basePackages = "com.ridemate.ridemate_server.domain.repository")
@ComponentScan(basePackages = {
	"com.ridemate.ridemate_server"
})
public class RidemateServerApplication {

	public static void main(String[] args) {
		
		new DotEnvConfig();
		SpringApplication.run(RidemateServerApplication.class, args);
	}

}
