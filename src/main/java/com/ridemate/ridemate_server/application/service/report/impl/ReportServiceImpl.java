package com.ridemate.ridemate_server.application.service.report.impl;

import com.ridemate.ridemate_server.application.dto.report.CreateReportRequest;
import com.ridemate.ridemate_server.application.dto.report.ReportResponse;
import com.ridemate.ridemate_server.application.dto.report.UpdateReportStatusRequest;
import com.ridemate.ridemate_server.application.mapper.ReportMapper;
import com.ridemate.ridemate_server.application.service.report.ReportService;
import com.ridemate.ridemate_server.domain.entity.Match;
import com.ridemate.ridemate_server.domain.entity.Report;
import com.ridemate.ridemate_server.domain.entity.User;
import com.ridemate.ridemate_server.domain.repository.MatchRepository;
import com.ridemate.ridemate_server.domain.repository.ReportRepository;
import com.ridemate.ridemate_server.domain.repository.UserRepository;
import com.ridemate.ridemate_server.presentation.exception.ResourceNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class ReportServiceImpl implements ReportService {

    @Autowired
    private ReportRepository reportRepository;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private MatchRepository matchRepository;
    @Autowired
    private ReportMapper reportMapper;

    @Override
    @Transactional
    public ReportResponse createReport(Long reporterId, CreateReportRequest request) {
        User reporter = userRepository.findById(reporterId)
                .orElseThrow(() -> new ResourceNotFoundException("Reporter not found"));

        User reportedUser = null;
        if (request.getReportedUserId() != null) {
            reportedUser = userRepository.findById(request.getReportedUserId())
                    .orElseThrow(() -> new ResourceNotFoundException("Reported user not found"));
        }

        Match match = null;
        if (request.getMatchId() != null) {
            match = matchRepository.findById(request.getMatchId())
                    .orElseThrow(() -> new ResourceNotFoundException("Match not found"));
        }

        Report report = Report.builder()
                .reporter(reporter)
                .reportedUser(reportedUser)
                .match(match)
                .title(request.getTitle())
                .description(request.getDescription())
                .category(Report.ReportCategory.valueOf(request.getCategory()))
                .evidenceUrl(request.getEvidenceUrl())
                .status(Report.ReportStatus.PENDING)
                .build();

        report = reportRepository.save(report);
        return reportMapper.toResponse(report);
    }

    @Override
    public List<ReportResponse> getMyReports(Long reporterId) {
        List<Report> reports = reportRepository.findByReporterId(reporterId);
        return reports.stream()
                .map(reportMapper::toResponse)
                .collect(Collectors.toList());
    }
    
    @Override
    public ReportResponse getReportById(Long reportId) {
        Report report = reportRepository.findById(reportId)
                .orElseThrow(() -> new ResourceNotFoundException("Report not found"));
        return reportMapper.toResponse(report);
    }
    
    // ==================== Admin Methods ====================
    
    @Override
    public Page<ReportResponse> getAllReports(String status, Pageable pageable) {
        Page<Report> reports;
        
        if (status != null && !status.isEmpty() && !status.equalsIgnoreCase("all")) {
            Report.ReportStatus reportStatus = Report.ReportStatus.valueOf(status.toUpperCase());
            reports = reportRepository.findByStatus(reportStatus, pageable);
        } else {
            reports = reportRepository.findAll(pageable);
        }
        
        return reports.map(reportMapper::toResponse);
    }
    
    @Override
    @Transactional
    public ReportResponse updateReportStatus(Long reportId, UpdateReportStatusRequest request) {
        Report report = reportRepository.findById(reportId)
                .orElseThrow(() -> new ResourceNotFoundException("Report not found"));
        
        // Update status
        Report.ReportStatus newStatus = Report.ReportStatus.valueOf(request.getStatus().toUpperCase());
        report.setStatus(newStatus);
        
        // Update resolution details
        if (request.getResolutionAction() != null) {
            Report.ResolutionAction resolutionAction = Report.ResolutionAction.valueOf(request.getResolutionAction().toUpperCase());
            report.setResolutionAction(resolutionAction);
        }
        if (request.getResolutionNotes() != null) {
            report.setResolutionNotes(request.getResolutionNotes());
        }
        
        // ===== ENFORCE ACTION ON REPORTED USER =====
        if (newStatus == Report.ReportStatus.RESOLVED && request.getResolutionAction() != null && report.getReportedUser() != null) {
            User reportedUser = report.getReportedUser();
            java.time.LocalDateTime now = java.time.LocalDateTime.now();
            
            switch (request.getResolutionAction()) {
                case "WARNING":
                    // Increment warning count
                    reportedUser.setViolationWarnings(reportedUser.getViolationWarnings() + 1);
                    
                    // If 3 warnings, auto-lock for 7 days
                    if (reportedUser.getViolationWarnings() >= 3) {
                        reportedUser.setAccountLockedUntil(now.plusDays(7));
                        reportedUser.setIsActive(false);
                        // Reset warnings after lock
                        reportedUser.setViolationWarnings(0);
                    }
                    break;
                    
                case "LOCK_7_DAYS":
                    reportedUser.setAccountLockedUntil(now.plusDays(7));
                    reportedUser.setIsActive(false);
                    break;
                    
                case "LOCK_30_DAYS":
                    reportedUser.setAccountLockedUntil(now.plusDays(30));
                    reportedUser.setIsActive(false);
                    break;
                    
                case "LOCK_PERMANENT":
                    reportedUser.setAccountLockedUntil(now.plusYears(100)); // Effectively permanent
                    reportedUser.setIsActive(false);
                    break;
            }
            
            userRepository.save(reportedUser);
        }
        
        report = reportRepository.save(report);
        return reportMapper.toResponse(report);
    }
    
    @Override
    public Map<String, Long> getReportStatistics() {
        long totalReports = reportRepository.count();
        long pendingReports = reportRepository.countByStatus(Report.ReportStatus.PENDING);
        long processingReports = reportRepository.countByStatus(Report.ReportStatus.PROCESSING);
        long resolvedReports = reportRepository.countByStatus(Report.ReportStatus.RESOLVED);
        long rejectedReports = reportRepository.countByStatus(Report.ReportStatus.REJECTED);
        
        return Map.of(
            "totalReports", totalReports,
            "pendingReports", pendingReports,
            "processingReports", processingReports,
            "resolvedReports", resolvedReports,
            "rejectedReports", rejectedReports
        );
    }
}