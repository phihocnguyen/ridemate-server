package com.ridemate.ridemate_server.presentation.controller;

import com.ridemate.ridemate_server.application.dto.user.DriverApplicationRequest;
import com.ridemate.ridemate_server.application.dto.user.UserManagementDto;
import com.ridemate.ridemate_server.application.service.user.DriverApplicationService;
import com.ridemate.ridemate_server.presentation.dto.response.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/users/driver-application")
@Tag(name = "Driver Application", description = "Endpoints for users to apply to become drivers")
@RequiredArgsConstructor
@SecurityRequirement(name = "Bearer Authentication")
public class DriverApplicationController {

    private final DriverApplicationService driverApplicationService;

    @PostMapping("/apply")
    @PreAuthorize("hasAnyRole('USER', 'PASSENGER')")
    @Operation(
        summary = "Apply to become a driver", 
        description = "Passenger users can submit an application to become a driver by providing license and vehicle information"
    )
    public ResponseEntity<ApiResponse<UserManagementDto>> applyToBeDriver(
            @AuthenticationPrincipal Long userId,
            @Valid @RequestBody DriverApplicationRequest request
    ) {
        UserManagementDto result = driverApplicationService.applyToBeDriver(userId, request);
        return ResponseEntity.ok(ApiResponse.success("Driver application submitted successfully", result));
    }

    @GetMapping("/status")
    @PreAuthorize("hasAnyRole('USER', 'PASSENGER', 'DRIVER')")
    @Operation(
        summary = "Get driver application status", 
        description = "Get the current status of the user's driver application"
    )
    public ResponseEntity<ApiResponse<UserManagementDto>> getApplicationStatus(
            @AuthenticationPrincipal Long userId
    ) {
        UserManagementDto status = driverApplicationService.getApplicationStatus(userId);
        return ResponseEntity.ok(ApiResponse.success("Application status retrieved successfully", status));
    }
}
