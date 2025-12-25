package com.ridemate.ridemate_server.domain.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.util.List;

@Entity
@Table(name = "memberships")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Membership extends BaseEntity {

    @Column(nullable = false, unique = true, length = 50)
    private String membershipId; // e.g., "MEM-1001"

    @Column(nullable = false, length = 200)
    private String name; // e.g., "RideMate Premium"

    @Column(nullable = false, columnDefinition = "TEXT")
    private String description;

    @Column(nullable = false)
    private Integer price; // Price in VND

    @Column(nullable = false)
    @Builder.Default
    private Integer duration = 30; // Duration in days

    @Column(nullable = false)
    @Builder.Default
    private Integer maxTripsPerDay = 5; // Maximum trips per day

    @Column(nullable = false)
    @Builder.Default
    private Double pointMultiplier = 1.0; // Point multiplier for this membership

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(columnDefinition = "jsonb")
    private List<String> benefits; // List of benefits as JSON array

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private MembershipStatus status = MembershipStatus.ACTIVE;

    public enum MembershipStatus {
        ACTIVE,  // Membership package is active and available
        PAUSED,  // Membership package is paused (not available for purchase)
        DELETED  // Membership package is deleted (soft delete)
    }

    public boolean isActive() {
        return status == MembershipStatus.ACTIVE;
    }
}

