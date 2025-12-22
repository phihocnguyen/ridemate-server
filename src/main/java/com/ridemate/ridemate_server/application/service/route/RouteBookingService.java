package com.ridemate.ridemate_server.application.service.route;

import com.ridemate.ridemate_server.application.dto.route.CreateRouteBookingRequest;
import com.ridemate.ridemate_server.application.dto.route.RouteBookingResponse;
import java.util.List;

public interface RouteBookingService {
    
    /**
     * Create a booking request (passenger joins a route)
     */
    RouteBookingResponse createBooking(Long passengerId, CreateRouteBookingRequest request);
    
    /**
     * Accept a booking request (driver accepts passenger)
     */
    RouteBookingResponse acceptBooking(Long bookingId, Long driverId);
    
    /**
     * Reject a booking request (driver rejects passenger)
     */
    RouteBookingResponse rejectBooking(Long bookingId, Long driverId);
    
    /**
     * Cancel a booking (passenger cancels)
     */
    RouteBookingResponse cancelBooking(Long bookingId, Long passengerId);
    
    /**
     * Get booking by ID
     */
    RouteBookingResponse getBookingById(Long bookingId);
    
    /**
     * Get all bookings by passenger
     */
    List<RouteBookingResponse> getBookingsByPassenger(Long passengerId);
    
    /**
     * Get all bookings by driver
     */
    List<RouteBookingResponse> getBookingsByDriver(Long driverId);
    
    /**
     * Get pending bookings for driver
     */
    List<RouteBookingResponse> getPendingBookingsByDriver(Long driverId);
    
    /**
     * Get bookings for a specific route
     */
    List<RouteBookingResponse> getBookingsByRoute(Long routeId);
    
    /**
     * Start trip for accepted bookings (creates Match and Session)
     */
    RouteBookingResponse startTrip(Long bookingId, Long driverId);
    
    /**
     * Complete trip
     */
    RouteBookingResponse completeTrip(Long bookingId, Long driverId);
}

