package com.ridemate.ridemate_server.presentation.controller;

import com.ridemate.ridemate_server.application.dto.membership.CreateMembershipRequest;
import com.ridemate.ridemate_server.application.dto.membership.MembershipResponse;
import com.ridemate.ridemate_server.application.dto.membership.UpdateMembershipRequest;
import com.ridemate.ridemate_server.application.service.membership.MembershipService;
import com.ridemate.ridemate_server.presentation.dto.response.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/memberships")
@Tag(name = "Membership", description = "APIs for membership packages")
@RequiredArgsConstructor
public class MembershipController {

    private final MembershipService membershipService;

    // ===== PUBLIC ENDPOINTS (No authentication required) =====
    
    @GetMapping("/active")
    @Operation(summary = "Get active membership packages", description = "Get only active membership packages (public)")
    public ResponseEntity<ApiResponse<List<MembershipResponse>>> getActiveMemberships() {
        List<MembershipResponse> memberships = membershipService.getActiveMemberships();
        return ResponseEntity.ok(ApiResponse.success("Active memberships retrieved successfully", memberships));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get membership by ID", description = "Get a specific membership package by ID (public)")
    public ResponseEntity<ApiResponse<MembershipResponse>> getMembershipById(@PathVariable Long id) {
        MembershipResponse membership = membershipService.getMembershipById(id);
        return ResponseEntity.ok(ApiResponse.success("Membership retrieved successfully", membership));
    }

    // ===== ADMIN ENDPOINTS (Require ADMIN role) =====
    
    @GetMapping("/admin/all")
    @Operation(summary = "Get all membership packages", description = "Get all membership packages (excluding deleted ones) - Admin only")
    @SecurityRequirement(name = "bearerAuth")
    @PreAuthorize("hasAuthority('ADMIN')")
    public ResponseEntity<ApiResponse<List<MembershipResponse>>> getAllMemberships() {
        List<MembershipResponse> memberships = membershipService.getAllMemberships();
        return ResponseEntity.ok(ApiResponse.success("Memberships retrieved successfully", memberships));
    }

    @PostMapping("/admin")
    @Operation(summary = "Create new membership package", description = "Create a new membership package - Admin only")
    @SecurityRequirement(name = "bearerAuth")
    @PreAuthorize("hasAuthority('ADMIN')")
    public ResponseEntity<ApiResponse<MembershipResponse>> createMembership(
            @Valid @RequestBody CreateMembershipRequest request) {
        MembershipResponse membership = membershipService.createMembership(request);
        return ResponseEntity.ok(ApiResponse.success("Membership created successfully", membership));
    }

    @PutMapping("/admin/{id}")
    @Operation(summary = "Update membership package", description = "Update an existing membership package - Admin only")
    @SecurityRequirement(name = "bearerAuth")
    @PreAuthorize("hasAuthority('ADMIN')")
    public ResponseEntity<ApiResponse<MembershipResponse>> updateMembership(
            @PathVariable Long id,
            @Valid @RequestBody UpdateMembershipRequest request) {
        MembershipResponse membership = membershipService.updateMembership(id, request);
        return ResponseEntity.ok(ApiResponse.success("Membership updated successfully", membership));
    }

    @DeleteMapping("/admin/{id}")
    @Operation(summary = "Delete membership package", description = "Soft delete a membership package - Admin only")
    @SecurityRequirement(name = "bearerAuth")
    @PreAuthorize("hasAuthority('ADMIN')")
    public ResponseEntity<ApiResponse<Void>> deleteMembership(@PathVariable Long id) {
        membershipService.deleteMembership(id);
        return ResponseEntity.ok(ApiResponse.success("Membership deleted successfully", null));
    }

    @PatchMapping("/admin/{id}/toggle-status")
    @Operation(summary = "Toggle membership status", description = "Toggle membership status between ACTIVE and PAUSED - Admin only")
    @SecurityRequirement(name = "bearerAuth")
    @PreAuthorize("hasAuthority('ADMIN')")
    public ResponseEntity<ApiResponse<MembershipResponse>> toggleStatus(@PathVariable Long id) {
        MembershipResponse membership = membershipService.toggleStatus(id);
        return ResponseEntity.ok(ApiResponse.success("Membership status toggled successfully", membership));
    }
}

