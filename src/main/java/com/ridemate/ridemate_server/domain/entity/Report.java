package com.ridemate.ridemate_server.domain.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@Entity
@Table(name = "reports")
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Report extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "reporter_id", nullable = false)
    private User reporter; 

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "reported_user_id")
    private User reportedUser; 

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "match_id")
    private Match match; 

    @Column(nullable = false, columnDefinition = "TEXT")
    private String title;

    @Column(nullable = false, length = 1000, columnDefinition = "TEXT")
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ReportCategory category;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private ReportStatus status = ReportStatus.PENDING;

    @Column(length = 500, columnDefinition = "VARCHAR(500)")
    private String evidenceUrl;

    /**
     * Resolution action taken on this report
     */
    @Enumerated(EnumType.STRING)
    private ResolutionAction resolutionAction;

    /**
     * Admin notes when resolving the report
     */
    @Column(length = 500, columnDefinition = "VARCHAR(500)")
    private String resolutionNotes;

    /**
     * When the report was resolved
     */
    private LocalDateTime resolvedAt;

    /**
     * Which admin resolved the report
     */
    @Column(columnDefinition = "TEXT")
    private String resolvedBy; 

    public enum ReportCategory {
        SAFETY,             
        BEHAVIOR,           
        LOST_ITEM,          
        PAYMENT,            
        APP_ISSUE,          
        OTHER
    }

    public enum ReportStatus {
        PENDING,    
        PROCESSING, 
        RESOLVED,   
        REJECTED    
    }

    public enum ResolutionAction {
        LOCK_7_DAYS,
        LOCK_30_DAYS,
        LOCK_PERMANENT,
        WARNING
    }
}