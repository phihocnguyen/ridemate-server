package com.ridemate.ridemate_server.presentation.dto.admin;

import com.ridemate.ridemate_server.domain.entity.Report;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdateReportStatusRequest {
    private Report.ReportStatus status;
    private Report.ResolutionAction resolutionAction;
    private String resolutionNotes;
}
