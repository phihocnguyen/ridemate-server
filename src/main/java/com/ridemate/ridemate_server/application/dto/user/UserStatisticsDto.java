package com.ridemate.ridemate_server.application.dto.user;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserStatisticsDto {
    private Long totalUsers;
    private Long totalDrivers;
    private Long totalPassengers;
    private Long totalAdmins;
    private Long activeUsers;
    private Long inactiveUsers;
    private Long pendingDriverApprovals;
    private Long approvedDrivers;
    private Long rejectedDrivers;
}
