package com.ridemate.ridemate_server.domain.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@Entity
@Table(name = "vouchers")
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Voucher extends BaseEntity {

    @Column(nullable = false, unique = true, length = 50, columnDefinition = "VARCHAR(50)")
    private String voucherCode;

    @Column(nullable = false, length = 255, columnDefinition = "VARCHAR(255)")
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private VoucherType voucherType;

    @Column(nullable = false)
    private Integer cost;

    @Column(nullable = false)
    private LocalDateTime expiryDate;

    @Column(nullable = false, columnDefinition = "BOOLEAN DEFAULT true")
    @Builder.Default
    private Boolean isActive = true;

    public enum VoucherType {
        DISCOUNT, PRIORITY_MATCH, FREE_RIDE
    }
}
