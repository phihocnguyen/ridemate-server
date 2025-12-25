package com.ridemate.ridemate_server.domain.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "daily_spins", uniqueConstraints = {
    @UniqueConstraint(columnNames = {"user_id", "spin_date"})
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DailySpin extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(nullable = false)
    private LocalDate spinDate;

    @Column(nullable = false)
    private Integer rewardPoints;

    @Column(nullable = false)
    private LocalDateTime spinTime;

    @Column(length = 500)
    private String notes;
}

