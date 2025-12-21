package com.ridemate.ridemate_server.application.service.admin.impl;

import com.ridemate.ridemate_server.application.service.admin.AdminService;
import com.ridemate.ridemate_server.domain.entity.Match;
import com.ridemate.ridemate_server.domain.entity.Report;
import com.ridemate.ridemate_server.domain.entity.Session;
import com.ridemate.ridemate_server.domain.entity.User;
import com.ridemate.ridemate_server.domain.repository.SessionRepository;
import com.ridemate.ridemate_server.domain.repository.UserRepository;
import com.ridemate.ridemate_server.domain.repository.VehicleRepository;
import com.ridemate.ridemate_server.domain.repository.VoucherRepository;
import com.ridemate.ridemate_server.presentation.dto.admin.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;

@Service
@RequiredArgsConstructor
public class AdminServiceImpl implements AdminService {

    private final UserRepository userRepository;
    private final SessionRepository sessionRepository;
    private final VoucherRepository voucherRepository;
    private final VehicleRepository vehicleRepository;
    private final com.ridemate.ridemate_server.domain.repository.MessageRepository messageRepository;
    private final com.ridemate.ridemate_server.domain.repository.UserVoucherRepository userVoucherRepository;
    private final com.ridemate.ridemate_server.domain.repository.MatchRepository matchRepository;
    private final com.ridemate.ridemate_server.domain.repository.FeedbackRepository feedbackRepository;
    private final com.ridemate.ridemate_server.domain.repository.ReportRepository reportRepository;

    @Override
    public AdminDashboardStatsDto getDashboardStats() {
        return AdminDashboardStatsDto.builder()
                .totalUsers(userRepository.count())
                .totalSessions(sessionRepository.count())
                .totalVouchers(voucherRepository.count())
                .totalVehicles(vehicleRepository.count())
                .totalCompletedTrips(matchRepository.countByStatus(com.ridemate.ridemate_server.domain.entity.Match.MatchStatus.COMPLETED))
                .totalCancelledTrips(matchRepository.countByStatus(com.ridemate.ridemate_server.domain.entity.Match.MatchStatus.CANCELLED))
                // --- SỬA Ở ĐÂY: Đếm Report thay vì Feedback ---
                .totalReports(reportRepository.count()) 
                .build();
    }

    @Override
    public AdminChartDataDto getChartData(String type) {
        if ("users".equalsIgnoreCase(type)) {
            return getUserChartData();
        } else if ("sessions".equalsIgnoreCase(type)) {
            return getSessionChartData();
        } else if ("messages".equalsIgnoreCase(type)) {
            return getMessageChartData();
        } else if ("vouchers".equalsIgnoreCase(type)) {
            return getVoucherChartData();
        } else if ("revenue".equalsIgnoreCase(type)) {
            return getRevenueChartData();
        }
        return new AdminChartDataDto();
    }

    private AdminChartDataDto getUserChartData() {
        List<User> users = userRepository.findAll();
        Map<String, Long> groupedByDate = users.stream()
                .collect(Collectors.groupingBy(
                        user -> user.getCreatedAt().toLocalDate().toString(),
                        Collectors.counting()
                ));
        
        return prepareChartData(groupedByDate, "New Users");
    }

    private AdminChartDataDto getSessionChartData() {
        List<Session> sessions = sessionRepository.findAll();
        Map<String, Long> groupedByDate = sessions.stream()
                .collect(Collectors.groupingBy(
                        session -> session.getCreatedAt().toLocalDate().toString(),
                        Collectors.counting()
                ));

        return prepareChartData(groupedByDate, "Sessions");
    }

    private AdminChartDataDto getMessageChartData() {
        List<com.ridemate.ridemate_server.domain.entity.Message> messages = messageRepository.findAll();
        Map<String, Long> groupedByDate = messages.stream()
                .collect(Collectors.groupingBy(
                        message -> message.getCreatedAt().toLocalDate().toString(),
                        Collectors.counting()
                ));

        return prepareChartData(groupedByDate, "Messages");
    }

