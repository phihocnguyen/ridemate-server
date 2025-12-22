package com.ridemate.ridemate_server.presentation.dto.mission;

import com.ridemate.ridemate_server.domain.entity.Mission;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdateMissionRequest {
    
    @Size(max = 200, message = "Title must not exceed 200 characters")
    private String title;
    
    private String description;
    
    private Mission.MissionType missionType;
    
    private Mission.TargetType targetType;
    
    @Min(value = 1, message = "Target value must be at least 1")
    private Integer targetValue;
    
    @Min(value = 0, message = "Reward points must be non-negative")
    private Integer rewardPoints;
    
    private Long rewardVoucherId;
    
    private LocalDateTime startDate;
    
    private LocalDateTime endDate;
    
    private Boolean isActive;
    
    private Integer priority;
    
    private String iconUrl;
    
    private String bannerUrl;
}
