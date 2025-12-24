package com.ridemate.ridemate_server.presentation.controller;

import com.ridemate.ridemate_server.application.dto.report.ReportActionRequest;
import com.ridemate.ridemate_server.application.dto.report.ReportManagementDto;
import com.ridemate.ridemate_server.application.dto.report.ReportManagementPageDto;
import com.ridemate.ridemate_server.application.dto.report.ReportStatisticsDto;
import com.ridemate.ridemate_server.application.service.report.ReportManagementService;
import com.ridemate.ridemate_server.domain.entity.Report;
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
@RequestMapping("/admin/reports")
@Tag(name = "Report Management", description = "Admin endpoints for managing user reports")
@RequiredArgsConstructor
@SecurityRequirement(name = "bearerAuth")
public class ReportManagementController {

    private final ReportManagementService reportManagementService;

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(
        summary = "Get all reports with filters",
        description = "Get paginated list of reports with optional filters (status, category, searchTerm)"
    )
    public ResponseEntity<ApiResponse<ReportManagementPageDto>> getAllReports(
            @Parameter(description = "Filter by status (PENDING, PROCESSING, RESOLVED, REJECTED)")
            @RequestParam(required = false) Report.ReportStatus status,

            @Parameter(description = "Filter by category (SAFETY, BEHAVIOR, LOST_ITEM, PAYMENT, APP_ISSUE, OTHER)")
            @RequestParam(required = false) Report.ReportCategory category,

            @Parameter(description = "Search by title or description")
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
        ReportManagementPageDto result = reportManagementService.getAllReports(
            status, category, searchTerm, page, size, sortBy, sortDirection
        );

        return ResponseEntity.ok(ApiResponse.success("Reports retrieved successfully", result));
    }

    @GetMapping("/statistics")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(
        summary = "Get report statistics",
        description = "Get overall statistics about reports by status"
    )
    public ResponseEntity<ApiResponse<ReportStatisticsDto>> getReportStatistics() {
        ReportStatisticsDto stats = reportManagementService.getReportStatistics();
        return ResponseEntity.ok(ApiResponse.success("Statistics retrieved successfully", stats));
    }

    @GetMapping("/pending")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(
        summary = "Get pending reports",
        description = "Get list of reports waiting for admin action"
    )
    public ResponseEntity<ApiResponse<List<ReportManagementDto>>> getPendingReports() {
        List<ReportManagementDto> pendingReports = reportManagementService.getPendingReports();
        return ResponseEntity.ok(ApiResponse.success("Pending reports retrieved successfully", pendingReports));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(
        summary = "Get report details",
        description = "Get detailed information about a specific report"
    )
    public ResponseEntity<ApiResponse<ReportManagementDto>> getReportById(
            @PathVariable Long id
    ) {
        ReportManagementDto report = reportManagementService.getReportById(id);
        return ResponseEntity.ok(ApiResponse.success("Report retrieved successfully", report));
    }

    @PostMapping("/{id}/process")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(
        summary = "Mark report as processing",
        description = "Admin starts processing a pending report"
    )
    public ResponseEntity<ApiResponse<ReportManagementDto>> processReport(
            @PathVariable Long id,
            @RequestBody(required = false) ReportActionRequest request
    ) {
        String adminNotes = request != null ? request.getReason() : null;
        ReportManagementDto report = reportManagementService.processReport(id, adminNotes);
        return ResponseEntity.ok(ApiResponse.success("Report marked as processing", report));
    }

    @PostMapping("/{id}/resolve")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(
        summary = "Resolve report with action",
        description = "Admin resolves a report with specific action (lock, warning, etc.)"
    )
    public ResponseEntity<ApiResponse<?>> resolveReport(
            @PathVariable Long id,
            @Valid @RequestBody ReportActionRequest request
    ) {
        String adminUsername = "admin"; // TODO: Get from authentication context
        var response = reportManagementService.resolveReport(id, request, adminUsername);
        return ResponseEntity.ok(ApiResponse.success("Report resolved successfully", response));
    }

    @PostMapping("/{id}/reject")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(
        summary = "Reject report",
        description = "Admin rejects a report (invalid/false report)"
    )
    public ResponseEntity<ApiResponse<?>> rejectReport(
            @PathVariable Long id,
            @Valid @RequestBody ReportActionRequest request
    ) {
        String adminUsername = "admin"; // TODO: Get from authentication context
        var response = reportManagementService.rejectReport(id, request.getReason(), adminUsername);
        return ResponseEntity.ok(ApiResponse.success("Report rejected", response));
    }

    @PatchMapping("/{id}/status")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(
        summary = "Update report status (legacy endpoint)",
        description = "Update report status - supports RESOLVED or REJECTED with optional action"
    )
    public ResponseEntity<ApiResponse<?>> updateReportStatus(
            @PathVariable Long id,
            @Valid @RequestBody com.ridemate.ridemate_server.application.dto.report.UpdateReportStatusRequest request
    ) {
        String adminUsername = "admin"; // TODO: Get from authentication context
        
        // If status is RESOLVED, use resolve endpoint logic
        if ("RESOLVED".equalsIgnoreCase(request.getStatus()) && request.getResolutionAction() != null) {
            ReportActionRequest actionRequest = new ReportActionRequest();
            actionRequest.setActionType(com.ridemate.ridemate_server.application.dto.report.ReportActionType.valueOf(request.getResolutionAction()));
            actionRequest.setReason(request.getResolutionNotes());
            
            var response = reportManagementService.resolveReport(id, actionRequest, adminUsername);
            return ResponseEntity.ok(ApiResponse.success("Report resolved successfully", response));
        }
        
        // If status is REJECTED, use reject endpoint logic
        if ("REJECTED".equalsIgnoreCase(request.getStatus())) {
            var response = reportManagementService.rejectReport(id, request.getResolutionNotes(), adminUsername);
            return ResponseEntity.ok(ApiResponse.success("Report rejected", response));
        }
        
        // For other statuses, return error
        return ResponseEntity.badRequest().body(
            ApiResponse.error(400, "Invalid status. Use RESOLVED or REJECTED")
        );
    }
}
