package com.ridemate.ridemate_server.presentation.dto.mission;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserMissionDto {
    private Long id;
    private Long userId;
    private MissionDto mission;
    private Integer progress;
    private Integer progressPercentage;
    private Boolean isCompleted;
    private LocalDateTime completedAt;
    private Boolean rewardClaimed;
    private LocalDateTime claimedAt;
    private LocalDateTime expiresAt;
    private Boolean canClaim;
}
