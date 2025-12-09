package com.ridemate.ridemate_server.application.dto.admin;

import com.ridemate.ridemate_server.application.dto.match.MatchResponse;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Active trip with full details
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ActiveTripResponse {
    private Long matchId;
    private String status; // IN_PROGRESS, ACCEPTED
    
    // Driver info
    private Long driverId;
    private String driverName;
    private String driverPhone;
    private String vehicleInfo;
    
    // Passenger info
    private Long passengerId;
    private String passengerName;
    private String passengerPhone;
    
    // Trip details
    private String pickupAddress;
    private String destinationAddress;
    private Integer coin;
    
    // Timing
    private LocalDateTime startTime;
    private LocalDateTime acceptedAt;
    private Integer estimatedDuration; // minutes
    
    // Location
    private Double currentLatitude;
    private Double currentLongitude;
}
