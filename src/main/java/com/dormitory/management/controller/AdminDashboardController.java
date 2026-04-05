package com.dormitory.management.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.dormitory.management.dto.common.ApiResponse;
import com.dormitory.management.dto.dashboard.AdminDashboardOverviewDTO;
import com.dormitory.management.service.AdminDashboardService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/admin/dashboard")
@RequiredArgsConstructor
public class AdminDashboardController {

    private final AdminDashboardService adminDashboardService;

    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    @GetMapping("/overview")
    public ResponseEntity<ApiResponse<AdminDashboardOverviewDTO>> getOverview() {
        AdminDashboardOverviewDTO result = adminDashboardService.getDashboardOverview();
        return ResponseEntity.ok(ApiResponse.success(200, "Lấy dữ liệu dashboard thành công", result));
    }
}
