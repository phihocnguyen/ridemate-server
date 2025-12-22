package com.ridemate.ridemate_server.presentation.controller;

import com.ridemate.ridemate_server.application.dto.location.UpdateLocationRequest;
import com.ridemate.ridemate_server.application.service.driver.DriverLocationService;
import com.ridemate.ridemate_server.application.service.driver.SupabaseRealtimeService;
import com.ridemate.ridemate_server.presentation.dto.response.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/driver/location")
@Tag(name = "Driver Location", description = "Real-time driver location tracking")
@RequiredArgsConstructor
public class DriverLocationController {

    private final DriverLocationService driverLocationService;
    private final SupabaseRealtimeService supabaseRealtimeService;

    @PostMapping
    @Operation(summary = "Update driver location", description = "Update current driver location (called from driver app)")
    @SecurityRequirement(name = "bearerAuth")
    public ResponseEntity<ApiResponse<Void>> updateLocation(
            @Valid @RequestBody UpdateLocationRequest request,
            @AuthenticationPrincipal Long userId) {

        driverLocationService.updateDriverLocation(
                userId,
                request.getLatitude(),
                request.getLongitude()
        );

        return ResponseEntity.ok(ApiResponse.success("Location updated successfully", null));
    }

    @GetMapping("/nearby")
    @Operation(summary = "Get nearby drivers", description = "Get all online drivers within specified radius")
    @SecurityRequirement(name = "bearerAuth")
    public ResponseEntity<ApiResponse<List<Map<String, Object>>>> getNearbyDrivers(
            @RequestParam Double latitude,
            @RequestParam Double longitude,
            @RequestParam(defaultValue = "7.0") Double radiusKm) {

        List<Map<String, Object>> drivers = supabaseRealtimeService
                .getNearbyDrivers(latitude, longitude, radiusKm)
                .block(); // Block to convert Mono to synchronous
        
        return ResponseEntity.ok(
                ApiResponse.success("Nearby drivers retrieved successfully", drivers)
        );
    }

    @GetMapping("/online")
    @Operation(summary = "Get all online drivers", description = "Get list of all online drivers with their locations")
    @SecurityRequirement(name = "bearerAuth")
    public ResponseEntity<ApiResponse<List<Map<String, Object>>>> getOnlineDrivers() {
        
        List<Map<String, Object>> onlineDrivers = driverLocationService.getOnlineDriversWithLocations();
        return ResponseEntity.ok(ApiResponse.success("Online drivers retrieved successfully", onlineDrivers));
    }

    @PostMapping("/status")
    @Operation(summary = "Set driver online/offline status", description = "Update driver availability status")
    @SecurityRequirement(name = "bearerAuth")
    public ResponseEntity<ApiResponse<Void>> setDriverStatus(
            @RequestParam String status,
            @AuthenticationPrincipal Long userId) {

        driverLocationService.setDriverOnlineStatus(userId, status);
        return ResponseEntity.ok(ApiResponse.success("Driver status updated to " + status, null));
    }
}
