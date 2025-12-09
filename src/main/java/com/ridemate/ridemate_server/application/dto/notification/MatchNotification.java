package com.ridemate.ridemate_server.application.dto.notification;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Notification DTO to push to Supabase Realtime Database
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MatchNotification {
    
    private Long matchId;
    private Long driverId;
    private Long passengerId;
    private String passengerName;
    private String passengerPhone;
    
    private String pickupAddress;
    private Double pickupLatitude;
    private Double pickupLongitude;
    
    private String destinationAddress;
    private Double destinationLatitude;
    private Double destinationLongitude;
    
    private Integer coin;
    private Double distanceToPickup; // km
    private Integer estimatedArrivalTime; // minutes
    private Double matchScore;
    
    private String status; // "PENDING", "WAITING", "ACCEPTED", etc.
    private String notificationType; // "NEW_MATCH_REQUEST"
    
    private LocalDateTime createdAt;
    private Boolean read;
}
