package com.halolight.controller;

import com.halolight.dto.ApiResponse;
import com.halolight.dto.ChartDataDTO;
import com.halolight.dto.DashboardStatsDTO;
import com.halolight.service.DashboardService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Dashboard statistics and charts controller
 */
@Tag(name = "Dashboard", description = "Dashboard statistics and charts API")
@RestController
@RequestMapping("/api/dashboard")
@RequiredArgsConstructor
@SecurityRequirement(name = "bearerAuth")
public class DashboardController {

    private final DashboardService dashboardService;

    @Operation(summary = "Get dashboard statistics", description = "Get overall dashboard statistics including user counts")
    @GetMapping("/stats")
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    public ResponseEntity<ApiResponse<DashboardStatsDTO>> getStats() {
        DashboardStatsDTO stats = dashboardService.getStats();
        return ResponseEntity.ok(ApiResponse.success("Dashboard statistics retrieved", stats));
    }

    @Operation(summary = "Get user registration chart", description = "Get user registration chart data for the last 7 days")
    @GetMapping("/charts/user-registration")
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    public ResponseEntity<ApiResponse<ChartDataDTO>> getUserRegistrationChart() {
        ChartDataDTO chartData = dashboardService.getUserRegistrationChart();
        return ResponseEntity.ok(ApiResponse.success("User registration chart data retrieved", chartData));
    }

    @Operation(summary = "Get user activity chart", description = "Get user activity chart data for the last 7 days")
    @GetMapping("/charts/user-activity")
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    public ResponseEntity<ApiResponse<ChartDataDTO>> getUserActivityChart() {
        ChartDataDTO chartData = dashboardService.getUserActivityChart();
        return ResponseEntity.ok(ApiResponse.success("User activity chart data retrieved", chartData));
    }
}
