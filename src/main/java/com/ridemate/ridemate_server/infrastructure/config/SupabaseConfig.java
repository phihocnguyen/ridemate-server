package com.ridemate.ridemate_server.infrastructure.config;

import io.github.cdimascio.dotenv.Dotenv;
import lombok.Getter;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;

@Configuration
@Getter
public class SupabaseConfig {

    private final String supabaseUrl;
    private final String supabaseServiceKey;
    private final WebClient webClient;

    public SupabaseConfig() {
        Dotenv dotenv = Dotenv.configure()
                .ignoreIfMissing()
                .load();

        this.supabaseUrl = dotenv.get("SUPABASE_URL");
        this.supabaseServiceKey = dotenv.get("SUPABASE_SERVICE_KEY");

        if (supabaseUrl == null || supabaseServiceKey == null) {
            System.out.println("WARNING: Supabase credentials not found. Real-time features will be disabled.");
            System.out.println("Add SUPABASE_URL and SUPABASE_SERVICE_KEY to .env to enable real-time tracking.");
            this.webClient = null;
            return;
        }

        this.webClient = WebClient.builder()
                .baseUrl(supabaseUrl + "/rest/v1")
                .defaultHeader("apikey", supabaseServiceKey)
                .defaultHeader("Authorization", "Bearer " + supabaseServiceKey)
                .defaultHeader("Content-Type", "application/json")
                .defaultHeader("Prefer", "return=representation")
                .build();

        System.out.println("Supabase configuration initialized: " + supabaseUrl);
    }

    public WebClient getWebClient() {
        return webClient;
    }

    public boolean isEnabled() {
        return webClient != null;
    }
}
