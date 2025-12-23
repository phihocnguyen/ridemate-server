package com.ridemate.ridemate_server.application.dto.admin;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MembershipStatsResponse {
    private String membershipTier;
    private Long memberCount;
    private Double percentage;
}
