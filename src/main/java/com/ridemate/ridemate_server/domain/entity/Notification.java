package com.ridemate.ridemate_server.domain.entity;

import jakarta.persistence.*;
import lombok.*;

@Getter
@Setter
@Entity
@Table(name = "notifications")
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Notification extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user; 

    @Column(nullable = false, columnDefinition = "VARCHAR(200)")
    private String title;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String body;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private NotificationType type;

    @Column(name = "reference_id")
    private Long referenceId; 

    @Column(nullable = false)
    @Builder.Default
    private Boolean isRead = false;

    public enum NotificationType {
        MATCH_REQUEST,
        MATCH_ACCEPTED,
        MATCH_CANCELLED,
        NEW_MESSAGE,
        RIDE_COMPLETED,
        PROMOTION,
        SYSTEM,
        TRIP_STARTED,
        TRIP_ENDED,
        DRIVER_ARRIVED,
        REFUND_PROCESSED
    }
}