    private AdminChartDataDto getVoucherChartData() {
        List<com.ridemate.ridemate_server.domain.entity.UserVoucher> userVouchers = userVoucherRepository.findAll();
        Map<String, Long> groupedByDate = userVouchers.stream()
                .collect(Collectors.groupingBy(
                        uv -> uv.getCreatedAt().toLocalDate().toString(),
                        Collectors.counting()
                ));

        return prepareChartData(groupedByDate, "Vouchers Redeemed");
    }

    private AdminChartDataDto getRevenueChartData() {
        List<com.ridemate.ridemate_server.domain.entity.UserVoucher> userVouchers = userVoucherRepository.findAll();
        Map<String, Long> groupedByDate = userVouchers.stream()
                .collect(Collectors.groupingBy(
                        uv -> uv.getCreatedAt().toLocalDate().toString(),
                        Collectors.summingLong(uv -> uv.getVoucher().getCost())
                ));

        return prepareChartData(groupedByDate, "Revenue (Coins)");
    }

    private AdminChartDataDto prepareChartData(Map<String, Long> groupedByDate, String label) {
        List<String> sortedDates = new ArrayList<>(groupedByDate.keySet());
        Collections.sort(sortedDates);

        List<Long> data = sortedDates.stream()
                .map(groupedByDate::get)
                .collect(Collectors.toList());

        return AdminChartDataDto.builder()
                .labels(sortedDates)
                .data(data)
                .label(label)
                .build();
    }

    @Override
    public TripStatsDto getTripStats() {
        long totalTrips = matchRepository.count();
        long pendingTrips = matchRepository.countByStatus(Match.MatchStatus.PENDING);
        long matchedTrips = matchRepository.countByStatus(Match.MatchStatus.ACCEPTED);
        long inProgressTrips = matchRepository.countByStatus(Match.MatchStatus.IN_PROGRESS);
        long completedTrips = matchRepository.countByStatus(Match.MatchStatus.COMPLETED);
        long cancelledTrips = matchRepository.countByStatus(Match.MatchStatus.CANCELLED);

        double completionRate = totalTrips > 0 ? (completedTrips * 100.0 / totalTrips) : 0.0;
        double cancellationRate = totalTrips > 0 ? (cancelledTrips * 100.0 / totalTrips) : 0.0;

        return TripStatsDto.builder()
                .totalTrips(totalTrips)
                .pendingTrips(pendingTrips)
                .matchedTrips(matchedTrips)
                .inProgressTrips(inProgressTrips)
                .completedTrips(completedTrips)
                .cancelledTrips(cancelledTrips)
                .completionRate(Math.round(completionRate * 100.0) / 100.0)
                .cancellationRate(Math.round(cancellationRate * 100.0) / 100.0)
                .build();
    }

    @Override
    public List<ActiveTripDto> getActiveTrips() {
        // Get all sessions that are active (IN_PROGRESS matches)
        List<Session> activeSessions = sessionRepository.findByIsActiveTrue();
        
        return activeSessions.stream()
                .filter(session -> {
                    Match match = session.getMatch();
                    return match != null && 
                           (match.getStatus() == Match.MatchStatus.ACCEPTED || 
                            match.getStatus() == Match.MatchStatus.IN_PROGRESS);
                })
                .map(session -> {
                    Match match = session.getMatch();
                    User driver = match.getDriver();
                    
                    return ActiveTripDto.builder()
                            .sessionId(session.getId())
                            .driverName(driver != null ? driver.getFullName() : "N/A")
                            .driverPhone(driver != null ? driver.getPhoneNumber() : "N/A")
                            .vehicleInfo(match.getVehicle() != null ? 
                                match.getVehicle().getMake() + " " + match.getVehicle().getModel() : "N/A")
                            .startLocation(match.getPickupAddress())
                            .endLocation(match.getDestinationAddress())
                            .startTime(session.getStartTime())
                            .status(match.getStatus().name())
                            .totalRiders(1)
                            .seatsAvailable(match.getVehicle() != null ? match.getVehicle().getCapacity() - 1 : 0)
                            .currentLatitude(driver != null ? driver.getCurrentLatitude() : null)
                            .currentLongitude(driver != null ? driver.getCurrentLongitude() : null)
                            .build();
                })
                .collect(Collectors.toList());
    }

