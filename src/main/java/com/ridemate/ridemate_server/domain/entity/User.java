package com.ridemate.ridemate_server.domain.entity;

import jakarta.persistence.*;
import lombok.*;

@Getter
@Setter
@Entity
@Table(name = "users")
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class User extends BaseEntity {

    // ===== ENUMS =====
    
    public enum UserType {
        DRIVER, PASSENGER, ADMIN
    }

    public enum AuthProvider {
        LOCAL, GOOGLE, FACEBOOK
    }

    public enum DriverStatus {
        ONLINE,   // Available and ready to accept rides
        OFFLINE,  // Not accepting rides
        BUSY      // Currently on a ride
    }
    
    public enum DriverApprovalStatus {
        NONE,      // Not applied or PASSENGER
        PENDING,   // Applied and waiting for approval
        APPROVED,  // Approved to be a driver
        REJECTED   // Application rejected
    }

    // ===== BASIC FIELDS =====

    @Column(nullable = false, length = 100, columnDefinition = "VARCHAR(100)")
    private String fullName;

    @Column(nullable = false, unique = true, length = 20, columnDefinition = "VARCHAR(20)")
    private String phoneNumber;

    @Column(nullable = true, unique = true, length = 255, columnDefinition = "VARCHAR(255)")
    private String email;

    @Column(nullable = true, columnDefinition = "TEXT")
    private String profilePictureUrl;

    @Column(nullable = false, columnDefinition = "FLOAT DEFAULT 0.0")
    @Builder.Default
    private Float rating = 0f;

    @Column(nullable = true, length = 500, columnDefinition = "VARCHAR(500)")
    private String faceIdData;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private UserType userType;

    @Column(nullable = true)
    private Double currentLatitude;

    @Column(nullable = true)
    private Double currentLongitude;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private AuthProvider authProvider;

    @Column(nullable = true, unique = true, length = 255, columnDefinition = "VARCHAR(255)")
    private String providerId;

    @Column(nullable = true, columnDefinition = "TEXT")
    private String passwordHash;

    @Column(nullable = false, columnDefinition = "INTEGER DEFAULT 0")
    @Builder.Default
    private Integer coins = 0;

    @Column(nullable = false, columnDefinition = "BOOLEAN DEFAULT true")
    @Builder.Default
    private Boolean isActive = true;

    // ===== VIOLATION & WARNING TRACKING =====
    
    @Column(nullable = false, columnDefinition = "INTEGER DEFAULT 0")
    @Builder.Default
    private Integer violationWarnings = 0;
    
    @Column(nullable = true)
    private java.time.LocalDateTime accountLockedUntil; // NULL = not locked, or contains the unlock datetime

    // ===== DRIVER APPROVAL FIELDS =====
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = true)
    @Builder.Default
    private DriverApprovalStatus driverApprovalStatus = DriverApprovalStatus.NONE;
    
    @Column(nullable = true, length = 50, columnDefinition = "VARCHAR(50)")
    private String licenseNumber;
    
    @Column(nullable = true, length = 500, columnDefinition = "TEXT")
    private String vehicleInfo;
    
    @Column(nullable = true, length = 1000, columnDefinition = "TEXT")
    private String rejectionReason;
    
    @Column(nullable = true, columnDefinition = "TEXT")
    private String licenseImageUrl;
    
    @Column(nullable = true, columnDefinition = "TEXT")
    private String vehicleImageUrl;

    // ===== DRIVER-SPECIFIC FIELDS FOR MATCHING ALGORITHM =====
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = true)
    @Builder.Default
    private DriverStatus driverStatus = DriverStatus.OFFLINE;

    @Column(nullable = false, columnDefinition = "INTEGER DEFAULT 0")
    @Builder.Default
    private Integer totalRidesCompleted = 0;

    @Column(nullable = false, columnDefinition = "INTEGER DEFAULT 0")
    @Builder.Default
    private Integer totalRidesAccepted = 0;

    @Column(nullable = false, columnDefinition = "INTEGER DEFAULT 0")
    @Builder.Default
    private Integer totalRidesOffered = 0;

    // Acceptance Rate = (totalRidesAccepted / totalRidesOffered) * 100
    @Column(nullable = false, columnDefinition = "FLOAT DEFAULT 0.0")
    @Builder.Default
    private Float acceptanceRate = 0f;

    // Completion Rate = (totalRidesCompleted / totalRidesAccepted) * 100
    @Column(nullable = false, columnDefinition = "FLOAT DEFAULT 0.0")
    @Builder.Default
    private Float completionRate = 0f;

    // Last time driver updated their location (for staleness check)
    @Column(nullable = true)
    private java.time.LocalDateTime lastLocationUpdate;
}
