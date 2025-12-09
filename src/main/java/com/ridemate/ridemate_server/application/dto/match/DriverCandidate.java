package com.ridemate.ridemate_server.application.dto.match;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO representing a driver candidate for matching algorithm
 * Contains all metrics needed for scoring
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DriverCandidate {
    private Long driverId;
    private String driverName;
    private String driverPhone;
    private Long vehicleId;
    private String vehicleInfo;
    
    // Location data
    private Double currentLatitude;
    private Double currentLongitude;
    private Double distanceToPickup; // in kilometers
    
    // Driver metrics for scoring
    private Float driverRating;
    private Float acceptanceRate;
    private Float completionRate;
    private Integer totalRidesCompleted;
    
    // Calculated score
    private Double matchScore;
    
    // ETA (estimated time of arrival) in minutes
    private Integer estimatedArrivalTime;
}
