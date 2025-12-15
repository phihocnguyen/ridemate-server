package com.ridemate.ridemate_server.application.dto.report;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReportStatisticsDto {
    private Long totalReports;
    private Long pendingReports;
    private Long processingReports;
    private Long resolvedReports;
    private Long rejectedReports;
}
