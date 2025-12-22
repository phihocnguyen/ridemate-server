package com.ridemate.ridemate_server.application.dto.admin;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class VoucherStatsResponse {
    private String voucherName;
    private Long usageCount;
    private Double percentage;
    private String color;
}
