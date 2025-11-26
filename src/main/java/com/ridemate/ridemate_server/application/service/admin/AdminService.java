package com.ridemate.ridemate_server.application.service.admin;

import com.ridemate.ridemate_server.presentation.dto.admin.AdminChartDataDto;
import com.ridemate.ridemate_server.presentation.dto.admin.AdminDashboardStatsDto;

public interface AdminService {
    AdminDashboardStatsDto getDashboardStats();
    AdminChartDataDto getChartData(String type); // type: "users", "sessions"
}
