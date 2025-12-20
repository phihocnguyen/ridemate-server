package com.ridemate.ridemate_server.presentation.controller;

import com.ridemate.ridemate_server.application.dto.user.UpdateDriverStatusRequest;
import com.ridemate.ridemate_server.application.dto.user.UpdateProfileRequest;
import com.ridemate.ridemate_server.application.dto.user.UserDto;
import com.ridemate.ridemate_server.application.service.user.UserService;
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
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/users")
@Tag(name = "Users", description = "User management endpoints")
public class UserController {

    @Autowired
    private UserService userService;

    @GetMapping("/me")
    @Operation(summary = "Get current user profile", description = "Retrieve the profile of the currently authenticated user")
    @SecurityRequirement(name = "Bearer Authentication")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "User profile retrieved successfully",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = UserDto.class))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Unauthorized - Invalid or missing JWT token"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "User not found")
    })
    public ResponseEntity<ApiResponse<UserDto>> getMyProfile(@AuthenticationPrincipal Long userId) {
        if (userId == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ApiResponse.error(HttpStatus.UNAUTHORIZED.value(), "Invalid token"));
        }
        
        UserDto userDto = userService.getUserProfile(userId);
        return ResponseEntity.ok(ApiResponse.success("Profile retrieved successfully", userDto));
    }

    @PatchMapping
    @Operation(summary = "Update user profile", description = "Update current user's profile information (partial update)")
    @SecurityRequirement(name = "Bearer Authentication")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Profile updated successfully",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = UserDto.class))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Invalid request data"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Unauthorized - Invalid or missing JWT token"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "User not found")
    })
    public ResponseEntity<ApiResponse<UserDto>> updateProfile(
            @Valid @RequestBody UpdateProfileRequest request,
            @AuthenticationPrincipal Long userId) {
        
        System.out.println("=== UPDATE PROFILE DEBUG ===");
        System.out.println("User ID: " + userId);
        System.out.println("Request: " + request);
        
        if (userId == null) {
            System.out.println("ERROR: userId is null");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ApiResponse.error(HttpStatus.UNAUTHORIZED.value(), "Invalid token"));
        }
        
        try {
            UserDto updatedUser = userService.updateProfile(userId, request);
            System.out.println("SUCCESS: Profile updated");
            return ResponseEntity.ok(ApiResponse.success("Profile updated successfully", updatedUser));
        } catch (Exception e) {
            System.out.println("ERROR: " + e.getMessage());
            e.printStackTrace();
            throw e;
        }
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    @Operation(summary = "Get user by ID", description = "Retrieve user information by ID (requires authentication)")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "User found",
                    content = @Content(mediaType = "application/json")),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Unauthorized - Invalid or missing JWT token"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Forbidden - Insufficient permissions"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "User not found")
    })
    public ResponseEntity<ApiResponse<String>> getUserById(@PathVariable Long id) {
        
        
        if (id == null || id <= 0) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ApiResponse.error(HttpStatus.NOT_FOUND.value(), "User not found"));
        }

        return ResponseEntity.ok(ApiResponse.success("User retrieved successfully", 
                "User with ID: " + id));
    }

    @GetMapping("/health")
    @Operation(summary = "Health check", description = "Check if API is running")
    public ResponseEntity<ApiResponse<String>> health() {
        return ResponseEntity.ok(ApiResponse.success(200, "API is running", "OK"));
    }

    @PostMapping("/driver/status")
    @Operation(summary = "Update driver status and location", 
               description = "Drivers use this to go ONLINE/OFFLINE and update their current location for matching algorithm")
    @SecurityRequirement(name = "bearerAuth")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Driver status updated successfully"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Invalid request"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Unauthorized"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Only drivers can update driver status")
    })
    public ResponseEntity<ApiResponse<UserDto>> updateDriverStatus(
            @Valid @RequestBody UpdateDriverStatusRequest request,
            @AuthenticationPrincipal Long userId) {
        
        UserDto updatedUser = userService.updateDriverStatus(userId, request);
        return ResponseEntity.ok(ApiResponse.success("Driver status updated successfully", updatedUser));
    }

    @PutMapping("/driver/location")
    @Operation(summary = "Update driver current location", 
               description = "Driver sends periodic location updates while ONLINE (for matching algorithm)")
    @SecurityRequirement(name = "bearerAuth")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Location updated successfully"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Invalid request"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Unauthorized"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Only drivers can update location")
    })
    public ResponseEntity<ApiResponse<UserDto>> updateDriverLocation(
            @RequestParam Double latitude,
            @RequestParam Double longitude,
            @AuthenticationPrincipal Long userId) {
        
        UpdateDriverStatusRequest request = UpdateDriverStatusRequest.builder()
                .latitude(latitude)
                .longitude(longitude)
                .build();
        
        UserDto updatedUser = userService.updateDriverStatus(userId, request);
        return ResponseEntity.ok(ApiResponse.success("Location updated successfully", updatedUser));
    }
}