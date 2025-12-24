package com.ridemate.ridemate_server.application.config;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;

/**
 * Supabase configuration for realtime notifications via REST API
 */
@Slf4j
@Getter
@Configuration
public class SupabaseConfig {

    @Value("${SUPABASE_URL:}")
    private String supabaseUrl;

    @Value("${SUPABASE_SERVICE_KEY:}")
    private String supabaseServiceKey;

    @Bean
    public WebClient supabaseWebClient() {
        if (supabaseUrl == null || supabaseUrl.isEmpty() || 
            supabaseServiceKey == null || supabaseServiceKey.isEmpty()) {
            log.warn("⚠️ Supabase credentials not configured. Realtime features will be disabled.");
            log.warn("   Set SUPABASE_URL and SUPABASE_SERVICE_KEY in .env file");
            return null;
        }
        
        log.info("✅ Initializing Supabase WebClient with URL: {}", supabaseUrl);
        return WebClient.builder()
                .baseUrl(supabaseUrl + "/rest/v1")
                .defaultHeader("apikey", supabaseServiceKey)
                .defaultHeader("Authorization", "Bearer " + supabaseServiceKey)
                .defaultHeader("Content-Type", "application/json")
                .defaultHeader("Prefer", "return=representation")
                .build();
    }
}
