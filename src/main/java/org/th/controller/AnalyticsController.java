package org.th.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.th.dto.ApiResponse;
import org.th.dto.analytics.*;
import org.th.service.AnalyticsService;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/admin/analytics")
@RequiredArgsConstructor
@Tag(name = "Analytics", description = "Admin analytics APIs")
@PreAuthorize("hasRole('ADMIN')")
public class AnalyticsController {

    private final AnalyticsService analyticsService;

    @GetMapping("/device/{deviceId}")
    @Operation(summary = "Device stats", description = "Get daily activity stats for a specific device")
    public ResponseEntity<ApiResponse<List<DailyActivityStatsDTO>>> getDeviceStats(
            @PathVariable String deviceId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate start,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate end) {

        LocalDate startDate = start != null ? start : LocalDate.now().minusDays(30);
        LocalDate endDate = end != null ? end : LocalDate.now();

        List<DailyActivityStatsDTO> stats = analyticsService.getDeviceDailyStats(deviceId, startDate, endDate);
        return ResponseEntity.ok(ApiResponse.success("Device stats retrieved", stats));
    }

    @GetMapping("/shops/popular")
    @Operation(summary = "Popular shops", description = "Get most viewed shops")
    public ResponseEntity<ApiResponse<List<ShopPopularityDTO>>> getPopularShops(
            @RequestParam(defaultValue = "10") int limit,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate start,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate end) {

        LocalDate startDate = start != null ? start : LocalDate.now().minusDays(30);
        LocalDate endDate = end != null ? end : LocalDate.now();

        List<ShopPopularityDTO> stats = analyticsService.getPopularShops(startDate, endDate, limit);
        return ResponseEntity.ok(ApiResponse.success("Popular shops retrieved", stats));
    }

    @GetMapping("/features")
    @Operation(summary = "Feature usage", description = "Get usage statistics for app features")
    public ResponseEntity<ApiResponse<List<FeatureUsageDTO>>> getFeatureUsage(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate start,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate end) {

        LocalDate startDate = start != null ? start : LocalDate.now().minusDays(30);
        LocalDate endDate = end != null ? end : LocalDate.now();

        List<FeatureUsageDTO> stats = analyticsService.getFeatureUsage(startDate, endDate);
        return ResponseEntity.ok(ApiResponse.success("Feature usage retrieved", stats));
    }

    @GetMapping("/devices")
    @Operation(summary = "Device OS stats", description = "Get breakdown by OS (iOS, Android, etc.)")
    public ResponseEntity<ApiResponse<List<DeviceTypeStatsDTO>>> getDeviceStats(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate start,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate end) {

        LocalDate startDate = start != null ? start : LocalDate.now().minusDays(30);
        LocalDate endDate = end != null ? end : LocalDate.now();

        List<DeviceTypeStatsDTO> stats = analyticsService.getOsStats(startDate, endDate);
        return ResponseEntity.ok(ApiResponse.success("Device OS stats retrieved", stats));
    }

    @GetMapping("/categories")
    @Operation(summary = "Popular categories", description = "Get most viewed categories")
    public ResponseEntity<ApiResponse<List<CategoryPopularityDTO>>> getCategoryStats(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate start,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate end) {

        LocalDate startDate = start != null ? start : LocalDate.now().minusDays(30);
        LocalDate endDate = end != null ? end : LocalDate.now();

        List<CategoryPopularityDTO> stats = analyticsService.getCategoryStats(startDate, endDate);
        return ResponseEntity.ok(ApiResponse.success("Category stats retrieved", stats));
    }

    @GetMapping("/locations")
    @Operation(summary = "Location stats", description = "Get activity by township")
    public ResponseEntity<ApiResponse<List<LocationStatsDTO>>> getLocationStats(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate start,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate end) {

        LocalDate startDate = start != null ? start : LocalDate.now().minusDays(30);
        LocalDate endDate = end != null ? end : LocalDate.now();

        List<LocationStatsDTO> stats = analyticsService.getLocationStats(startDate, endDate);
        return ResponseEntity.ok(ApiResponse.success("Location stats retrieved", stats));
    }
}
