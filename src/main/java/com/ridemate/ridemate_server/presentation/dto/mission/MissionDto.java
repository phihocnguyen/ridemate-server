package com.ridemate.ridemate_server.presentation.dto.mission;

import com.ridemate.ridemate_server.domain.entity.Mission;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MissionDto {
    private Long id;
    private String title;
    private String description;
    private Mission.MissionType missionType;
    private Mission.TargetType targetType;
    private Integer targetValue;
    private Integer rewardPoints;
    private Long rewardVoucherId;
    private LocalDateTime startDate;
    private LocalDateTime endDate;
    private Boolean isActive;
    private Integer priority;
    private String iconUrl;
    private String bannerUrl;
    private Boolean isExpired;
    private Boolean isAvailable;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
