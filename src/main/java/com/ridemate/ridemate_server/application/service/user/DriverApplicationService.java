package com.ridemate.ridemate_server.application.service.user;

import com.ridemate.ridemate_server.application.dto.user.DriverApplicationRequest;
import com.ridemate.ridemate_server.application.dto.user.UserManagementDto;
import com.ridemate.ridemate_server.domain.entity.User;
import com.ridemate.ridemate_server.domain.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class DriverApplicationService {

    private final UserRepository userRepository;

    /**
     * Apply to become a driver
     */
    @Transactional
    public UserManagementDto applyToBeDriver(Long userId, DriverApplicationRequest request) {
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new RuntimeException("User not found with ID: " + userId));
        
        // Check if user is a passenger
        if (user.getUserType() != User.UserType.PASSENGER) {
            throw new RuntimeException("Only passengers can apply to become drivers");
        }
        
        // Check if already applied
        if (user.getDriverApprovalStatus() == User.DriverApprovalStatus.PENDING) {
            throw new RuntimeException("Application already pending approval");
        }
        
        if (user.getDriverApprovalStatus() == User.DriverApprovalStatus.APPROVED) {
            throw new RuntimeException("User is already a driver");
        }
        
        // Set driver application info
        user.setLicenseNumber(request.getLicenseNumber());
        user.setVehicleInfo(request.getVehicleInfo());
        user.setLicenseImageUrl(request.getLicenseImageUrl());
        user.setVehicleImageUrl(request.getVehicleImageUrl());
        user.setDriverApprovalStatus(User.DriverApprovalStatus.PENDING);
        user.setRejectionReason(null);
        
        User updatedUser = userRepository.save(user);
        
        return mapToUserManagementDto(updatedUser);
    }

    /**
     * Get driver application status
     */
    public UserManagementDto getApplicationStatus(Long userId) {
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new RuntimeException("User not found with ID: " + userId));
        
        return mapToUserManagementDto(user);
    }

    private UserManagementDto mapToUserManagementDto(User user) {
        return UserManagementDto.builder()
            .id(user.getId())
            .fullName(user.getFullName())
            .phoneNumber(user.getPhoneNumber())
            .email(user.getEmail())
            .profilePictureUrl(user.getProfilePictureUrl())
            .rating(user.getRating())
            .userType(user.getUserType().name())
            .driverApprovalStatus(user.getDriverApprovalStatus() != null 
                ? user.getDriverApprovalStatus().name() 
                : null)
            .licenseNumber(user.getLicenseNumber())
            .vehicleInfo(user.getVehicleInfo())
            .isActive(user.getIsActive())
            .coins(user.getCoins())
            .totalRidesCompleted(user.getTotalRidesCompleted())
            .acceptanceRate(user.getAcceptanceRate())
            .completionRate(user.getCompletionRate())
            .createdAt(user.getCreatedAt())
            .updatedAt(user.getUpdatedAt())
            .build();
    }
}
