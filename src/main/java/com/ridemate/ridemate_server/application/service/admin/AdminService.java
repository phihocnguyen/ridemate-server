package com.ridemate.ridemate_server.application.service.admin;

import com.ridemate.ridemate_server.domain.entity.Match.MatchStatus;
import com.ridemate.ridemate_server.presentation.dto.admin.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface AdminService {
    AdminDashboardStatsDto getDashboardStats();
    AdminChartDataDto getChartData(String type); // type: "users", "sessions", "messages", "vouchers", "revenue"
    
    // Dashboard endpoints
    TripStatsDto getTripStats();
    List<ActiveTripDto> getActiveTrips();
    List<TopUserDto> getTopUsers(int limit);
    MembershipStatsDto getMembershipStats();
    RevenueStatsDto getRevenueStats();
    
    // Trip Management endpoints
    Page<TripManagementDto> getAllTrips(MatchStatus status, String searchTerm, Pageable pageable);
    TripManagementDto getTripById(Long tripId);
    
    // Report Management endpoints
    Page<ReportManagementDto> getAllReports(com.ridemate.ridemate_server.domain.entity.Report.ReportStatus status, Pageable pageable);
    ReportManagementDto getReportById(Long reportId);
    ReportManagementDto updateReportStatus(Long reportId, UpdateReportStatusRequest request, String adminUsername);
}
