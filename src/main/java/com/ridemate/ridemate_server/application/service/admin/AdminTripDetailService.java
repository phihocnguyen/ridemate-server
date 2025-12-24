package com.ridemate.ridemate_server.application.service.admin;

import com.ridemate.ridemate_server.application.dto.admin.TripDetailResponse;

import java.util.List;

public interface AdminTripDetailService {
    
    TripDetailResponse getTripDetail(Long tripId);
    
    List<TripDetailResponse> getAllTripsDetailed();
    
    List<TripDetailResponse> getCompletedTripsDetailed();
    
    List<TripDetailResponse> getCancelledTripsDetailed();
}
