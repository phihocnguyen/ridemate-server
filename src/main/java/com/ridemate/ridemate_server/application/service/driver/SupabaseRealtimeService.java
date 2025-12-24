package com.ridemate.ridemate_server.application.service.driver;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
public class SupabaseRealtimeService {

    @Autowired(required = false)
    private WebClient supabaseWebClient;

    public void publishDriverLocation(Long driverId, Double latitude, Double longitude, String driverStatus) {
        if (supabaseWebClient == null) {
            log.debug("Supabase not configured, skipping location publish for driver {}", driverId);
            return;
        }
        
        try {
            Map<String, Object> locationData = new HashMap<>();
            locationData.put("driver_id", driverId);
            locationData.put("latitude", latitude);
            locationData.put("longitude", longitude);
            locationData.put("driver_status", driverStatus);
            locationData.put("last_updated", Instant.now().toString());

            String locationPoint = String.format("POINT(%f %f)", longitude, latitude);
            locationData.put("location", locationPoint);

            supabaseWebClient
                    .post()
                    .uri("/driver_locations")
                    .bodyValue(locationData)
                    .retrieve()
                    .bodyToMono(String.class)
                    .doOnSuccess(response -> log.debug("Driver {} location published to Supabase", driverId))
                    .doOnError(error -> log.error("Failed to publish driver {} location: {}", driverId, error.getMessage()))
                    .onErrorResume(e -> Mono.empty())
                    .subscribe();

        } catch (Exception e) {
            log.error("Error publishing driver location to Supabase", e);
        }
    }

    public void updateDriverLocation(Long driverId, Double latitude, Double longitude, String driverStatus) {
        if (supabaseWebClient == null) {
            log.debug("Supabase not configured, skipping location update for driver {}", driverId);
            return;
        }
        
        try {
            Map<String, Object> locationData = new HashMap<>();
            locationData.put("driver_id", driverId); // Ensure ID is present for UPSERT
            locationData.put("latitude", latitude);
            locationData.put("longitude", longitude);
            locationData.put("driver_status", driverStatus);
            locationData.put("last_updated", Instant.now().toString());

            String locationPoint = String.format("POINT(%f %f)", longitude, latitude);
            locationData.put("location", locationPoint);

            // Use UPSERT (POST with Prefer: resolution=merge-duplicates)
            supabaseWebClient
                    .post()
                    .uri(uriBuilder -> uriBuilder
                            .path("/driver_locations")
                            .queryParam("on_conflict", "driver_id")
                            .build())
                    .header("Prefer", "resolution=merge-duplicates")
                    .bodyValue(locationData)
                    .retrieve()
                    .bodyToMono(String.class)
                    .doOnSuccess(response -> log.debug("Driver {} location updated (upsert) in Supabase", driverId))
                    .doOnError(error -> {
                         log.error("Failed to update driver {} location: {}", driverId, error.getMessage());
                         if (error instanceof org.springframework.web.reactive.function.client.WebClientResponseException) {
                            org.springframework.web.reactive.function.client.WebClientResponseException ex = (org.springframework.web.reactive.function.client.WebClientResponseException) error;
                            log.error("Response body: {}", ex.getResponseBodyAsString());
                        }
                    })
                    .onErrorResume(e -> Mono.empty())
                    .subscribe();

        } catch (Exception e) {
            log.error("Error updating driver location in Supabase", e);
        }
    }

    public void removeDriverLocation(Long driverId) {
        if (supabaseWebClient == null) {
            log.debug("Supabase not configured, skipping location removal for driver {}", driverId);
            return;
        }
        
        try {
            supabaseWebClient
                    .delete()
                    .uri(uriBuilder -> uriBuilder
                            .path("/driver_locations")
                            .queryParam("driver_id", "eq." + driverId)
                            .build())
                    .retrieve()
                    .bodyToMono(Void.class)
                    .doOnSuccess(response -> log.info("Driver {} location removed from Supabase", driverId))
                    .doOnError(error -> log.error("Failed to remove driver {} location: {}", driverId, error.getMessage()))
                    .onErrorResume(e -> Mono.empty())
                    .subscribe();

        } catch (Exception e) {
            log.error("Error removing driver location from Supabase", e);
        }
    }

