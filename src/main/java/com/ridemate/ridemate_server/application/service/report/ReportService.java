package com.ridemate.ridemate_server.application.service.report;

import com.ridemate.ridemate_server.application.dto.report.CreateReportRequest;
import com.ridemate.ridemate_server.application.dto.report.ReportResponse;
import com.ridemate.ridemate_server.application.dto.report.UpdateReportStatusRequest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import java.util.List;
import java.util.Map;

public interface ReportService {
    ReportResponse createReport(Long reporterId, CreateReportRequest request);
    List<ReportResponse> getMyReports(Long reporterId);
    ReportResponse getReportById(Long reportId);
    
    // Admin methods
    Page<ReportResponse> getAllReports(String status, Pageable pageable);
    ReportResponse updateReportStatus(Long reportId, UpdateReportStatusRequest request);
    Map<String, Long> getReportStatistics();
}