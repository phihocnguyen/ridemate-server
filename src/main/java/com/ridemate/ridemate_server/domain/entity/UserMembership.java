package com.ridemate.ridemate_server.domain.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "user_memberships")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserMembership extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "membership_package_id")
    private Membership membership; // Reference to membership package (new)

    @Column(name = "membership_id", nullable = false, length = 100)
    private String membershipId; // Reference to membership package ID (kept for backward compatibility)

    @Column(nullable = false, length = 200)
    private String membershipName; // Name of membership package (kept for backward compatibility)

    @Column(nullable = false)
    private LocalDateTime startDate;

    @Column(nullable = false)
    private LocalDateTime endDate;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private MembershipStatus status = MembershipStatus.ACTIVE;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "payment_id")
    private Payment payment; // Link to payment that activated this membership

    @Column(length = 500)
    private String notes; // Additional notes

    public enum MembershipStatus {
        ACTIVE,      // Membership is currently active
        EXPIRED,     // Membership has expired
        CANCELLED    // Membership was cancelled
    }

    public boolean isActive() {
        return status == MembershipStatus.ACTIVE 
            && LocalDateTime.now().isBefore(endDate)
            && LocalDateTime.now().isAfter(startDate);
    }

    public boolean isExpired() {
        return LocalDateTime.now().isAfter(endDate);
    }
}

