package com.ridemate.ridemate_server.application.service.driver;

import com.ridemate.ridemate_server.infrastructure.config.SupabaseConfig;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class SupabaseRealtimeService {

    private final SupabaseConfig supabaseConfig;

    public void publishDriverLocation(Long driverId, Double latitude, Double longitude, String driverStatus) {
        if (supabaseConfig.getWebClient() == null) {
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

            supabaseConfig.getWebClient()
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
        if (supabaseConfig.getWebClient() == null) {
            log.debug("Supabase not configured, skipping location update for driver {}", driverId);
            return;
        }
        
        try {
            Map<String, Object> locationData = new HashMap<>();
            locationData.put("latitude", latitude);
            locationData.put("longitude", longitude);
            locationData.put("driver_status", driverStatus);
            locationData.put("last_updated", Instant.now().toString());

            String locationPoint = String.format("POINT(%f %f)", longitude, latitude);
            locationData.put("location", locationPoint);

            supabaseConfig.getWebClient()
                    .patch()
                    .uri(uriBuilder -> uriBuilder
                            .path("/driver_locations")
                            .queryParam("driver_id", "eq." + driverId)
                            .build())
                    .bodyValue(locationData)
                    .retrieve()
                    .bodyToMono(String.class)
                    .doOnSuccess(response -> log.debug("Driver {} location updated in Supabase", driverId))
                    .doOnError(error -> {
                        log.warn("Failed to update driver {} location, trying insert", driverId);
                        publishDriverLocation(driverId, latitude, longitude, driverStatus);
                    })
                    .onErrorResume(e -> Mono.empty())
                    .subscribe();

        } catch (Exception e) {
            log.error("Error updating driver location in Supabase", e);
        }
    }

    public void removeDriverLocation(Long driverId) {
        if (supabaseConfig.getWebClient() == null) {
            log.debug("Supabase not configured, skipping location removal for driver {}", driverId);
            return;
        }
        
        try {
            supabaseConfig.getWebClient()
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
        if (supabaseConfig.getWebClient() == null) {
            log.debug("Supabase not configured, returning empty driver list");
            return Mono.just(List.of());
        }
        
        try {
            String query = String.format(
                    "location.st_dwithin(POINT(%f %f)::geography, %f)",
                    longitude, latitude, radiusKm * 1000
            );

            return supabaseConfig.getWebClient()
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
}
