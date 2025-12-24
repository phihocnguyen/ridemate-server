package com.ridemate.ridemate_server.application.service.admin;

import com.ridemate.ridemate_server.application.dto.admin.DriverRankingResponse;

import java.util.List;

public interface AdminDriverAnalyticsService {
    
    List<DriverRankingResponse> getTopDrivers(int limit);
    
    Long getTotalDrivers();
    
    Long getActiveDrivers();
    
    Double getAverageDriverRating();
}
