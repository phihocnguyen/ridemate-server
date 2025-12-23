package com.ridemate.ridemate_server.domain.repository;

import com.ridemate.ridemate_server.domain.entity.TemporaryVerification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;

@Repository
public interface TemporaryVerificationRepository extends JpaRepository<TemporaryVerification, String> {
    
    Optional<TemporaryVerification> findByTempId(String tempId);
    
    @Modifying
    @Transactional
    @Query("DELETE FROM TemporaryVerification t WHERE t.expiresAt < :now")
    void deleteExpiredVerifications(LocalDateTime now);
}
