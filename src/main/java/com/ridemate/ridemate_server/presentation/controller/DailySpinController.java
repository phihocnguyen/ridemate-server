package com.ridemate.ridemate_server.presentation.controller;

import com.ridemate.ridemate_server.application.dto.dailyspin.DailySpinResponse;
import com.ridemate.ridemate_server.application.service.dailyspin.DailySpinService;
import com.ridemate.ridemate_server.presentation.dto.response.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/daily-spin")
@Tag(name = "Daily Spin", description = "APIs for daily spin wheel")
@SecurityRequirement(name = "bearerAuth")
@RequiredArgsConstructor
public class DailySpinController {

    private final DailySpinService dailySpinService;

    @GetMapping("/check")
    @Operation(summary = "Check daily spin status", description = "Check if user can spin today")
    public ResponseEntity<ApiResponse<DailySpinResponse>> checkDailySpin(HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        
        if (userId == null) {
            return ResponseEntity.badRequest()
                .body(ApiResponse.error(400, "User ID is required. Please ensure you are authenticated."));
        }
        
        DailySpinResponse response = dailySpinService.checkDailySpin(userId);
        return ResponseEntity.ok(ApiResponse.success("Daily spin status retrieved successfully", response));
    }

    @PostMapping("/spin")
    @Operation(summary = "Perform daily spin", description = "Spin the wheel and get reward (100, 200, 300, 400, or 500 points)")
    public ResponseEntity<ApiResponse<DailySpinResponse>> performSpin(HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        
        if (userId == null) {
            return ResponseEntity.badRequest()
                .body(ApiResponse.error(400, "User ID is required. Please ensure you are authenticated."));
        }
        
        try {
            DailySpinResponse response = dailySpinService.performSpin(userId);
            return ResponseEntity.ok(ApiResponse.success("Spin completed successfully", response));
        } catch (IllegalStateException e) {
            return ResponseEntity.badRequest()
                .body(ApiResponse.error(400, e.getMessage()));
        }
    }
}

