package com.ridemate.ridemate_server.presentation.controller;

import com.ridemate.ridemate_server.application.dto.report.*;
import com.ridemate.ridemate_server.application.service.report.ReportManagementService;
import com.ridemate.ridemate_server.domain.entity.Report;
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
@RequestMapping("/admin/reports") // Đã thêm /api vào đầu
@Tag(name = "Report Management", description = "Admin endpoints for managing user reports")
@RequiredArgsConstructor
@SecurityRequirement(name = "Bearer Authentication")
public class ReportManagementController {

    private final ReportManagementService reportManagementService;

    @GetMapping
    @PreAuthorize("hasAuthority('ADMIN')") 
    @Operation(summary = "Get all reports with filters")
    public ResponseEntity<ApiResponse<ReportManagementPageDto>> getAllReports(
            @RequestParam(required = false) Report.ReportStatus status,
            @RequestParam(required = false) Report.ReportCategory category,
            @RequestParam(required = false) String searchTerm,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "DESC") String sortDirection
    ) {
        ReportManagementPageDto result = reportManagementService.getAllReports(
            status, category, searchTerm, page, size, sortBy, sortDirection
        );
        return ResponseEntity.ok(ApiResponse.success("Reports retrieved successfully", result));
    }

    @GetMapping("/statistics")
    @PreAuthorize("hasAuthority('ADMIN')") 
    @Operation(summary = "Get report statistics")
    public ResponseEntity<ApiResponse<ReportStatisticsDto>> getReportStatistics() {
        ReportStatisticsDto stats = reportManagementService.getReportStatistics();
        return ResponseEntity.ok(ApiResponse.success("Statistics retrieved successfully", stats));
    }

    @GetMapping("/pending")
    @PreAuthorize("hasAuthority('ADMIN')") 
    @Operation(summary = "Get pending reports")
    public ResponseEntity<ApiResponse<List<ReportManagementDto>>> getPendingReports() {
        List<ReportManagementDto> pendingReports = reportManagementService.getPendingReports();
        return ResponseEntity.ok(ApiResponse.success("Pending reports retrieved successfully", pendingReports));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('ADMIN')") 
    @Operation(summary = "Get report details")
    public ResponseEntity<ApiResponse<ReportManagementDto>> getReportById(@PathVariable Long id) {
        ReportManagementDto report = reportManagementService.getAdminReportDetail(id);
        return ResponseEntity.ok(ApiResponse.success("Report retrieved successfully", report));
    }

    @PostMapping("/{id}/process")
    @PreAuthorize("hasAuthority('ADMIN')") 
    @Operation(summary = "Mark report as processing")
    public ResponseEntity<ApiResponse<ReportManagementDto>> processReport(
            @PathVariable Long id,
            @RequestBody(required = false) ReportActionRequest request
    ) {
        String adminNotes = request != null ? request.getReason() : null;
        ReportManagementDto report = reportManagementService.processReport(id, adminNotes);
        return ResponseEntity.ok(ApiResponse.success("Report marked as processing", report));
    }

    @PostMapping("/{id}/resolve")
    @PreAuthorize("hasAuthority('ADMIN')") 
    @Operation(summary = "Resolve report with action")
    public ResponseEntity<ApiResponse<ReportManagementDto>> resolveReport(
            @PathVariable Long id,
            @Valid @RequestBody ReportActionRequest request
    ) {
        String adminUsername = "admin";
        ReportManagementDto response = reportManagementService.resolveReport(id, request, adminUsername);
        return ResponseEntity.ok(ApiResponse.success("Report resolved successfully", response));
    }

    @PostMapping("/{id}/reject")
    @PreAuthorize("hasAuthority('ADMIN')") 
    @Operation(summary = "Reject report")
    public ResponseEntity<ApiResponse<ReportManagementDto>> rejectReport(
            @PathVariable Long id,
            @Valid @RequestBody ReportActionRequest request
    ) {
        String adminUsername = "admin";
        ReportManagementDto response = reportManagementService.rejectReport(id, request.getReason(), adminUsername);
        return ResponseEntity.ok(ApiResponse.success("Report rejected", response));
    }
}