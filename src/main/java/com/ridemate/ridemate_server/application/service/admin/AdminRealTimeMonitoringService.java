package com.ridemate.ridemate_server.application.service.admin;

import com.ridemate.ridemate_server.application.dto.admin.ActiveTripResponse;

import java.util.List;

public interface AdminRealTimeMonitoringService {
    
    List<ActiveTripResponse> getActiveTrips();
    
    Long getActiveTripsCount();
    
    List<ActiveTripResponse> getTripsInProgress();
    
    List<ActiveTripResponse> getAcceptedTrips();
}
