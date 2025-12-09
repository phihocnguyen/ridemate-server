package com.ridemate.ridemate_server.application.dto.admin;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Driver ranking entry
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DriverRankingResponse {
    private Long driverId;
    private String driverName;
    private String driverPhone;
    private String avatarUrl;
    
    private Integer totalTrips;
    private Integer totalCoinsEarned;
    private Double averageRating;
    private Double acceptanceRate;
    private Double completionRate;
    
    private Integer rank; // Position in leaderboard
}
