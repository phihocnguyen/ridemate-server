package com.ridemate.ridemate_server.application.service.admin;

import com.ridemate.ridemate_server.application.dto.admin.DashboardStatsResponse;

public interface AdminDashboardService {
    
    DashboardStatsResponse getComprehensiveDashboardStats();
}
