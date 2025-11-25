package com.ridemate.ridemate_server.application.dto.voucher;

import com.ridemate.ridemate_server.domain.entity.Voucher;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class VoucherDto {
    private Long id;
    private String voucherCode;
    private String description;
    private Voucher.VoucherType voucherType;
    private Integer cost;
    private LocalDateTime expiryDate;
    private Boolean isActive;
}
