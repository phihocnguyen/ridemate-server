package com.ridemate.ridemate_server.presentation.controller;

import com.ridemate.ridemate_server.application.dto.user.*;
import com.ridemate.ridemate_server.application.service.user.UserManagementService;
import com.ridemate.ridemate_server.domain.entity.User;
import com.ridemate.ridemate_server.presentation.dto.response.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/admin/users")
@Tag(name = "User Management", description = "Admin endpoints for managing users and driver approvals")
@RequiredArgsConstructor
@SecurityRequirement(name = "bearerAuth")
public class UserManagementController {

    private final UserManagementService userManagementService;

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(
        summary = "Get all users with filters", 
        description = "Get paginated list of users with optional filters (userType, isActive, driverApprovalStatus, searchTerm)"
    )
    public ResponseEntity<ApiResponse<UserManagementPageDto>> getAllUsers(
            @Parameter(description = "Filter by user type (DRIVER, PASSENGER, ADMIN)")
            @RequestParam(required = false) User.UserType userType,
            
            @Parameter(description = "Filter by active status")
            @RequestParam(required = false) Boolean isActive,
            
            @Parameter(description = "Filter by driver approval status (NONE, PENDING, APPROVED, REJECTED)")
            @RequestParam(required = false) User.DriverApprovalStatus driverApprovalStatus,
            
            @Parameter(description = "Search by name, phone, or email")
            @RequestParam(required = false) String searchTerm,
            
            @Parameter(description = "Page number (0-indexed)")
            @RequestParam(defaultValue = "0") int page,
            
            @Parameter(description = "Page size")
            @RequestParam(defaultValue = "10") int size,
            
            @Parameter(description = "Sort by field")
            @RequestParam(defaultValue = "createdAt") String sortBy,
            
            @Parameter(description = "Sort direction (ASC or DESC)")
            @RequestParam(defaultValue = "DESC") String sortDirection
    ) {
        UserManagementPageDto result = userManagementService.getAllUsers(
            userType, isActive, driverApprovalStatus, searchTerm, 
            page, size, sortBy, sortDirection
        );
        
        return ResponseEntity.ok(ApiResponse.success("Users retrieved successfully", result));
    }

    @GetMapping("/statistics")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(
        summary = "Get user statistics", 
        description = "Get overall statistics about users, drivers, and driver approvals"
    )
    public ResponseEntity<ApiResponse<UserStatisticsDto>> getUserStatistics() {
        UserStatisticsDto stats = userManagementService.getUserStatistics();
        return ResponseEntity.ok(ApiResponse.success("Statistics retrieved successfully", stats));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(
        summary = "Get user by ID", 
        description = "Get detailed information about a specific user"
    )
    public ResponseEntity<ApiResponse<UserManagementDto>> getUserById(
            @PathVariable Long id
    ) {
        UserManagementDto user = userManagementService.getUserById(id);
        return ResponseEntity.ok(ApiResponse.success("User retrieved successfully", user));
    }

    @GetMapping("/pending-drivers")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(
        summary = "Get pending driver approvals", 
        description = "Get list of users waiting for driver approval"
    )
    public ResponseEntity<ApiResponse<List<UserManagementDto>>> getPendingDriverApprovals() {
        List<UserManagementDto> pendingDrivers = userManagementService.getPendingDriverApprovals();
        return ResponseEntity.ok(ApiResponse.success("Pending drivers retrieved successfully", pendingDrivers));
    }

    @PostMapping("/{id}/approve-driver")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(
        summary = "Approve driver application", 
        description = "Approve a user's application to become a driver"
    )
    public ResponseEntity<ApiResponse<UserManagementDto>> approveDriver(
            @PathVariable Long id
    ) {
        UserManagementDto approvedUser = userManagementService.approveDriver(id);
        return ResponseEntity.ok(ApiResponse.success("Driver application approved successfully", approvedUser));
    }

    @PostMapping("/{id}/reject-driver")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(
        summary = "Reject driver application", 
        description = "Reject a user's application to become a driver with a reason"
    )
    public ResponseEntity<ApiResponse<UserManagementDto>> rejectDriver(
            @PathVariable Long id,
            @Valid @RequestBody DriverApprovalRequest request
    ) {
        UserManagementDto rejectedUser = userManagementService.rejectDriver(id, request.getRejectionReason());
        return ResponseEntity.ok(ApiResponse.success("Driver application rejected", rejectedUser));
    }

    @PatchMapping("/{id}/status")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(
        summary = "Update user active status", 
        description = "Enable or disable a user account"
    )
    public ResponseEntity<ApiResponse<UserManagementDto>> updateUserStatus(
            @PathVariable Long id,
            @Valid @RequestBody UserStatusUpdateRequest request
    ) {
        UserManagementDto updatedUser = userManagementService.toggleUserStatus(id, request.getIsActive());
        String message = request.getIsActive() ? "User activated successfully" : "User deactivated successfully";
        return ResponseEntity.ok(ApiResponse.success(message, updatedUser));
    }
}
