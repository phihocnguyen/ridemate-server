package com.ridemate.ridemate_server.application.service.report;

import com.ridemate.ridemate_server.application.dto.report.CreateReportRequest;
import com.ridemate.ridemate_server.application.dto.report.ReportResponse;
import java.util.List;

public interface ReportService {
    ReportResponse createReport(Long reporterId, CreateReportRequest request);
    List<ReportResponse> getMyReports(Long reporterId);
    ReportResponse getReportById(Long reportId);
}