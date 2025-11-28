package com.ridemate.ridemate_server.domain.entity;

import jakarta.persistence.*;
import lombok.*;

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

    @Column(nullable = false)
    private String title;

    @Column(nullable = false, length = 1000)
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ReportCategory category;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private ReportStatus status = ReportStatus.PENDING;

    @Column(length = 500)
    private String evidenceUrl; 

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
}