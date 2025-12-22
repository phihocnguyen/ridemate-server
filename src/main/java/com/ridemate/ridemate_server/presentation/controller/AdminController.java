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
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
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

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/admin")
@Tag(name = "Admin Management", description = "Admin management endpoints")
@RequiredArgsConstructor
@SecurityRequirement(name = "Bearer Authentication")
public class AdminController {

    private final AdminService adminService;
    private final AdminDashboardService adminDashboardService;
    private final AdminDriverAnalyticsService driverAnalyticsService;
    private final AdminTripAnalyticsService tripAnalyticsService;
    private final AdminRealTimeMonitoringService realTimeMonitoringService;
    private final AdminTripDetailService tripDetailService;
    private final AdminVoucherAndMembershipService voucherAndMembershipService;
    private final MissionService missionService;

    @GetMapping("/dashboard/stats")
    @PreAuthorize("hasAuthority('ADMIN')") // <--- Đã sửa: dùng hasAuthority
    @Operation(summary = "Get dashboard statistics", description = "Get overall statistics for admin dashboard")
    public ResponseEntity<AdminDashboardStatsDto> getDashboardStats() {
        return ResponseEntity.ok(adminService.getDashboardStats());
    }

    @GetMapping("/dashboard/charts")
    @PreAuthorize("hasAuthority('ADMIN')") // <--- Đã sửa
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
}
