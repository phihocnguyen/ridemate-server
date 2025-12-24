package com.ridemate.ridemate_server.domain.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "temporary_verifications")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TemporaryVerification {
    
    @Id
    @Column(name = "temp_id", nullable = false, unique = true)
    private String tempId;
    
    @Column(name = "id_card_face_embedding", columnDefinition = "TEXT")
    private String idCardFaceEmbedding;
    
    @Column(name = "selfie_face_embedding", columnDefinition = "TEXT")
    private String selfieFaceEmbedding;  // Embedding from liveness selfie
    
    @Column(name = "liveness_verified")
    @Builder.Default
    private Boolean livenessVerified = false;
    
    @Column(name = "similarity_score")
    private Float similarityScore;
    
    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;
    
    @Column(name = "expires_at", nullable = false)
    private LocalDateTime expiresAt;
    
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        expiresAt = LocalDateTime.now().plusMinutes(30); // Auto-expire after 30 minutes
    }
}
