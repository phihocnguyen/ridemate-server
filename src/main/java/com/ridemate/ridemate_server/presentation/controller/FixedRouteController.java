package com.ridemate.ridemate_server.presentation.controller;

import com.ridemate.ridemate_server.application.dto.route.*;
import com.ridemate.ridemate_server.application.security.jwt.JwtTokenProvider;
import com.ridemate.ridemate_server.application.service.route.FixedRouteService;
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
@RequestMapping("/api/fixed-routes")
@Tag(name = "Fixed Routes", description = "APIs for managing fixed routes")
public class FixedRouteController {

    @Autowired
    private FixedRouteService fixedRouteService;

    @Autowired
    private JwtTokenProvider jwtTokenProvider;

    @PostMapping
    @Operation(summary = "Create a new fixed route (Driver only)")
    public ResponseEntity<ApiResponse<FixedRouteResponse>> createRoute(
            @RequestHeader("Authorization") String token,
            @Valid @RequestBody CreateFixedRouteRequest request) {
        
        Long driverId = jwtTokenProvider.getUserIdFromToken(token.substring(7));
        FixedRouteResponse response = fixedRouteService.createRoute(driverId, request);
        
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Fixed route created successfully", response));
    }

    @PutMapping("/{routeId}")
    @Operation(summary = "Update a fixed route (Driver only)")
    public ResponseEntity<ApiResponse<FixedRouteResponse>> updateRoute(
            @RequestHeader("Authorization") String token,
            @PathVariable Long routeId,
            @Valid @RequestBody UpdateFixedRouteRequest request) {
        
        Long driverId = jwtTokenProvider.getUserIdFromToken(token.substring(7));
        FixedRouteResponse response = fixedRouteService.updateRoute(routeId, driverId, request);
        
        return ResponseEntity.ok(ApiResponse.success("Fixed route updated successfully", response));
    }

    @DeleteMapping("/{routeId}")
    @Operation(summary = "Delete a fixed route (Driver only)")
    public ResponseEntity<ApiResponse<Void>> deleteRoute(
            @RequestHeader("Authorization") String token,
            @PathVariable Long routeId) {
        
        Long driverId = jwtTokenProvider.getUserIdFromToken(token.substring(7));
        fixedRouteService.deleteRoute(routeId, driverId);
        
        return ResponseEntity.ok(ApiResponse.success("Fixed route deleted successfully", null));
    }

    @GetMapping("/{routeId}")
    @Operation(summary = "Get route by ID")
    public ResponseEntity<ApiResponse<FixedRouteResponse>> getRouteById(@PathVariable Long routeId) {
        FixedRouteResponse response = fixedRouteService.getRouteById(routeId);
        return ResponseEntity.ok(ApiResponse.success("Route retrieved successfully", response));
    }

    @GetMapping("/my-routes")
    @Operation(summary = "Get all routes by current driver")
    public ResponseEntity<ApiResponse<List<FixedRouteResponse>>> getMyRoutes(
            @RequestHeader("Authorization") String token) {
        
        Long driverId = jwtTokenProvider.getUserIdFromToken(token.substring(7));
        List<FixedRouteResponse> responses = fixedRouteService.getRoutesByDriver(driverId);
        
        return ResponseEntity.ok(ApiResponse.success("Routes retrieved successfully", responses));
    }

    @GetMapping
    @Operation(summary = "Get all active routes")
    public ResponseEntity<ApiResponse<List<FixedRouteResponse>>> getAllActiveRoutes() {
        List<FixedRouteResponse> responses = fixedRouteService.getAllActiveRoutes();
        return ResponseEntity.ok(ApiResponse.success("Routes retrieved successfully", responses));
    }

    @PostMapping("/search")
    @Operation(summary = "Search for routes matching pickup and dropoff locations")
    public ResponseEntity<ApiResponse<List<FixedRouteResponse>>> searchRoutes(
            @Valid @RequestBody SearchFixedRoutesRequest request) {
        
        List<FixedRouteResponse> responses = fixedRouteService.searchRoutes(request);
        return ResponseEntity.ok(ApiResponse.success(
                String.format("Found %d matching routes", responses.size()), responses));
    }

    @PatchMapping("/{routeId}/status")
    @Operation(summary = "Update route status (Driver only)")
    public ResponseEntity<ApiResponse<FixedRouteResponse>> updateRouteStatus(
            @RequestHeader("Authorization") String token,
            @PathVariable Long routeId,
            @RequestParam String status) {
        
        Long driverId = jwtTokenProvider.getUserIdFromToken(token.substring(7));
        FixedRouteResponse response = fixedRouteService.updateRouteStatus(routeId, driverId, status);
        
        return ResponseEntity.ok(ApiResponse.success("Route status updated successfully", response));
    }
}

