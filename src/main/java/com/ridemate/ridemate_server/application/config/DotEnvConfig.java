package com.ridemate.ridemate_server.application.config;

import io.github.cdimascio.dotenv.Dotenv;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Configuration;

import java.io.File;

@Slf4j
@Configuration
public class DotEnvConfig {

    static {
        
        String envPath = ".env";
        File envFile = new File(envPath);
        
        if (envFile.exists()) {
            try {
                Dotenv dotenv = Dotenv.load();
                
                dotenv.entries().forEach(entry -> {
                    String key = entry.getKey();
                    String value = entry.getValue();
                    System.setProperty(key, value);
                    if (!key.contains("PASSWORD") && !key.contains("SECRET") && !key.contains("TOKEN")) {
                        log.debug("Loaded environment variable: {}", key);
                    }
                });
                log.info(".env file loaded successfully");
            } catch (Exception e) {
                log.warn("Failed to load .env file: {}", e.getMessage());
            }
        } else {
            log.info(".env file not found, using system environment variables or application.properties");
        }
    }
}
