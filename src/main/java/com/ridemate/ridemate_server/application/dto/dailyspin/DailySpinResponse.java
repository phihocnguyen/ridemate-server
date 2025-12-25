package com.ridemate.ridemate_server.application.dto.dailyspin;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DailySpinResponse {
    private Long id;
    private Long userId;
    private LocalDate spinDate;
    private Integer rewardPoints;
    private LocalDateTime spinTime;
    private Boolean canSpinToday; // Whether user can spin today
}

