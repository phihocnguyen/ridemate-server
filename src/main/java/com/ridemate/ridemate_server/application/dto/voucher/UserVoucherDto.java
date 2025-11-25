package com.ridemate.ridemate_server.application.dto.voucher;

import com.ridemate.ridemate_server.domain.entity.UserVoucher;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserVoucherDto {
    private Long id;
    private VoucherDto voucher;
    private UserVoucher.UserVoucherStatus status;
    private LocalDateTime acquiredDate;
}
