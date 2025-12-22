package com.ridemate.ridemate_server.application.service.admin;

public interface AdminRevenueAnalyticsService {
    
    Long getTotalRevenue();
    
    Long getTodayRevenue();
    
    Long getRevenueByDateRange(java.time.LocalDate startDate, java.time.LocalDate endDate);
    
    Double getRevenueGrowthPercentage();
}
