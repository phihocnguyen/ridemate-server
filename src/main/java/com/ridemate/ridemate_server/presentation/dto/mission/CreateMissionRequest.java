package com.ridemate.ridemate_server.presentation.dto.mission;

import com.ridemate.ridemate_server.domain.entity.Mission;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateMissionRequest {
    
    @NotBlank(message = "Title is required")
    @Size(max = 200, message = "Title must not exceed 200 characters")
    private String title;
    
    private String description;
    
    @NotNull(message = "Mission type is required")
    private Mission.MissionType missionType;
    
    @NotNull(message = "Target type is required")
    private Mission.TargetType targetType;
    
    @NotNull(message = "Target value is required")
    @Min(value = 1, message = "Target value must be at least 1")
    private Integer targetValue;
    
    @NotNull(message = "Reward points is required")
    @Min(value = 0, message = "Reward points must be non-negative")
    private Integer rewardPoints;
    
    private Long rewardVoucherId;
    
    @NotNull(message = "Start date is required")
    private LocalDateTime startDate;
    
    @NotNull(message = "End date is required")
    private LocalDateTime endDate;
    
    @Builder.Default
    private Boolean isActive = true;
    
    @Builder.Default
    private Integer priority = 0;
    
    private String iconUrl;
    
    private String bannerUrl;
}
