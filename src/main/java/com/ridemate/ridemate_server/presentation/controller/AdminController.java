package com.ridemate.ridemate_server.presentation.controller;

import com.ridemate.ridemate_server.application.service.admin.AdminService;
import com.ridemate.ridemate_server.presentation.dto.admin.AdminChartDataDto;
import com.ridemate.ridemate_server.presentation.dto.admin.AdminDashboardStatsDto;

import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin")
@Tag(name = "Admin Management", description = "Admin management endpoints")
@RequiredArgsConstructor
public class AdminController {

    private final AdminService adminService;

    @GetMapping("/dashboard/stats")
    public ResponseEntity<AdminDashboardStatsDto> getDashboardStats() {
        return ResponseEntity.ok(adminService.getDashboardStats());
    }

    @GetMapping("/dashboard/charts")
    public ResponseEntity<AdminChartDataDto> getChartData(@RequestParam(defaultValue = "users") String type) {
        return ResponseEntity.ok(adminService.getChartData(type));
    }
}
