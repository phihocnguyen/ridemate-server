package com.ridemate.ridemate_server.application.service.user;

import com.ridemate.ridemate_server.application.dto.user.*;
import com.ridemate.ridemate_server.domain.entity.User;
import com.ridemate.ridemate_server.domain.repository.UserRepository;
import com.ridemate.ridemate_server.domain.repository.UserSpecification;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UserManagementService {

    private final UserRepository userRepository;

    /**
     * Get all users with pagination and filters
     */
    public UserManagementPageDto getAllUsers(
            User.UserType userType,
            Boolean isActive,
            User.DriverApprovalStatus driverApprovalStatus,
            String searchTerm,
            int page,
            int size,
            String sortBy,
            String sortDirection
    ) {
        Sort.Direction direction = sortDirection.equalsIgnoreCase("DESC") 
            ? Sort.Direction.DESC 
            : Sort.Direction.ASC;
        
        Pageable pageable = PageRequest.of(page, size, Sort.by(direction, sortBy));
        
        Specification<User> spec = UserSpecification.searchUsers(userType, isActive, driverApprovalStatus, searchTerm);
        Page<User> userPage = userRepository.findAll(spec, pageable);
        
        List<UserManagementDto> userDtos = userPage.getContent()
            .stream()
            .map(this::mapToUserManagementDto)
            .collect(Collectors.toList());
        
        return UserManagementPageDto.builder()
            .users(userDtos)
            .currentPage(userPage.getNumber())
            .totalPages(userPage.getTotalPages())
            .totalElements(userPage.getTotalElements())
            .pageSize(userPage.getSize())
            .build();
    }

    /**
     * Get pending driver approvals
     */
    public List<UserManagementDto> getPendingDriverApprovals() {
        List<User> pendingDrivers = userRepository.findByDriverApprovalStatus(
            User.DriverApprovalStatus.PENDING
        );
        
        return pendingDrivers.stream()
            .map(this::mapToUserManagementDto)
            .collect(Collectors.toList());
    }

    /**
     * Approve a driver application
     */
    @Transactional
    public UserManagementDto approveDriver(Long userId) {
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new RuntimeException("User not found with ID: " + userId));
        
        if (user.getDriverApprovalStatus() != User.DriverApprovalStatus.PENDING) {
            throw new RuntimeException("User application is not in PENDING status");
        }
        
        // Update user to driver
        user.setUserType(User.UserType.DRIVER);
        user.setDriverApprovalStatus(User.DriverApprovalStatus.APPROVED);
        user.setRejectionReason(null);
        
        User updatedUser = userRepository.save(user);
        return mapToUserManagementDto(updatedUser);
    }

    /**
     * Reject a driver application
     */
    @Transactional
    public UserManagementDto rejectDriver(Long userId, String rejectionReason) {
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new RuntimeException("User not found with ID: " + userId));
        
        if (user.getDriverApprovalStatus() != User.DriverApprovalStatus.PENDING) {
            throw new RuntimeException("User application is not in PENDING status");
        }
        
        user.setDriverApprovalStatus(User.DriverApprovalStatus.REJECTED);
        user.setRejectionReason(rejectionReason);
        
        User updatedUser = userRepository.save(user);
        return mapToUserManagementDto(updatedUser);
    }

    /**
     * Toggle user active status
     */
    @Transactional
    public UserManagementDto toggleUserStatus(Long userId, Boolean isActive) {
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new RuntimeException("User not found with ID: " + userId));
        
        user.setIsActive(isActive);
        
        User updatedUser = userRepository.save(user);
        return mapToUserManagementDto(updatedUser);
    }

    /**
     * Get user statistics
     */
    public UserStatisticsDto getUserStatistics() {
        return UserStatisticsDto.builder()
            .totalUsers(userRepository.count())
            .totalDrivers(userRepository.countByUserType(User.UserType.DRIVER))
            .totalPassengers(userRepository.countByUserType(User.UserType.PASSENGER))
            .totalAdmins(userRepository.countByUserType(User.UserType.ADMIN))
            .activeUsers(userRepository.countByIsActive(Boolean.TRUE))
            .inactiveUsers(userRepository.countByIsActive(Boolean.FALSE))
            .pendingDriverApprovals(userRepository.countByDriverApprovalStatus(User.DriverApprovalStatus.PENDING))
            .approvedDrivers(userRepository.countByDriverApprovalStatus(User.DriverApprovalStatus.APPROVED))
            .rejectedDrivers(userRepository.countByDriverApprovalStatus(User.DriverApprovalStatus.REJECTED))
            .build();
    }

    /**
     * Get user by ID for management
     */
    public UserManagementDto getUserById(Long userId) {
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new RuntimeException("User not found with ID: " + userId));
        
        return mapToUserManagementDto(user);
    }

    /**
     * Map User entity to UserManagementDto
     */
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
