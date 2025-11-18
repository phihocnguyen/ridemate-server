package com.ridemate.ridemate_server.presentation.controller;

import com.ridemate.ridemate_server.application.dto.vehicle.RegisterVehicleRequest;
import com.ridemate.ridemate_server.application.dto.vehicle.UpdateVehicleStatusRequest;
import com.ridemate.ridemate_server.application.dto.vehicle.VehicleResponse;
import com.ridemate.ridemate_server.application.service.vehicle.VehicleService;
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
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/vehicles")
@Tag(name = "Vehicles", description = "Vehicle registration and management endpoints")
public class VehicleController {

    @Autowired
    private VehicleService vehicleService;

    @PostMapping("/register")
    @Operation(
            summary = "Register a new vehicle",
            description = "Register a new vehicle with registration document URL. " +
                    "First, upload the registration document using POST /upload/image endpoint to get the URL. " +
                    "Then use that URL in registrationDocumentUrl field. Vehicle will be in PENDING status until admin approval."
    )
    @SecurityRequirement(name = "bearerAuth")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "201",
                    description = "Vehicle registered successfully",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = VehicleResponse.class))
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "400",
                    description = "Invalid input or license plate already exists"
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "401",
                    description = "Unauthorized"
            )
    })
    public ResponseEntity<ApiResponse<VehicleResponse>> registerVehicle(
            @Valid @RequestBody RegisterVehicleRequest request,
            Authentication authentication) {
        
        Long driverId = getUserIdFromAuthentication(authentication);
        VehicleResponse response = vehicleService.registerVehicle(driverId, request);
        
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(HttpStatus.CREATED.value(), 
                        "Vehicle registered successfully. Waiting for admin approval.", response));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get vehicle by ID", description = "Retrieve vehicle information by ID")
    @SecurityRequirement(name = "bearerAuth")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "Vehicle found",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = VehicleResponse.class))
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "404",
                    description = "Vehicle not found"
            )
    })
    public ResponseEntity<ApiResponse<VehicleResponse>> getVehicleById(@PathVariable Long id) {
        VehicleResponse response = vehicleService.getVehicleById(id);
        return ResponseEntity.ok(ApiResponse.success("Vehicle retrieved successfully", response));
    }

    @GetMapping("/my-vehicle")
    @Operation(summary = "Get my vehicle", description = "Get the current driver's vehicle")
    @SecurityRequirement(name = "bearerAuth")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "Vehicle found",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = VehicleResponse.class))
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "404",
                    description = "No vehicle found for this driver"
            )
    })
    public ResponseEntity<ApiResponse<VehicleResponse>> getMyVehicle(Authentication authentication) {
        Long driverId = getUserIdFromAuthentication(authentication);
        VehicleResponse response = vehicleService.getMyVehicle(driverId);
        return ResponseEntity.ok(ApiResponse.success("Vehicle retrieved successfully", response));
    }

    @GetMapping("/driver/{driverId}")
    @Operation(summary = "Get vehicles by driver", description = "Get all vehicles registered by a specific driver")
    @SecurityRequirement(name = "bearerAuth")
    public ResponseEntity<ApiResponse<List<VehicleResponse>>> getVehiclesByDriver(@PathVariable Long driverId) {
        List<VehicleResponse> response = vehicleService.getVehiclesByDriver(driverId);
        return ResponseEntity.ok(ApiResponse.success("Vehicles retrieved successfully", response));
    }

    @GetMapping("/pending")
    @Operation(summary = "Get pending vehicles", description = "Get all vehicles waiting for admin approval (Admin only)")
    @SecurityRequirement(name = "bearerAuth")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "Pending vehicles retrieved successfully"
            )
    })
    public ResponseEntity<ApiResponse<List<VehicleResponse>>> getPendingVehicles() {
        List<VehicleResponse> response = vehicleService.getPendingVehicles();
        return ResponseEntity.ok(ApiResponse.success("Pending vehicles retrieved successfully", response));
    }

    @PutMapping("/{id}/status")
    @Operation(summary = "Update vehicle status", description = "Update vehicle status (APPROVED, REJECTED, INACTIVE) - Admin only")
    @SecurityRequirement(name = "bearerAuth")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "Vehicle status updated successfully",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = VehicleResponse.class))
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "400",
                    description = "Invalid status or status transition"
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "404",
                    description = "Vehicle not found"
            )
    })
    public ResponseEntity<ApiResponse<VehicleResponse>> updateVehicleStatus(
            @PathVariable Long id,
            @Valid @RequestBody UpdateVehicleStatusRequest request) {
        VehicleResponse response = vehicleService.updateVehicleStatus(id, request);
        return ResponseEntity.ok(ApiResponse.success("Vehicle status updated successfully", response));
    }

    private Long getUserIdFromAuthentication(Authentication authentication) {
        if (authentication == null || authentication.getPrincipal() == null) {
            throw new IllegalStateException("User not authenticated");
        }
        return (Long) authentication.getPrincipal();
    }
}

