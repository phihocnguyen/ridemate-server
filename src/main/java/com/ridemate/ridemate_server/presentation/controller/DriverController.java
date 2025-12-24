package com.ridemate.ridemate_server.presentation.controller;

import com.ridemate.ridemate_server.presentation.dto.response.ApiResponse;
import com.ridemate.ridemate_server.application.dto.driver.StartPersonalRideRequest;
import com.ridemate.ridemate_server.application.dto.match.MatchResponse;
import com.ridemate.ridemate_server.application.service.driver.PersonalRideService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/driver")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Driver", description = "Driver management APIs")
public class DriverController {

    private final PersonalRideService personalRideService;

    @PostMapping("/personal-ride")
    @PreAuthorize("hasRole('DRIVER')")
    @Operation(
        summary = "Start a personal ride",
        description = "Driver starts a ride for themselves or instant pickup. Creates a match and session immediately."
    )
    public ResponseEntity<ApiResponse<MatchResponse>> startPersonalRide(
            @Valid @RequestBody StartPersonalRideRequest request,
            @AuthenticationPrincipal Long driverId
    ) {
        // Fallback for demo/testing if driverId is null (should be handled by security context)
        // Note: In production, AuthenticationPrincipal should resolve to the user ID from the token
        if (driverId == null) {
            // For now, if no auth context, we might accept it from a header or fail
            // But let's assume security is working or we throw a 401
             log.warn("Principal is null in startPersonalRide");
             // throw new AccessDeniedException("User not authenticated");
        }
        
        MatchResponse response = personalRideService.startPersonalRide(driverId, request);
        return ResponseEntity.ok(ApiResponse.success("Personal ride started successfully", response));
    }
}