    @Override
    public List<TopUserDto> getTopUsers(int limit) {
        List<User> topUsers = userRepository.findAll().stream()
                .filter(user -> user.getUserType() != User.UserType.ADMIN)
                .sorted((u1, u2) -> {
                    int coinsCompare = Integer.compare(u2.getCoins(), u1.getCoins());
                    if (coinsCompare != 0) return coinsCompare;
                    return Float.compare(u2.getRating(), u1.getRating());
                })
                .limit(limit)
                .collect(Collectors.toList());

        return topUsers.stream()
                .map(user -> {
                    long totalTrips = matchRepository.findAll().stream()
                            .filter(match -> 
                                (match.getDriver() != null && match.getDriver().getId().equals(user.getId())) ||
                                (match.getPassenger() != null && match.getPassenger().getId().equals(user.getId()))
                            )
                            .count();

                    String tier = getMembershipTier(user.getCoins());

                    return TopUserDto.builder()
                            .userId(user.getId())
                            .fullName(user.getFullName())
                            .phoneNumber(user.getPhoneNumber())
                            .profilePictureUrl(user.getProfilePictureUrl())
                            .coins(user.getCoins())
                            .rating(user.getRating())
                            .userType(user.getUserType().name())
                            .totalTrips((int) totalTrips)
                            .membershipTier(tier)
                            .build();
                })
                .collect(Collectors.toList());
    }

    @Override
    public MembershipStatsDto getMembershipStats() {
        List<User> allUsers = userRepository.findAll().stream()
                .filter(user -> user.getUserType() != User.UserType.ADMIN)
                .collect(Collectors.toList());

        long totalMembers = allUsers.size();

        Map<String, Long> tierDistribution = new HashMap<>();
        tierDistribution.put("Bronze", 0L);
        tierDistribution.put("Silver", 0L);
        tierDistribution.put("Gold", 0L);
        tierDistribution.put("Platinum", 0L);

        allUsers.forEach(user -> {
            String tier = getMembershipTier(user.getCoins());
            tierDistribution.put(tier, tierDistribution.get(tier) + 1);
        });

        Map<String, Double> tierPercentages = new HashMap<>();
        tierDistribution.forEach((tier, count) -> {
            double percentage = totalMembers > 0 ? (count * 100.0 / totalMembers) : 0.0;
            tierPercentages.put(tier, Math.round(percentage * 100.0) / 100.0);
        });

        return MembershipStatsDto.builder()
                .totalMembers(totalMembers)
                .tierDistribution(tierDistribution)
                .tierPercentages(tierPercentages)
                .build();
    }

