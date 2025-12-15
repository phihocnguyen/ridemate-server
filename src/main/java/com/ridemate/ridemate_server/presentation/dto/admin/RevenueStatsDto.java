package com.ridemate.ridemate_server.presentation.dto.admin;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RevenueStatsDto {
    private long totalRevenue; // total coins spent on vouchers
    private long dailyRevenue;
    private long weeklyRevenue;
    private long monthlyRevenue;
    private Map<String, Long> revenueByDate; // "2025-12-15" -> revenue
    private Map<String, Long> topVouchers; // voucher name -> times redeemed
}
