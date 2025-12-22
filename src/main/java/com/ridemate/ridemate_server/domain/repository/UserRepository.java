package com.ridemate.ridemate_server.domain.repository;

import com.ridemate.ridemate_server.domain.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long>, JpaSpecificationExecutor<User> {
    Optional<User> findByPhoneNumber(String phoneNumber);
    Optional<User> findByProviderId(String providerId);
    Optional<User> findByEmail(String email);
    boolean existsByPhoneNumber(String phoneNumber);
    
    List<User> findByUserTypeAndDriverStatus(User.UserType userType, User.DriverStatus driverStatus);
    
    long countByUserType(User.UserType userType);
    
    @Query("SELECT COUNT(u) FROM User u WHERE u.userType = :userType AND u.createdAt >= :startDate")
    long countByUserTypeSince(@Param("userType") User.UserType userType, 
                              @Param("startDate") LocalDateTime startDate);
    
    @Query("SELECT COUNT(u) FROM User u WHERE u.userType = 'DRIVER' AND u.driverStatus = 'ONLINE'")
    long countActiveDrivers();
    // For user management
    Page<User> findByUserType(User.UserType userType, Pageable pageable);
    
    Page<User> findByIsActive(Boolean isActive, Pageable pageable);
    
    Page<User> findByDriverApprovalStatus(User.DriverApprovalStatus driverApprovalStatus, Pageable pageable);
    
    List<User> findByDriverApprovalStatus(User.DriverApprovalStatus driverApprovalStatus);
    
    long countByIsActive(Boolean isActive);
    
    long countByDriverApprovalStatus(User.DriverApprovalStatus driverApprovalStatus);
}

