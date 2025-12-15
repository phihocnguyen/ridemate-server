package com.ridemate.ridemate_server.presentation.dto.admin;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TripStatsDto {
    private long totalTrips;
    private long pendingTrips;
    private long matchedTrips;
    private long inProgressTrips;
    private long completedTrips;
    private long cancelledTrips;
    private double completionRate; // percentage
    private double cancellationRate; // percentage
}
