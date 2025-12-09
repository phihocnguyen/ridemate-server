package com.ridemate.ridemate_server.presentation.controller;

import com.ridemate.ridemate_server.application.dto.location.LocationResponse;
import com.ridemate.ridemate_server.application.dto.location.SaveLocationRequest;
import com.ridemate.ridemate_server.application.service.location.LocationService;
import com.ridemate.ridemate_server.presentation.dto.response.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/locations")
@Tag(name = "Favorite Locations", description = "Manage saved addresses")
public class LocationController {

    @Autowired
    private LocationService locationService;

    @PostMapping
    @Operation(summary = "Save a location", description = "Add a new favorite location (e.g. Home, Work)")
    @SecurityRequirement(name = "bearerAuth")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "201", description = "Location saved",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = LocationResponse.class)))
    })
    public ResponseEntity<ApiResponse<LocationResponse>> saveLocation(
            @Valid @RequestBody SaveLocationRequest request,
            @AuthenticationPrincipal Long userId) {
        
        LocationResponse response = locationService.saveLocation(userId, request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Location saved successfully", response));
    }

    @GetMapping
    @Operation(summary = "Get my locations", description = "List all saved locations for current user")
    @SecurityRequirement(name = "bearerAuth")
    public ResponseEntity<ApiResponse<List<LocationResponse>>> getMyLocations(
            @AuthenticationPrincipal Long userId) {
        
        List<LocationResponse> response = locationService.getMyLocations(userId);
        return ResponseEntity.ok(ApiResponse.success("Locations retrieved successfully", response));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete a location", description = "Remove a saved location by ID")
    @SecurityRequirement(name = "bearerAuth")
    public ResponseEntity<ApiResponse<Void>> deleteLocation(
            @PathVariable Long id,
            @AuthenticationPrincipal Long userId) {
        
        locationService.deleteLocation(userId, id);
        return ResponseEntity.ok(ApiResponse.success("Location deleted successfully", null));
    }
}