package com.ridemate.ridemate_server.application.service.report;

import com.ridemate.ridemate_server.application.dto.report.*;
import com.ridemate.ridemate_server.domain.entity.Report;
import com.ridemate.ridemate_server.domain.entity.User;
import com.ridemate.ridemate_server.domain.repository.ReportRepository;
import com.ridemate.ridemate_server.domain.repository.ReportSpecification;
import com.ridemate.ridemate_server.domain.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class ReportManagementService {

    private final ReportRepository reportRepository;
    private final UserRepository userRepository;

    /**
     * Get all reports with pagination and filters
     */
    public ReportManagementPageDto getAllReports(
            Report.ReportStatus status,
            Report.ReportCategory category,
            String searchTerm,
            int page,
            int size,
            String sortBy,
            String sortDirection
    ) {
        Sort.Direction direction = sortDirection.equalsIgnoreCase("DESC") 
            ? Sort.Direction.DESC 
            : Sort.Direction.ASC;
        
        Pageable pageable = PageRequest.of(page, size, Sort.by(direction, sortBy));
        
        Specification<Report> spec = ReportSpecification.searchReports(status, category, searchTerm);
        Page<Report> reportPage = reportRepository.findAll(spec, pageable);
        
        List<ReportManagementDto> reportDtos = reportPage.getContent()
            .stream()
            .map(this::mapToReportManagementDto)
            .collect(Collectors.toList());
        
        return ReportManagementPageDto.builder()
            .reports(reportDtos)
            .currentPage(reportPage.getNumber())
            .totalPages(reportPage.getTotalPages())
            .totalElements(reportPage.getTotalElements())
            .pageSize(reportPage.getSize())
            .build();
    }

    /**
     * Get pending reports
     */
    public List<ReportManagementDto> getPendingReports() {
        List<Report> pendingReports = reportRepository.findByStatus(Report.ReportStatus.PENDING);
        return pendingReports.stream()
            .map(this::mapToReportManagementDto)
            .collect(Collectors.toList());
    }

    /**
     * Get report details
     */
    public ReportManagementDto getReportById(Long reportId) {
        Report report = reportRepository.findById(reportId)
            .orElseThrow(() -> new RuntimeException("Report not found with ID: " + reportId));
        
        return mapToReportManagementDto(report);
    }

    /**
     * Process report (mark as PROCESSING)
     */
    @Transactional
    public ReportManagementDto processReport(Long reportId, String adminNotes) {
        Report report = reportRepository.findById(reportId)
            .orElseThrow(() -> new RuntimeException("Report not found with ID: " + reportId));
        
        if (report.getStatus() != Report.ReportStatus.PENDING) {
            throw new RuntimeException("Only PENDING reports can be processed");
        }
        
        report.setStatus(Report.ReportStatus.PROCESSING);
        
        Report updatedReport = reportRepository.save(report);
        return mapToReportManagementDto(updatedReport);
    }

    /**
     * Resolve report with specific action on the violating user
     * 
     * Actions:
     * - LOCK_7_DAYS: Lock account for 7 days
     * - LOCK_30_DAYS: Lock account for 30 days
     * - LOCK_PERMANENT: Lock account permanently
     * - WARNING: Send warning (3 warnings = 7-day auto-ban)
     */
    @Transactional
    public ReportResolutionResponse resolveReport(
            Long reportId,
            ReportActionRequest actionRequest,
            String adminUsername) {
        
        Report report = reportRepository.findById(reportId)
                .orElseThrow(() -> new RuntimeException("Report not found: " + reportId));
        
        if (report.getStatus() == Report.ReportStatus.REJECTED || report.getStatus() == Report.ReportStatus.RESOLVED) {
            throw new RuntimeException("This report has already been finalized");
        }
        
        User reportedUser = report.getReportedUser();
        if (reportedUser == null) {
            throw new RuntimeException("Reported user not found for report: " + reportId);
        }
        
        LocalDateTime now = LocalDateTime.now();
        String actionDescription = "";
        String lockMessage = null;
        LocalDateTime lockedUntil = null;
        
        // Process the action
        switch (actionRequest.getActionType()) {
            case LOCK_7_DAYS:
                lockedUntil = now.plusDays(7);
                reportedUser.setAccountLockedUntil(lockedUntil);
                actionDescription = "Account locked for 7 days";
                lockMessage = "Your account has been locked for 7 days due to violation report";
                break;
                
            case LOCK_30_DAYS:
                lockedUntil = now.plusDays(30);
                reportedUser.setAccountLockedUntil(lockedUntil);
                actionDescription = "Account locked for 30 days";
                lockMessage = "Your account has been locked for 30 days due to violation report";
                break;
                
            case LOCK_PERMANENT:
                reportedUser.setIsActive(false);
                reportedUser.setAccountLockedUntil(null); // NULL means permanent lock
                actionDescription = "Account locked permanently";
                lockMessage = "Your account has been permanently locked due to violation report";
                break;
                
            case WARNING:
                reportedUser.setViolationWarnings(reportedUser.getViolationWarnings() + 1);
                actionDescription = "Warning issued to user";
                
                // Check if user reached 3 warnings -> auto-ban for 7 days
                if (reportedUser.getViolationWarnings() >= 3) {
                    lockedUntil = now.plusDays(7);
                    reportedUser.setAccountLockedUntil(lockedUntil);
                    actionDescription = "Warning issued (3rd warning) - Account auto-locked for 7 days";
                    lockMessage = "Your account has been automatically locked for 7 days after receiving 3 violation warnings";
                }
                break;
        }
        
        // Update report
        report.setStatus(Report.ReportStatus.RESOLVED);
        report.setResolutionAction(Report.ResolutionAction.valueOf(actionRequest.getActionType().name()));
        report.setResolutionNotes(actionRequest.getReason());
        report.setResolvedAt(now);
        report.setResolvedBy(adminUsername);
        
        // Save changes
        reportRepository.save(report);
        userRepository.save(reportedUser);
        
        log.info("Report {} resolved with action {} by admin {}", 
                reportId, actionRequest.getActionType(), adminUsername);
        
        // Build response
        return ReportResolutionResponse.builder()
                .reportId(reportId)
                .status(Report.ReportStatus.RESOLVED.name())
                .actionType(actionRequest.getActionType())
                .actionDescription(actionDescription)
                .resolutionNotes(actionRequest.getReason())
                .resolvedAt(now)
                .resolvedBy(adminUsername)
                .reportedUserId(reportedUser.getId())
                .reportedUserName(reportedUser.getFullName())
                .userWarningCount(reportedUser.getViolationWarnings())
                .userLockedUntil(lockedUntil)
                .userLockMessage(lockMessage)
                .build();
    }

    /**
     * Reject a report (no action taken on user)
     */
    @Transactional
    public ReportResolutionResponse rejectReport(
            Long reportId,
            String rejectionReason,
            String adminUsername) {
        
        Report report = reportRepository.findById(reportId)
                .orElseThrow(() -> new RuntimeException("Report not found: " + reportId));
        
        if (report.getStatus() == Report.ReportStatus.REJECTED || report.getStatus() == Report.ReportStatus.RESOLVED) {
            throw new RuntimeException("This report has already been finalized");
        }
        
        LocalDateTime now = LocalDateTime.now();
        
        report.setStatus(Report.ReportStatus.REJECTED);
        report.setResolutionNotes(rejectionReason);
        report.setResolvedAt(now);
        report.setResolvedBy(adminUsername);
        
        reportRepository.save(report);
        
        log.info("Report {} rejected by admin {}", reportId, adminUsername);
        
        return ReportResolutionResponse.builder()
                .reportId(reportId)
                .status(Report.ReportStatus.REJECTED.name())
                .resolutionNotes(rejectionReason)
                .resolvedAt(now)
                .resolvedBy(adminUsername)
                .build();
    }

    /**
     * Get report statistics
     */
    public ReportStatisticsDto getReportStatistics() {
        return ReportStatisticsDto.builder()
            .totalReports(reportRepository.count())
            .pendingReports(reportRepository.countByStatus(Report.ReportStatus.PENDING))
            .processingReports(reportRepository.countByStatus(Report.ReportStatus.PROCESSING))
            .resolvedReports(reportRepository.countByStatus(Report.ReportStatus.RESOLVED))
            .rejectedReports(reportRepository.countByStatus(Report.ReportStatus.REJECTED))
            .build();
    }

    /**
     * Map Report entity to ReportManagementDto
     */
    private ReportManagementDto mapToReportManagementDto(Report report) {
        return ReportManagementDto.builder()
            .id(report.getId())
            .reporterId(report.getReporter().getId())
            .reporterName(report.getReporter().getFullName())
            .reporterPhone(report.getReporter().getPhoneNumber())
            .reportedUserId(report.getReportedUser() != null ? report.getReportedUser().getId() : null)
            .reportedUserName(report.getReportedUser() != null ? report.getReportedUser().getFullName() : null)
            .reportedUserPhone(report.getReportedUser() != null ? report.getReportedUser().getPhoneNumber() : null)
            .matchId(report.getMatch() != null ? report.getMatch().getId() : null)
            .title(report.getTitle())
            .description(report.getDescription())
            .category(report.getCategory().name())
            .status(report.getStatus().name())
            .evidenceUrl(report.getEvidenceUrl())
            .createdAt(report.getCreatedAt())
            .updatedAt(report.getUpdatedAt())
            .build();
    }
}
