package com.ridemate.ridemate_server.application.dto.admin;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Dashboard statistics overview
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DashboardStatsResponse {
    
    // Total counts
    private Long totalDrivers;
    private Long totalPassengers;
    private Long totalTrips;
    private Long totalRevenue; // Total coins earned
    
    // Active stats
    private Long activeDrivers; // Currently online
    private Long activeTrips; // In progress
    private Long todayTrips;
    private Long todayRevenue;
    
    // Rating
    private Double averageRating;
    private Long totalRatings;
    
    // Growth percentages (compared to previous period)
    private Double driversGrowth;
    private Double tripsGrowth;
    private Double revenueGrowth;
}
