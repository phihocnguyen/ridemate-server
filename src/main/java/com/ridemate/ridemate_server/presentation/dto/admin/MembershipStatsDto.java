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
public class MembershipStatsDto {
    private long totalMembers;
    private Map<String, Long> tierDistribution; // "Bronze" -> count, "Silver" -> count, etc.
    private Map<String, Double> tierPercentages; // "Bronze" -> 45.5%, "Silver" -> 30.2%, etc.
}
