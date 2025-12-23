package com.ridemate.ridemate_server.presentation.dto.admin;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import com.ridemate.ridemate_server.domain.entity.Match.MatchStatus;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TripManagementDto {
    private Long id;
    private DriverInfo driver;
    private String startLocation;
    private String endLocation;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private MatchStatus status;
    
    private Double fare;
    private LocalDateTime createdAt;
    
    private Integer matchedRidersCount; 
    private List<PassengerInfo> passengers;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class DriverInfo {
        private Long id;
        private String fullName;
        private String phoneNumber;
        private Double rating;
        private String profilePictureUrl;
        private VehicleInfo vehicle;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class VehicleInfo {
        private Long id;
        private String vehicleType;
        private String licensePlate;
        private String model;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class PassengerInfo {
        private Long id;
        private String fullName;
        private String phoneNumber;
        private Double rating;
        private String profilePictureUrl;
    }
}