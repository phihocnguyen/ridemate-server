package com.ridemate.ridemate_server.domain.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@Entity
@Table(name = "user_vouchers")
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserVoucher extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "voucher_id", nullable = false)
    private Voucher voucher;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private UserVoucherStatus status;

    @Column(nullable = false)
    private LocalDateTime acquiredDate;

    public enum UserVoucherStatus {
        UNUSED, REDEEMED, EXPIRED
    }
}
