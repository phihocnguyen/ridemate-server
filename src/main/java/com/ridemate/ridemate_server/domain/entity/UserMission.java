package com.ridemate.ridemate_server.domain.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "user_missions", 
       uniqueConstraints = @UniqueConstraint(columnNames = {"user_id", "mission_id"}))
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserMission extends BaseEntity {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "mission_id", nullable = false)
    private Mission mission;
    
    @Builder.Default
    @Column(nullable = false)
    private Integer progress = 0;
    
    @Builder.Default
    @Column(nullable = false)
    private Boolean isCompleted = false;
    
    @Column
    private LocalDateTime completedAt;
    
    @Builder.Default
    @Column(nullable = false)
    private Boolean rewardClaimed = false;
    
    @Column
    private LocalDateTime claimedAt;
    
    @Column
    private LocalDateTime expiresAt;
    
    public void incrementProgress(int amount) {
        this.progress += amount;
        
        // Check if mission is completed
        if (this.progress >= mission.getTargetValue() && !this.isCompleted) {
            this.isCompleted = true;
            this.completedAt = LocalDateTime.now();
        }
    }
    
    public void claimReward() {
        if (this.isCompleted && !this.rewardClaimed) {
            this.rewardClaimed = true;
            this.claimedAt = LocalDateTime.now();
        }
    }
    
    public boolean canClaim() {
        return this.isCompleted && !this.rewardClaimed;
    }
    
    public Integer getProgressPercentage() {
        if (mission.getTargetValue() == 0) return 0;
        return Math.min(100, (progress * 100) / mission.getTargetValue());
    }
}
