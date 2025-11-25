package com.ridemate.ridemate_server.presentation.controller;

import com.ridemate.ridemate_server.application.dto.voucher.UserVoucherDto;
import com.ridemate.ridemate_server.application.dto.voucher.VoucherDto;
import com.ridemate.ridemate_server.application.service.voucher.VoucherService;
import com.ridemate.ridemate_server.presentation.dto.response.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/vouchers")
@RequiredArgsConstructor
@Tag(name = "Vouchers", description = "Voucher management endpoints")
public class VoucherController {

    private final VoucherService voucherService;

    @PostMapping
    @Operation(summary = "Create a new voucher", description = "Admin creates a new voucher")
    @SecurityRequirement(name = "bearerAuth")
    public ResponseEntity<ApiResponse<VoucherDto>> createVoucher(@RequestBody VoucherDto voucherDto) {
        VoucherDto createdVoucher = voucherService.createVoucher(voucherDto);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Voucher created successfully", createdVoucher));
    }

    @GetMapping
    @Operation(summary = "Get all vouchers", description = "Get a list of all active vouchers")
    public ResponseEntity<ApiResponse<List<VoucherDto>>> getAllVouchers() {
        List<VoucherDto> vouchers = voucherService.getAllVouchers();
        return ResponseEntity.ok(ApiResponse.success("Vouchers retrieved successfully", vouchers));
    }

    @PostMapping("/{id}/redeem")
    @Operation(summary = "Redeem a voucher", description = "User redeems a voucher using points")
    @SecurityRequirement(name = "bearerAuth")
    public ResponseEntity<ApiResponse<UserVoucherDto>> redeemVoucher(
            @PathVariable Long id,
            HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        UserVoucherDto userVoucher = voucherService.redeemVoucher(userId, id);
        return ResponseEntity.ok(ApiResponse.success("Voucher redeemed successfully", userVoucher));
    }

    @GetMapping("/my-vouchers")
    @Operation(summary = "Get my vouchers", description = "Get a list of vouchers owned by the current user")
    @SecurityRequirement(name = "bearerAuth")
    public ResponseEntity<ApiResponse<List<UserVoucherDto>>> getMyVouchers(HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        List<UserVoucherDto> userVouchers = voucherService.getUserVouchers(userId);
        return ResponseEntity.ok(ApiResponse.success("My vouchers retrieved successfully", userVouchers));
    }
}
