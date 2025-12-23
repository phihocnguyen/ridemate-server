package com.ridemate.ridemate_server.application.service.notification.impl;

import com.ridemate.ridemate_server.application.dto.match.DriverCandidate;
import com.ridemate.ridemate_server.application.dto.notification.MatchNotification;
import com.ridemate.ridemate_server.domain.entity.Match;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Service for pushing match notifications to Supabase Realtime Database
 * Drivers will subscribe to realtime changes via Supabase client
 * Temporarily disabled
 */
@Slf4j
@Service
public class SupabaseNotificationService {

    @Autowired(required = false)
    private WebClient supabaseWebClient;
    
    // Removed SupabaseConfig dependency
    
    private static final String NOTIFICATIONS_TABLE = "/rest/v1/driver_notifications";
    private static final int TOP_DRIVERS_TO_NOTIFY = 5; // Notify top 5 closest drivers

    /**
     * Push notification to Supabase for top N drivers
     * Drivers subscribe to realtime changes on `driver_notifications` table
     */
    public void notifyDriversOfNewMatch(Match match, List<DriverCandidate> candidates) {
        log.warn("Supabase notifications are temporarily disabled");
        return;
        /*
        if (candidates == null || candidates.isEmpty()) {
            log.warn("No driver candidates to notify for match {}", match.getId());
            return;
        }

        int driversToNotify = Math.min(candidates.size(), TOP_DRIVERS_TO_NOTIFY);
        log.info("Notifying top {} drivers for match {}", driversToNotify, match.getId());

        for (int i = 0; i < driversToNotify; i++) {
            DriverCandidate candidate = candidates.get(i);
            pushNotificationToSupabase(match, candidate, i + 1);
        }
        */
    }

    /**
     * Push individual notification to Supabase
     * This will trigger realtime update on driver's mobile app
     */
    private void pushNotificationToSupabase(Match match, DriverCandidate candidate, int rank) {
        MatchNotification notification = MatchNotification.builder()
                .matchId(match.getId())
                .driverId(candidate.getDriverId())
                .passengerId(match.getPassenger().getId())
                .passengerName(match.getPassenger().getFullName())
                .passengerPhone(match.getPassenger().getPhoneNumber())
                .pickupAddress(match.getPickupAddress())
                .pickupLatitude(match.getPickupLatitude())
                .pickupLongitude(match.getPickupLongitude())
                .destinationAddress(match.getDestinationAddress())
                .destinationLatitude(match.getDestinationLatitude())
                .destinationLongitude(match.getDestinationLongitude())
                .coin(match.getCoin())
                .distanceToPickup(candidate.getDistanceToPickup())
                .estimatedArrivalTime(candidate.getEstimatedArrivalTime())
                .matchScore(candidate.getMatchScore())
                .status(match.getStatus().name())
                .notificationType("NEW_MATCH_REQUEST")
                .createdAt(LocalDateTime.now())
                .read(false)
                .build();

        // Push to Supabase REST API (will trigger realtime event)
        if (supabaseWebClient != null) {
            supabaseWebClient
                    .post()
                    .uri(NOTIFICATIONS_TABLE)
                    .bodyValue(notification)
                    .retrieve()
                    .bodyToMono(String.class)
                    .doOnSuccess(response -> {
                        log.info("✅ Notification pushed to Supabase for Driver {} (Rank #{}): Match {}, Distance: {:.2f}km, Score: {:.3f}",
                                candidate.getDriverId(), rank, match.getId(),
                                candidate.getDistanceToPickup(), candidate.getMatchScore());
                    })
                    .doOnError(error -> {
                        log.error("❌ Failed to push notification to Supabase for Driver {}: {}",
                                candidate.getDriverId(), error.getMessage());
                    })
                    .onErrorResume(e -> Mono.empty()) // Don't fail the whole process if one notification fails
                    .subscribe();
        } else {
             log.warn("Supabase WebClient is not initialized. Skipping notification.");
        }
    }
}
