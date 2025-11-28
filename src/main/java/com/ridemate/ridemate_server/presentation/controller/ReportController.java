package com.ridemate.ridemate_server.presentation.controller;

import com.ridemate.ridemate_server.application.dto.report.CreateReportRequest;
import com.ridemate.ridemate_server.application.dto.report.ReportResponse;
import com.ridemate.ridemate_server.application.service.report.ReportService;
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
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/reports")
@Tag(name = "Reports", description = "Incident and violation reporting")
public class ReportController {

    @Autowired
    private ReportService reportService;

    @PostMapping
    @Operation(summary = "Create a new report", description = "Submit a report about a trip or user")
    @SecurityRequirement(name = "bearerAuth")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "201", description = "Report submitted successfully",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ReportResponse.class)))
    })
    public ResponseEntity<ApiResponse<ReportResponse>> createReport(
            @Valid @RequestBody CreateReportRequest request,
            @AuthenticationPrincipal Long userId) {
        
        ReportResponse response = reportService.createReport(userId, request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Report submitted successfully", response));
    }

    @GetMapping("/my")
    @Operation(summary = "Get my reports", description = "Get history of reports submitted by current user")
    @SecurityRequirement(name = "bearerAuth")
    public ResponseEntity<ApiResponse<List<ReportResponse>>> getMyReports(
            @AuthenticationPrincipal Long userId) {
        
        List<ReportResponse> response = reportService.getMyReports(userId);
        return ResponseEntity.ok(ApiResponse.success("Reports retrieved successfully", response));
    }
    
    @GetMapping("/{id}")
    @Operation(summary = "Get report details", description = "Get details of a specific report")
    @SecurityRequirement(name = "bearerAuth")
    public ResponseEntity<ApiResponse<ReportResponse>> getReportById(@PathVariable Long id) {
        ReportResponse response = reportService.getReportById(id);
        return ResponseEntity.ok(ApiResponse.success("Report details retrieved", response));
    }
}