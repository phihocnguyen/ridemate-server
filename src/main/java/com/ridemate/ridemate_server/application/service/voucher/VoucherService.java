package com.ridemate.ridemate_server.application.service.voucher;

import com.ridemate.ridemate_server.application.dto.voucher.UserVoucherDto;
import com.ridemate.ridemate_server.application.dto.voucher.VoucherDto;

import java.util.List;

public interface VoucherService {
    VoucherDto createVoucher(VoucherDto voucherDto);
    List<VoucherDto> getAllVouchers();
    /**
     * Admin-only: get all vouchers, optionally filtered by active status.
     * @param isActive if null returns all; otherwise returns only active/inactive vouchers
     */
    List<VoucherDto> getAllVouchersForAdmin(Boolean isActive);
    UserVoucherDto redeemVoucher(Long userId, Long voucherId);
    List<UserVoucherDto> getUserVouchers(Long userId);
    VoucherDto updateVoucher(Long id, VoucherDto voucherDto);
    void deleteVoucher(Long id);
}