    public Mono<List<Map<String, Object>>> getNearbyDrivers(Double latitude, Double longitude, Double radiusKm) {
        if (supabaseWebClient == null) {
            log.debug("Supabase not configured, returning empty driver list");
            return Mono.just(List.of());
        }
        
        try {
            // Note: PostGIS query simulation for Supabase Rest API
            // Ideally we should use an RPC call, but for simplicity we filter by status first
            // and perform client-side filtering if complex query is not supported unless we setup RPC
            
            return supabaseWebClient
                    .get()
                    .uri(uriBuilder -> uriBuilder
                            .path("/driver_locations")
                            .queryParam("driver_status", "eq.ONLINE")
                            .queryParam("select", "*")
                            .build())
                    .retrieve()
                    .bodyToFlux(Map.class)
                    .collectList()
                    .map(list -> (List<Map<String, Object>>) (List<?>) list)
                    .doOnSuccess(drivers -> log.debug("Found {} nearby drivers", drivers.size()))
                    .doOnError(error -> log.error("Failed to get nearby drivers: {}", error.getMessage()));

        } catch (Exception e) {
            log.error("Error getting nearby drivers from Supabase", e);
            return Mono.just(List.of());
        }
    }

    public void publishMatch(Map<String, Object> matchData) {
        if (supabaseWebClient == null) {
            log.warn("⚠️ Supabase WebClient not configured, skipping match publish");
            return;
        }

        try {
            Object matchId = matchData.get("id");
            if (matchId == null) {
                log.warn("⚠️ Cannot publish match to Supabase: match ID is missing");
                return;
            }

            log.info("📤 Publishing match {} to Supabase...", matchId);

            // Use POST with UPSERT (on_conflict) to handle both insert and update cases
            // This ensures match is created/updated in Supabase and triggers realtime event
            supabaseWebClient
                    .post()
                    .uri(uriBuilder -> uriBuilder
                            .path("/matches")
                            .queryParam("on_conflict", "id")
                            .build())
                    .header("Prefer", "resolution=merge-duplicates")
                    .bodyValue(matchData)
                    .retrieve()
                    .bodyToMono(String.class)
                    .doOnSuccess(response -> log.info("✅ Match {} published/updated in Supabase (realtime trigger): {}", matchId, response))
                    .doOnError(error -> {
                        if (error instanceof org.springframework.web.reactive.function.client.WebClientResponseException) {
                            org.springframework.web.reactive.function.client.WebClientResponseException ex = (org.springframework.web.reactive.function.client.WebClientResponseException) error;
                            log.error("❌ Failed to publish match {} to Supabase. Status: {}, Body: {}", matchId, ex.getStatusCode(), ex.getResponseBodyAsString());
                        } else {
                            log.error("❌ Failed to publish match {} to Supabase: {}", matchId, error.getMessage());
                        }
                    })
                    .onErrorResume(e -> {
                        log.error("❌ Error publishing match {} to Supabase, will try PATCH as fallback", matchId, e);
                        // Fallback: Try PATCH if POST fails (match might already exist)
                        return tryPatchMatch(matchId, matchData);
                    })
                    .subscribe();

        } catch (Exception e) {
            log.error("❌ Error publishing match to Supabase", e);
        }
    }

    private Mono<String> tryPatchMatch(Object matchId, Map<String, Object> matchData) {
        if (supabaseWebClient == null) {
            return Mono.empty();
        }

        return supabaseWebClient
                .patch()
                .uri(uriBuilder -> uriBuilder
                        .path("/matches")
                        .queryParam("id", "eq." + matchId)
                        .build())
                .bodyValue(matchData)
                .retrieve()
                .bodyToMono(String.class)
                .doOnSuccess(response -> log.info("✅ Match {} updated in Supabase via PATCH (fallback): {}", matchId, response))
                .doOnError(error -> log.error("❌ Failed to update match {} in Supabase via PATCH: {}", matchId, error.getMessage()))
                .onErrorResume(e -> Mono.empty());
    }
}
