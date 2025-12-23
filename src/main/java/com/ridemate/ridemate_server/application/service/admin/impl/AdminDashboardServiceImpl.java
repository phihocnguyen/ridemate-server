package com.ridemate.ridemate_server.application.service.admin.impl;

import com.ridemate.ridemate_server.application.dto.admin.DashboardStatsResponse;
import com.ridemate.ridemate_server.application.service.admin.AdminDashboardService;
import com.ridemate.ridemate_server.application.service.admin.AdminDriverAnalyticsService;
import com.ridemate.ridemate_server.application.service.admin.AdminRevenueAnalyticsService;
import com.ridemate.ridemate_server.application.service.admin.AdminTripAnalyticsService;
import com.ridemate.ridemate_server.application.service.admin.AdminRealTimeMonitoringService;
import com.ridemate.ridemate_server.domain.entity.User;
import com.ridemate.ridemate_server.domain.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

@Service
@RequiredArgsConstructor
public class AdminDashboardServiceImpl implements AdminDashboardService {

    private final AdminDriverAnalyticsService driverAnalyticsService;
    private final AdminTripAnalyticsService tripAnalyticsService;
    private final AdminRevenueAnalyticsService revenueAnalyticsService;
    private final AdminRealTimeMonitoringService realTimeMonitoringService;
    private final UserRepository userRepository;

    @Override
    public DashboardStatsResponse getComprehensiveDashboardStats() {
        Long totalDrivers = driverAnalyticsService.getTotalDrivers();
        Long totalPassengers = userRepository.findAll().stream()
                .filter(u -> u.getUserType() == User.UserType.PASSENGER)
                .count();
        
        Long totalTrips = tripAnalyticsService.getTotalTrips();
        Long totalRevenue = revenueAnalyticsService.getTotalRevenue();
        
        Long activeDrivers = driverAnalyticsService.getActiveDrivers();
        Long activeTrips = realTimeMonitoringService.getActiveTripsCount();
        Long todayTrips = tripAnalyticsService.getTodayTrips();
        Long todayRevenue = revenueAnalyticsService.getTodayRevenue();
        
        Double averageRating = driverAnalyticsService.getAverageDriverRating();
        
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime startOfLastMonth = now.minusMonths(1).truncatedTo(ChronoUnit.DAYS);
        
        long driversLastMonth = userRepository.findAll().stream()
                .filter(u -> u.getUserType() == User.UserType.DRIVER && 
                             u.getCreatedAt() != null && 
                             u.getCreatedAt().isAfter(startOfLastMonth))
                .count();
        
        Double driversGrowth = totalDrivers > 0 ? (driversLastMonth * 100.0 / totalDrivers) : 0.0;
        Double revenueGrowth = revenueAnalyticsService.getRevenueGrowthPercentage();
        Double tripsGrowth = totalTrips > 0 ? (todayTrips * 100.0 / totalTrips) : 0.0;
        
        return DashboardStatsResponse.builder()
                .totalDrivers(totalDrivers)
                .totalPassengers(totalPassengers)
                .totalTrips(totalTrips)
                .totalRevenue(totalRevenue)
                .activeDrivers(activeDrivers)
                .activeTrips(activeTrips)
                .todayTrips(todayTrips)
                .todayRevenue(todayRevenue)
                .averageRating(averageRating)
                .totalRatings(0L)
                .driversGrowth(driversGrowth)
                .tripsGrowth(tripsGrowth)
                .revenueGrowth(revenueGrowth)
                .build();
    }
}