    @Override
    public RevenueStatsDto getRevenueStats() {
        List<com.ridemate.ridemate_server.domain.entity.UserVoucher> allRedemptions = 
                userVoucherRepository.findAll();

        long totalRevenue = allRedemptions.stream()
                .mapToLong(uv -> uv.getVoucher().getCost())
                .sum();

        LocalDate today = LocalDate.now();
        long dailyRevenue = allRedemptions.stream()
                .filter(uv -> uv.getCreatedAt().toLocalDate().equals(today))
                .mapToLong(uv -> uv.getVoucher().getCost())
                .sum();

        LocalDate weekAgo = today.minusDays(7);
        long weeklyRevenue = allRedemptions.stream()
                .filter(uv -> uv.getCreatedAt().toLocalDate().isAfter(weekAgo))
                .mapToLong(uv -> uv.getVoucher().getCost())
                .sum();

        LocalDate monthStart = today.withDayOfMonth(1);
        long monthlyRevenue = allRedemptions.stream()
                .filter(uv -> uv.getCreatedAt().toLocalDate().isAfter(monthStart.minusDays(1)))
                .mapToLong(uv -> uv.getVoucher().getCost())
                .sum();

        Map<String, Long> revenueByDate = allRedemptions.stream()
                .filter(uv -> uv.getCreatedAt().toLocalDate().isAfter(today.minusDays(30)))
                .collect(Collectors.groupingBy(
                        uv -> uv.getCreatedAt().toLocalDate().toString(),
                        LinkedHashMap::new,
                        Collectors.summingLong(uv -> uv.getVoucher().getCost())
                ));

        Map<String, Long> topVouchers = allRedemptions.stream()
                .collect(Collectors.groupingBy(
                        uv -> uv.getVoucher().getVoucherCode(),
                        Collectors.counting()
                ))
                .entrySet().stream()
                .sorted((e1, e2) -> Long.compare(e2.getValue(), e1.getValue()))
                .limit(10)
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        Map.Entry::getValue
                ));

        return RevenueStatsDto.builder()
                .totalRevenue(totalRevenue)
                .dailyRevenue(dailyRevenue)
                .weeklyRevenue(weeklyRevenue)
                .monthlyRevenue(monthlyRevenue)
                .revenueByDate(revenueByDate)
                .topVouchers(topVouchers)
                .build();
    }

    private String getMembershipTier(Integer coins) {
        if (coins == null) coins = 0;
        
        if (coins >= 10000) return "Platinum";
        if (coins >= 5000) return "Gold";
        if (coins >= 2000) return "Silver";
        return "Bronze";
    }
    
    @Override
    public org.springframework.data.domain.Page<TripManagementDto> getAllTrips(
            com.ridemate.ridemate_server.domain.entity.Match.MatchStatus status,
            String searchTerm,
            org.springframework.data.domain.Pageable pageable) {
        
        org.springframework.data.jpa.domain.Specification<Match> spec = 
            com.ridemate.ridemate_server.domain.repository.MatchSpecification.searchTrips(status, searchTerm);
        
        org.springframework.data.domain.Page<Match> matches = matchRepository.findAll(spec, pageable);
        
        return matches.map(this::convertToTripManagementDto);
    }
    
    @Override
    public TripManagementDto getTripById(Long tripId) {
        Match match = matchRepository.findById(tripId)
                .orElseThrow(() -> new RuntimeException("Trip not found with id: " + tripId));
        return convertToTripManagementDto(match);
    }
    
    private TripManagementDto convertToTripManagementDto(Match match) {
        User driver = match.getDriver();
        User passenger = match.getPassenger();
        Session session = match.getSession();
        
        TripManagementDto.DriverInfo driverInfo = null;
        if (driver != null) {
            var vehicle = vehicleRepository.findByDriverIdAndStatus(
                    driver.getId(),
                    com.ridemate.ridemate_server.domain.entity.Vehicle.VehicleStatus.APPROVED
            ).stream().findFirst().orElse(null);
            
            TripManagementDto.VehicleInfo vehicleInfo = null;
            if (vehicle != null) {
                vehicleInfo = TripManagementDto.VehicleInfo.builder()
                        .id(vehicle.getId())
                        .vehicleType(vehicle.getVehicleType().toString())
                        .licensePlate(vehicle.getLicensePlate())
                        .model(vehicle.getModel())
                        .build();
            }
            
            driverInfo = TripManagementDto.DriverInfo.builder()
                    .id(driver.getId())
                    .fullName(driver.getFullName())
                    .phoneNumber(driver.getPhoneNumber())
                    .rating(driver.getRating() != null ? driver.getRating() : 0.0)
                    .profilePictureUrl(driver.getProfilePictureUrl())
                    .vehicle(vehicleInfo)
                    .build();
        }
        
        List<TripManagementDto.PassengerInfo> passengers = new ArrayList<>();
        if (passenger != null) {
            passengers.add(TripManagementDto.PassengerInfo.builder()
                    .id(passenger.getId())
                    .fullName(passenger.getFullName())
                    .phoneNumber(passenger.getPhoneNumber())
                    .rating(passenger.getRating() != null ? passenger.getRating() : 0.0)
                    .profilePictureUrl(passenger.getProfilePictureUrl())
                    .build());
        }
        
        // --- ĐÃ SỬA Ở ĐÂY: Lấy địa chỉ trực tiếp từ MATCH ---
        String startLocation = match.getPickupAddress(); 
        String endLocation = match.getDestinationAddress();
        
        if (startLocation == null) startLocation = "Điểm đón chưa xác định";
        if (endLocation == null) endLocation = "Điểm đến chưa xác định";
        // ---------------------------------------------------
        
        return TripManagementDto.builder()
                .id(match.getId())
                .driver(driverInfo)
                .startLocation(startLocation)
                .endLocation(endLocation)
                .startTime(match.getStartTime()) 
                .endTime(match.getEndTime())
                .status(match.getStatus())
                .createdAt(match.getCreatedAt())
                .totalPassengers(passengers.size())
                .passengers(passengers)
                .build();
    }
    
    // Report Management methods
    @Override
    public org.springframework.data.domain.Page<ReportManagementDto> getAllReports(
            Report.ReportStatus status,
            org.springframework.data.domain.Pageable pageable) {
        
        org.springframework.data.domain.Page<Report> reports;
        
        if (status != null) {
            reports = reportRepository.findByStatus(status, pageable);
        } else {
            reports = reportRepository.findAll(pageable);
        }
        
        return reports.map(this::convertToReportManagementDto);
    }
    
    @Override
    public ReportManagementDto getReportById(Long reportId) {
        Report report = reportRepository.findById(reportId)
                .orElseThrow(() -> new RuntimeException("Report not found with id: " + reportId));
        return convertToReportManagementDto(report);
    }
    
    @Override
    public ReportManagementDto updateReportStatus(Long reportId, UpdateReportStatusRequest request, String adminUsername) {
        Report report = reportRepository.findById(reportId)
                .orElseThrow(() -> new RuntimeException("Report not found with id: " + reportId));
        
        report.setStatus(request.getStatus());
        report.setResolutionAction(request.getResolutionAction());
        report.setResolutionNotes(request.getResolutionNotes());
        report.setResolvedAt(LocalDateTime.now());
        report.setResolvedBy(adminUsername);
        
        Report savedReport = reportRepository.save(report);
        return convertToReportManagementDto(savedReport);
    }
    
    private ReportManagementDto convertToReportManagementDto(Report report) {
        ReportManagementDto.UserInfo reporterInfo = null;
        if (report.getReporter() != null) {
            User reporter = report.getReporter();
            reporterInfo = ReportManagementDto.UserInfo.builder()
                    .id(reporter.getId())
                    .fullName(reporter.getFullName())
                    .phoneNumber(reporter.getPhoneNumber())
                    .profilePictureUrl(reporter.getProfilePictureUrl())
                    .build();
        }
        
        ReportManagementDto.UserInfo reportedUserInfo = null;
        if (report.getReportedUser() != null) {
            User reportedUser = report.getReportedUser();
            reportedUserInfo = ReportManagementDto.UserInfo.builder()
                    .id(reportedUser.getId())
                    .fullName(reportedUser.getFullName())
                    .phoneNumber(reportedUser.getPhoneNumber())
                    .profilePictureUrl(reportedUser.getProfilePictureUrl())
                    .build();
        }
        
        return ReportManagementDto.builder()
                .id(report.getId())
                .reporter(reporterInfo)
                .reportedUser(reportedUserInfo)
                .matchId(report.getMatch() != null ? report.getMatch().getId() : null)
                .title(report.getTitle())
                .description(report.getDescription())
                .category(report.getCategory())
                .status(report.getStatus())
                .evidenceUrl(report.getEvidenceUrl())
                .resolutionAction(report.getResolutionAction())
                .resolutionNotes(report.getResolutionNotes())
                .resolvedAt(report.getResolvedAt())
                .resolvedBy(report.getResolvedBy())
                .createdAt(report.getCreatedAt())
                .build();
    }
}