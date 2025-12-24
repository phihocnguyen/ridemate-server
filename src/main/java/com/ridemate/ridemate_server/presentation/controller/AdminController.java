package com.ridemate.ridemate_server.presentation.controller;

import com.ridemate.ridemate_server.application.dto.admin.ActiveTripResponse;
import com.ridemate.ridemate_server.application.dto.admin.DashboardStatsResponse;
import com.ridemate.ridemate_server.application.dto.admin.DriverRankingResponse;
import com.ridemate.ridemate_server.application.dto.admin.TripDetailResponse;
import com.ridemate.ridemate_server.application.dto.admin.TripVolumeResponse;
import com.ridemate.ridemate_server.application.dto.admin.VoucherStatsResponse;
import com.ridemate.ridemate_server.application.dto.admin.MembershipStatsResponse;
import com.ridemate.ridemate_server.application.service.admin.AdminDashboardService;
import com.ridemate.ridemate_server.application.service.admin.AdminDriverAnalyticsService;
import com.ridemate.ridemate_server.application.service.admin.AdminRealTimeMonitoringService;
import com.ridemate.ridemate_server.application.service.admin.AdminTripAnalyticsService;
import com.ridemate.ridemate_server.application.service.admin.AdminTripDetailService;
import com.ridemate.ridemate_server.application.service.admin.AdminVoucherAndMembershipService;
import com.ridemate.ridemate_server.application.service.admin.AdminService;
import com.ridemate.ridemate_server.application.service.mission.MissionService;
import com.ridemate.ridemate_server.application.service.user.impl.UserSyncService;
import com.ridemate.ridemate_server.domain.entity.Match.MatchStatus;
import com.ridemate.ridemate_server.presentation.dto.admin.*;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/admin")
@Tag(name = "Admin Management", description = "Admin management endpoints")
@RequiredArgsConstructor
@SecurityRequirement(name = "bearerAuth")
public class AdminController {

    private final AdminService adminService;
    private final AdminDashboardService adminDashboardService;
    private final AdminDriverAnalyticsService driverAnalyticsService;
    private final AdminTripAnalyticsService tripAnalyticsService;
    private final AdminRealTimeMonitoringService realTimeMonitoringService;
    private final AdminTripDetailService tripDetailService;
    private final AdminVoucherAndMembershipService voucherAndMembershipService;
    private final MissionService missionService;
    private final UserSyncService userSyncService;

