package com.ridemate.ridemate_server.application.dto.user;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserManagementDto {
    private Long id;
    private String fullName;
    private String phoneNumber;
    private String email;
    private String profilePictureUrl;
    private Float rating;
    private String userType;
    private String driverApprovalStatus;
    private String licenseNumber;
    private String vehicleInfo;
    private Boolean isActive;
    private Integer coins;
    private Integer totalRidesCompleted;
    private Float acceptanceRate;
    private Float completionRate;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
