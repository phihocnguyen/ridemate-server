package com.ridemate.ridemate_server;

import com.ridemate.ridemate_server.application.config.DotEnvConfig;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class RidemateServerApplication {

	public static void main(String[] args) {
		
		new DotEnvConfig();
		SpringApplication.run(RidemateServerApplication.class, args);
	}

}