    @GetMapping("/trips")
    @PreAuthorize("hasAuthority('ADMIN')")
    @Operation(summary = "Get trips list", description = "Get paginated list of trips with filters")
    public ResponseEntity<Page<TripManagementDto>> getAllTrips(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "DESC") String sortDirection,
            @RequestParam(required = false) String search,
            @RequestParam(required = false) String status
    ) {
        Sort.Direction direction = sortDirection.equalsIgnoreCase("ASC") ? Sort.Direction.ASC : Sort.Direction.DESC;
        Pageable pageable = PageRequest.of(page, size, Sort.by(direction, sortBy));

        MatchStatus matchStatus = null;
        if (status != null && !status.isEmpty() && !status.equalsIgnoreCase("all")) {
            try {
                matchStatus = MatchStatus.valueOf(status.toUpperCase());
            } catch (IllegalArgumentException e) {
                matchStatus = null;
            }
        }

        return ResponseEntity.ok(adminService.getAllTrips(matchStatus, search, pageable));
    }

    @GetMapping("/dashboard/stats")
    @PreAuthorize("hasAuthority('ADMIN')")
    @Operation(summary = "Get dashboard statistics", description = "Get overall statistics for admin dashboard")
    public ResponseEntity<AdminDashboardStatsDto> getDashboardStats() {
        return ResponseEntity.ok(adminService.getDashboardStats());
    }

    @GetMapping("/dashboard/stats/trips")
    @PreAuthorize("hasAuthority('ADMIN')")
    @Operation(summary = "Get trip statistics", description = "Get specific trip statistics for dashboard")
    public ResponseEntity<TripStatsDto> getTripStats() {
        return ResponseEntity.ok(adminService.getTripStats());
    }

    @GetMapping("/dashboard/charts")
    @PreAuthorize("hasAuthority('ADMIN')")
    @Operation(summary = "Get chart data", description = "Get chart data for various metrics (users, sessions, messages, vouchers, revenue)")
    public ResponseEntity<AdminChartDataDto> getChartData(
            @Parameter(description = "Chart type: users, sessions, messages, vouchers, revenue")
            @RequestParam(defaultValue = "users") String type) {
        return ResponseEntity.ok(adminService.getChartData(type));
    }

    @GetMapping("/dashboard/comprehensive")
    @Operation(summary = "Get comprehensive dashboard statistics")
    public ResponseEntity<DashboardStatsResponse> getComprehensiveDashboard() {
        return ResponseEntity.ok(adminDashboardService.getComprehensiveDashboardStats());
    }

    @GetMapping("/drivers/rankings")
    @Operation(summary = "Get top drivers by coins earned")
    public ResponseEntity<List<DriverRankingResponse>> getTopDrivers(
            @RequestParam(defaultValue = "10") int limit
    ) {
        return ResponseEntity.ok(driverAnalyticsService.getTopDrivers(limit));
    }

    @GetMapping("/trips/volume")
    @Operation(summary = "Get trip volume by date range")
    public ResponseEntity<List<TripVolumeResponse>> getTripVolume(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate
    ) {
        return ResponseEntity.ok(tripAnalyticsService.getTripVolumeByDateRange(startDate, endDate));
    }

    @GetMapping("/trips/active")
    @Operation(summary = "Get all active trips")
    public ResponseEntity<List<ActiveTripResponse>> getActiveTrips() {
        return ResponseEntity.ok(realTimeMonitoringService.getActiveTrips());
    }

    @GetMapping("/trips/{tripId}")
    @Operation(summary = "Get trip detail by ID")
    public ResponseEntity<TripDetailResponse> getTripDetail(@PathVariable Long tripId) {
        return ResponseEntity.ok(tripDetailService.getTripDetail(tripId));
    }

    @GetMapping("/trips/status/completed")
    @Operation(summary = "Get all completed trips with details")
    public ResponseEntity<List<TripDetailResponse>> getCompletedTrips() {
        return ResponseEntity.ok(tripDetailService.getCompletedTripsDetailed());
    }

    @GetMapping("/trips/status/cancelled")
    @Operation(summary = "Get all cancelled trips with details")
    public ResponseEntity<List<TripDetailResponse>> getCancelledTrips() {
        return ResponseEntity.ok(tripDetailService.getCancelledTripsDetailed());
    }

    @GetMapping("/vouchers/statistics")
    @Operation(summary = "Get voucher usage statistics")
    public ResponseEntity<List<VoucherStatsResponse>> getVoucherStats() {
        return ResponseEntity.ok(voucherAndMembershipService.getVoucherStatistics());
    }

    @GetMapping("/memberships/statistics")
    @Operation(summary = "Get membership tier statistics")
    public ResponseEntity<List<MembershipStatsResponse>> getMembershipStats() {
        return ResponseEntity.ok(voucherAndMembershipService.getMembershipStatistics());
    }

    @PostMapping("/users/{userId}/sync-stats")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Sync user ride statistics", description = "Sync totalRidesCompleted with actual COMPLETED matches")
    public ResponseEntity<Map<String, String>> syncUserStats(@PathVariable Long userId) {
        userSyncService.syncUserRideStats(userId);
        return ResponseEntity.ok(Map.of("message", "User stats synced successfully"));
    }

    @PostMapping("/users/sync-all-stats")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Sync all users' ride statistics", description = "Sync totalRidesCompleted for all users (use with caution)")
    public ResponseEntity<Map<String, String>> syncAllUsersStats() {
        userSyncService.syncAllUsersRideStats();
        return ResponseEntity.ok(Map.of("message", "All users' stats synced successfully"));
    }
}