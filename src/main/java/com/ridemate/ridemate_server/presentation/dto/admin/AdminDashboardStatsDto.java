package com.ridemate.ridemate_server.presentation.dto.admin;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AdminDashboardStatsDto {
    private long totalUsers;
    private long totalSessions;
    private long totalVouchers;
    private long totalVehicles;
    private long totalCompletedTrips;
    private long totalCancelledTrips;
    private long totalReports;
}
