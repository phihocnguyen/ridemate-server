package com.ridemate.ridemate_server.application.service.location;

import com.ridemate.ridemate_server.application.dto.location.LocationResponse;
import com.ridemate.ridemate_server.application.dto.location.SaveLocationRequest;
import java.util.List;

public interface LocationService {
    LocationResponse saveLocation(Long userId, SaveLocationRequest request);
    List<LocationResponse> getMyLocations(Long userId);
    void deleteLocation(Long userId, Long locationId);
}