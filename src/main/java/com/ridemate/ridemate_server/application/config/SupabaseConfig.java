package com.ridemate.ridemate_server.application.config;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;

/**
 * Supabase configuration for realtime notifications via REST API
 * Temporarily disabled - set supabase.enabled=true in .env to enable
 */
@Slf4j
@Getter
@Configuration
@ConditionalOnProperty(name = "supabase.enabled", havingValue = "true", matchIfMissing = false)
public class SupabaseConfig {

    @Value("${supabase.url:https://disabled}")
    private String supabaseUrl;

    @Value("${supabase.anon.key:disabled}")
    private String supabaseAnonKey;

    @Bean
    public WebClient supabaseWebClient() {
        log.info("Initializing Supabase WebClient with URL: {}", supabaseUrl);
        return WebClient.builder()
                .baseUrl(supabaseUrl)
                .defaultHeader("apikey", supabaseAnonKey)
                .defaultHeader("Authorization", "Bearer " + supabaseAnonKey)
                .defaultHeader("Content-Type", "application/json")
                .defaultHeader("Prefer", "return=representation")
                .build();
    }
}
