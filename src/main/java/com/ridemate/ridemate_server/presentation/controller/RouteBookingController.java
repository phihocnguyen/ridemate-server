package com.ridemate.ridemate_server.presentation.controller;

import com.ridemate.ridemate_server.application.dto.route.CreateRouteBookingRequest;
import com.ridemate.ridemate_server.application.dto.route.RouteBookingResponse;
import com.ridemate.ridemate_server.application.security.jwt.JwtTokenProvider;
import com.ridemate.ridemate_server.application.service.route.RouteBookingService;
import com.ridemate.ridemate_server.presentation.dto.response.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/route-bookings")
@Tag(name = "Route Bookings", description = "APIs for managing route bookings")
public class RouteBookingController {

    @Autowired
    private RouteBookingService routeBookingService;

    @Autowired
    private JwtTokenProvider jwtTokenProvider;

    @PostMapping
    @Operation(summary = "Create a booking request (Passenger joins a route)")
    public ResponseEntity<ApiResponse<RouteBookingResponse>> createBooking(
            @RequestHeader("Authorization") String token,
            @Valid @RequestBody CreateRouteBookingRequest request) {
        
        Long passengerId = jwtTokenProvider.getUserIdFromToken(token.substring(7));
        RouteBookingResponse response = routeBookingService.createBooking(passengerId, request);
        
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Booking request sent successfully", response));
    }

    @PostMapping("/{bookingId}/accept")
    @Operation(summary = "Accept a booking request (Driver only)")
    public ResponseEntity<ApiResponse<RouteBookingResponse>> acceptBooking(
            @RequestHeader("Authorization") String token,
            @PathVariable Long bookingId) {
        
        Long driverId = jwtTokenProvider.getUserIdFromToken(token.substring(7));
        RouteBookingResponse response = routeBookingService.acceptBooking(bookingId, driverId);
        
        return ResponseEntity.ok(ApiResponse.success("Booking accepted successfully", response));
    }

    @PostMapping("/{bookingId}/reject")
    @Operation(summary = "Reject a booking request (Driver only)")
    public ResponseEntity<ApiResponse<RouteBookingResponse>> rejectBooking(
            @RequestHeader("Authorization") String token,
            @PathVariable Long bookingId) {
        
        Long driverId = jwtTokenProvider.getUserIdFromToken(token.substring(7));
        RouteBookingResponse response = routeBookingService.rejectBooking(bookingId, driverId);
        
        return ResponseEntity.ok(ApiResponse.success("Booking rejected successfully", response));
    }

    @PostMapping("/{bookingId}/cancel")
    @Operation(summary = "Cancel a booking (Passenger only)")
    public ResponseEntity<ApiResponse<RouteBookingResponse>> cancelBooking(
            @RequestHeader("Authorization") String token,
            @PathVariable Long bookingId) {
        
        Long passengerId = jwtTokenProvider.getUserIdFromToken(token.substring(7));
        RouteBookingResponse response = routeBookingService.cancelBooking(bookingId, passengerId);
        
        return ResponseEntity.ok(ApiResponse.success("Booking cancelled successfully", response));
    }

    @GetMapping("/{bookingId}")
    @Operation(summary = "Get booking by ID")
    public ResponseEntity<ApiResponse<RouteBookingResponse>> getBookingById(
            @PathVariable Long bookingId) {
        
        RouteBookingResponse response = routeBookingService.getBookingById(bookingId);
        return ResponseEntity.ok(ApiResponse.success("Booking retrieved successfully", response));
    }

    @GetMapping("/my-bookings")
    @Operation(summary = "Get all bookings by current user (passenger or driver)")
    public ResponseEntity<ApiResponse<List<RouteBookingResponse>>> getMyBookings(
            @RequestHeader("Authorization") String token,
            @RequestParam(required = false) String role) {
        
        Long userId = jwtTokenProvider.getUserIdFromToken(token.substring(7));
        List<RouteBookingResponse> responses;
        
        if ("driver".equalsIgnoreCase(role)) {
            responses = routeBookingService.getBookingsByDriver(userId);
        } else {
            responses = routeBookingService.getBookingsByPassenger(userId);
        }
        
        return ResponseEntity.ok(ApiResponse.success("Bookings retrieved successfully", responses));
    }

    @GetMapping("/pending")
    @Operation(summary = "Get pending bookings for current driver")
    public ResponseEntity<ApiResponse<List<RouteBookingResponse>>> getPendingBookings(
            @RequestHeader("Authorization") String token) {
        
        Long driverId = jwtTokenProvider.getUserIdFromToken(token.substring(7));
        List<RouteBookingResponse> responses = routeBookingService.getPendingBookingsByDriver(driverId);
        
        return ResponseEntity.ok(ApiResponse.success("Pending bookings retrieved successfully", responses));
    }

    @GetMapping("/route/{routeId}")
    @Operation(summary = "Get all bookings for a specific route")
    public ResponseEntity<ApiResponse<List<RouteBookingResponse>>> getBookingsByRoute(
            @PathVariable Long routeId) {
        
        List<RouteBookingResponse> responses = routeBookingService.getBookingsByRoute(routeId);
        return ResponseEntity.ok(ApiResponse.success("Bookings retrieved successfully", responses));
    }

    @PostMapping("/{bookingId}/start")
    @Operation(summary = "Start trip for accepted booking (Driver only)")
    public ResponseEntity<ApiResponse<RouteBookingResponse>> startTrip(
            @RequestHeader("Authorization") String token,
            @PathVariable Long bookingId) {
        
        try {
            log.error("🚀🚀🚀 RouteBookingController.startTrip CALLED for bookingId: {} 🚀🚀🚀", bookingId);
            System.out.println("🚀🚀🚀 RouteBookingController.startTrip CALLED for bookingId: " + bookingId + " 🚀🚀🚀");
            
            Long driverId = jwtTokenProvider.getUserIdFromToken(token.substring(7));
            log.error("🚀 Driver {} starting trip for booking {}", driverId, bookingId);
            System.out.println("🚀 Driver " + driverId + " starting trip for booking " + bookingId);
            
            RouteBookingResponse response = routeBookingService.startTrip(bookingId, driverId);
            
            log.error("✅ Trip started successfully for booking {}", bookingId);
            System.out.println("✅ Trip started successfully for booking " + bookingId);
            return ResponseEntity.ok(ApiResponse.success("Trip started successfully", response));
        } catch (Exception e) {
            log.error("❌❌❌ ERROR in RouteBookingController.startTrip for bookingId {}: {}", bookingId, e.getMessage(), e);
            System.out.println("❌❌❌ ERROR in RouteBookingController.startTrip: " + e.getMessage());
            e.printStackTrace();
            throw e;
        }
    }

    @PostMapping("/{bookingId}/complete")
    @Operation(summary = "Complete trip (Driver only)")
    public ResponseEntity<ApiResponse<RouteBookingResponse>> completeTrip(
            @RequestHeader("Authorization") String token,
            @PathVariable Long bookingId) {
        
        Long driverId = jwtTokenProvider.getUserIdFromToken(token.substring(7));
        RouteBookingResponse response = routeBookingService.completeTrip(bookingId, driverId);
        
        return ResponseEntity.ok(ApiResponse.success("Trip completed successfully", response));
    }
}

