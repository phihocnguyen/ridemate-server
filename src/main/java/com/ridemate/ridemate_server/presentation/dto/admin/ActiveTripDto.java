package com.ridemate.ridemate_server.presentation.dto.admin;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ActiveTripDto {
    private Long sessionId;
    private String driverName;
    private String driverPhone;
    private String vehicleInfo;
    private String startLocation;
    private String endLocation;
    private LocalDateTime startTime;
    private String status; // "MATCHED", "IN_PROGRESS"
    private int totalRiders;
    private int seatsAvailable;
    private Double currentLatitude;
    private Double currentLongitude;
}
