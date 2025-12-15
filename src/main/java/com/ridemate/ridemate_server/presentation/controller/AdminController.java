package com.ridemate.ridemate_server.presentation.controller;

import com.ridemate.ridemate_server.application.service.admin.AdminService;
import com.ridemate.ridemate_server.application.service.mission.MissionService;
import com.ridemate.ridemate_server.domain.entity.Match.MatchStatus;
import com.ridemate.ridemate_server.domain.entity.Mission;
import com.ridemate.ridemate_server.presentation.dto.admin.*;
import com.ridemate.ridemate_server.presentation.dto.mission.CreateMissionRequest;
import com.ridemate.ridemate_server.presentation.dto.mission.MissionDto;
import com.ridemate.ridemate_server.presentation.dto.mission.UpdateMissionRequest;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/admin")
@Tag(name = "Admin Management", description = "Admin management endpoints")
@RequiredArgsConstructor
@SecurityRequirement(name = "Bearer Authentication")
public class AdminController {

    private final AdminService adminService;
    private final MissionService missionService;

    @GetMapping("/dashboard/stats")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Get dashboard statistics", description = "Get overall statistics for admin dashboard")
    public ResponseEntity<AdminDashboardStatsDto> getDashboardStats() {
        return ResponseEntity.ok(adminService.getDashboardStats());
    }

    @GetMapping("/dashboard/charts")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Get chart data", description = "Get chart data for various metrics (users, sessions, messages, vouchers, revenue)")
    public ResponseEntity<AdminChartDataDto> getChartData(
            @Parameter(description = "Chart type: users, sessions, messages, vouchers, revenue")
            @RequestParam(defaultValue = "users") String type) {
        return ResponseEntity.ok(adminService.getChartData(type));
    }

    @GetMapping("/dashboard/stats/trips")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Get trip statistics", description = "Get detailed statistics about trips (pending, matched, in-progress, completed, cancelled)")
    public ResponseEntity<TripStatsDto> getTripStats() {
        return ResponseEntity.ok(adminService.getTripStats());
    }

    @GetMapping("/dashboard/active-trips")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Get active trips", description = "Get list of currently active trips (matched or in-progress)")
    public ResponseEntity<List<ActiveTripDto>> getActiveTrips() {
        return ResponseEntity.ok(adminService.getActiveTrips());
    }

    @GetMapping("/dashboard/top-users")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Get top users", description = "Get top users by coins/points and rating")
    public ResponseEntity<List<TopUserDto>> getTopUsers(
            @Parameter(description = "Number of top users to return")
            @RequestParam(defaultValue = "10") int limit) {
        return ResponseEntity.ok(adminService.getTopUsers(limit));
    }

    @GetMapping("/dashboard/stats/membership")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Get membership statistics", description = "Get membership tier distribution and percentages")
    public ResponseEntity<MembershipStatsDto> getMembershipStats() {
        return ResponseEntity.ok(adminService.getMembershipStats());
    }

    @GetMapping("/dashboard/stats/revenue")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Get revenue statistics", description = "Get revenue statistics (daily, weekly, monthly, by date, top vouchers)")
    public ResponseEntity<RevenueStatsDto> getRevenueStats() {
        return ResponseEntity.ok(adminService.getRevenueStats());
    }
    
    // Trip Management endpoints
    @GetMapping("/trips")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Get all trips", description = "Get paginated list of trips with optional status filter")
    public ResponseEntity<Page<TripManagementDto>> getAllTrips(
            @Parameter(description = "Filter by status: PENDING, WAITING, ACCEPTED, IN_PROGRESS, COMPLETED, CANCELLED")
            @RequestParam(required = false) com.ridemate.ridemate_server.domain.entity.Match.MatchStatus status,
            @Parameter(description = "Search term for driver/passenger name or phone")
            @RequestParam(required = false) String search,
            @Parameter(description = "Page number (0-indexed)")
            @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Page size")
            @RequestParam(defaultValue = "20") int size,
            @Parameter(description = "Sort by field (e.g., 'createdAt', 'id')")
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @Parameter(description = "Sort direction: ASC or DESC")
            @RequestParam(defaultValue = "DESC") String sortDirection) {
        
        Pageable pageable = PageRequest.of(
                page,
                size,
                Sort.Direction.fromString(sortDirection),
                sortBy
        );
        
        return ResponseEntity.ok(adminService.getAllTrips(status, search, pageable));
    }
    
    @GetMapping("/trips/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Get trip by ID", description = "Get detailed information about a specific trip")
    public ResponseEntity<TripManagementDto> getTripById(
            @Parameter(description = "Trip ID")
            @PathVariable Long id) {
        return ResponseEntity.ok(adminService.getTripById(id));
    }
    
    // ==================== Mission Management ====================
    
    @PostMapping("/missions")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Create new mission", description = "Create a new mission for users")
    public ResponseEntity<MissionDto> createMission(@Valid @RequestBody CreateMissionRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(missionService.createMission(request));
    }
    
    @PutMapping("/missions/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Update mission", description = "Update an existing mission")
    public ResponseEntity<MissionDto> updateMission(
            @PathVariable Long id,
            @Valid @RequestBody UpdateMissionRequest request) {
        return ResponseEntity.ok(missionService.updateMission(id, request));
    }
    
    @DeleteMapping("/missions/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Delete mission", description = "Delete a mission")
    public ResponseEntity<Void> deleteMission(@PathVariable Long id) {
        missionService.deleteMission(id);
        return ResponseEntity.noContent().build();
    }
    
    @GetMapping("/missions")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Get all missions", description = "Get all missions with pagination and filtering")
    public ResponseEntity<Page<MissionDto>> getAllMissions(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "DESC") String sortDirection,
            @RequestParam(required = false) String type) {
        
        Pageable pageable = PageRequest.of(page, size, 
                Sort.Direction.fromString(sortDirection), sortBy);
        
        if (type != null && !type.isEmpty()) {
            Mission.MissionType missionType = Mission.MissionType.valueOf(type.toUpperCase());
            return ResponseEntity.ok(missionService.getMissionsByType(missionType, pageable));
        }
        
        return ResponseEntity.ok(missionService.getAllMissions(pageable));
    }
    
    @GetMapping("/missions/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Get mission by ID", description = "Get detailed information about a specific mission")
    public ResponseEntity<MissionDto> getMissionById(@PathVariable Long id) {
        return ResponseEntity.ok(missionService.getMissionById(id));
    }
    
    @GetMapping("/missions/stats")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Get mission statistics", description = "Get statistics about missions")
    public ResponseEntity<Map<String, Long>> getMissionStats() {
        Long activeMissions = missionService.getActiveMissionCount();
        return ResponseEntity.ok(Map.of("activeMissions", activeMissions));
    }
}
