package com.ridemate.ridemate_server.domain.repository;

import com.ridemate.ridemate_server.domain.entity.OTP;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface OTPRepository extends JpaRepository<OTP, Long> {
    Optional<OTP> findByIdentifierAndPurpose(String identifier, OTP.OTPPurpose purpose);
    Optional<OTP> findByIdentifierAndOtpCodeAndIsVerifiedFalse(String identifier, String otpCode);
}
