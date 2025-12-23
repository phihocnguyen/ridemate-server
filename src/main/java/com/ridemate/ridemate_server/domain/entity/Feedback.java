package com.ridemate.ridemate_server.domain.entity;

import jakarta.persistence.*;
import lombok.*;

@Getter
@Setter
@Entity
@Table(name = "feedback")
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Feedback extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "match_id", nullable = false)
    private Match match;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "reviewer_id", nullable = false)
    private User reviewer; 

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "reviewed_id", nullable = false)
    private User reviewed; 

    @Column(nullable = false)
    private Integer rating; 

    @Column(length = 500, columnDefinition = "VARCHAR(500)")
    private String comment;

    @Column(columnDefinition = "TEXT")
    private String tags; // JSON string or comma-separated tags
}