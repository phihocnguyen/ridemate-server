package com.ridemate.ridemate_server.application.dto.admin;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TripDetailResponse {
    private Long tripId;
    private String tripCode;
    
    // Driver info
    private Long driverId;
    private String driverName;
    private String driverPhone;
    private String driverAvatar;
    private Double driverRating;
    
    // Passenger info
    private Long passengerId;
    private String passengerName;
    private String passengerPhone;
    private String passengerAvatar;
    
    // Trip details
    private String pickupAddress;
    private Double pickupLatitude;
    private Double pickupLongitude;
    
    private String dropoffAddress;
    private Double dropoffLatitude;
    private Double dropoffLongitude;
    
    private Integer coinAmount;
    private Double distance;
    private Integer duration; // minutes
    
    // Status & Timing
    private String status; // PENDING, WAITING, ACCEPTED, IN_PROGRESS, COMPLETED, CANCELLED
    private LocalDateTime createdAt;
    private LocalDateTime acceptedAt;
    private LocalDateTime startedAt;
    private LocalDateTime completedAt;
}
