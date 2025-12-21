package com.ridemate.ridemate_server.application.service.report;

import com.ridemate.ridemate_server.application.dto.report.*;
import com.ridemate.ridemate_server.domain.entity.Report;

import java.util.List;

public interface ReportManagementService {

    ReportManagementPageDto getAllReports(
            Report.ReportStatus status,
            Report.ReportCategory category,
            String searchTerm,
            int page,
            int size,
            String sortBy,
            String sortDirection
    );

    ReportStatisticsDto getReportStatistics();

    List<ReportManagementDto> getPendingReports();

    // Dùng tên khác với ReportService để tránh xung đột
    ReportManagementDto getAdminReportDetail(Long id);

    ReportManagementDto processReport(Long id, String adminNotes);

    ReportManagementDto resolveReport(Long id, ReportActionRequest request, String adminUsername);

    ReportManagementDto rejectReport(Long id, String reason, String adminUsername);
}