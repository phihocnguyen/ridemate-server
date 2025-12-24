package com.ridemate.ridemate_server.presentation.controller;

import com.ridemate.ridemate_server.application.dto.voucher.VoucherDto;
import com.ridemate.ridemate_server.application.service.voucher.VoucherService;
import com.ridemate.ridemate_server.presentation.dto.response.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/admin/vouchers")
@Tag(name = "Voucher Management", description = "Admin endpoints for managing vouchers")
@RequiredArgsConstructor
@SecurityRequirement(name = "Bearer Authentication")
public class VoucherManagementController {

    private final VoucherService voucherService;

    @GetMapping
    @PreAuthorize("hasAuthority('ADMIN')")
    @Operation(
            summary = "Get all vouchers (admin)",
            description = "Admin gets a list of all vouchers (active and inactive). Optional filter by isActive."
    )
    public ResponseEntity<ApiResponse<List<VoucherDto>>> getAllVouchersForAdmin(
            @Parameter(description = "Optional filter by active status")
            @RequestParam(required = false) Boolean isActive
    ) {
        List<VoucherDto> vouchers = voucherService.getAllVouchersForAdmin(isActive);
        return ResponseEntity.ok(ApiResponse.success("Vouchers retrieved successfully", vouchers));
    }
}