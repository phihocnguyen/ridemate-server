package com.ridemate.ridemate_server.application.service.route;

import com.ridemate.ridemate_server.application.dto.route.*;
import java.util.List;

public interface FixedRouteService {
    
    /**
     * Create a new fixed route
     */
    FixedRouteResponse createRoute(Long driverId, CreateFixedRouteRequest request);
    
    /**
     * Update an existing fixed route
     */
    FixedRouteResponse updateRoute(Long routeId, Long driverId, UpdateFixedRouteRequest request);
    
    /**
     * Delete a fixed route
     */
    void deleteRoute(Long routeId, Long driverId);
    
    /**
     * Get route by ID
     */
    FixedRouteResponse getRouteById(Long routeId);
    
    /**
     * Get all routes by driver
     */
    List<FixedRouteResponse> getRoutesByDriver(Long driverId);
    
    /**
     * Get all active routes
     */
    List<FixedRouteResponse> getAllActiveRoutes();
    
    /**
     * Search for routes matching user's pickup and dropoff locations
     */
    List<FixedRouteResponse> searchRoutes(SearchFixedRoutesRequest request);
    
    /**
     * Update route status
     */
    FixedRouteResponse updateRouteStatus(Long routeId, Long driverId, String status);
}

