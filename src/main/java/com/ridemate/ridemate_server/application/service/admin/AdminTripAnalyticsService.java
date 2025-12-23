package com.ridemate.ridemate_server.application.service.admin;

import com.ridemate.ridemate_server.application.dto.admin.TripVolumeResponse;

import java.time.LocalDate;
import java.util.List;

public interface AdminTripAnalyticsService {
    
    List<TripVolumeResponse> getTripVolumeByDateRange(LocalDate startDate, LocalDate endDate);
    
    Long getTotalTrips();
    
    Long getTodayTrips();
    
    Long getCompletedTripsCount();
    
    Long getCancelledTripsCount();
}
