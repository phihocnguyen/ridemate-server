package com.ridemate.ridemate_server.domain.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@Entity
@Table(name = "otps")
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OTP extends BaseEntity {

    @Column(nullable = false, length = 255)
    private String identifier;

    @Column(nullable = false)
    private String otpCode;

    @Column(nullable = false)
    private LocalDateTime expiryTime;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private OTPPurpose purpose;

    @Column(nullable = false, columnDefinition = "BOOLEAN DEFAULT false")
    @Builder.Default
    private Boolean isVerified = false;

    public enum OTPPurpose {
        REGISTER, LOGIN, RESET_PASS
    }
}
