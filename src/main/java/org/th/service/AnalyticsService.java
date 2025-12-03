package org.th.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.th.dto.analytics.*;
import org.th.entity.enums.ActivityType;
import org.th.repository.UserActivityRepository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AnalyticsService {

    private final UserActivityRepository userActivityRepository;

    /**
     * Get daily stats for a device
     */
    @Transactional(readOnly = true)
    public List<DailyActivityStatsDTO> getDeviceDailyStats(String deviceId, LocalDate startDate, LocalDate endDate) {
        LocalDateTime start = startDate.atStartOfDay();
        LocalDateTime end = endDate.atTime(23, 59, 59);

        return userActivityRepository.getDailyStatsByDevice(deviceId, start, end);
    }

    /**
     * Get most popular shops
     */
    @Transactional(readOnly = true)
    public List<ShopPopularityDTO> getPopularShops(LocalDate startDate, LocalDate endDate, int limit) {
        LocalDateTime start = startDate.atStartOfDay();
        LocalDateTime end = endDate.atTime(23, 59, 59);

        List<ShopPopularityDTO> allShops = userActivityRepository.getMostPopularShops(start, end);

        return allShops.stream()
                .limit(limit)
                .collect(Collectors.toList());
    }

    /**
     * Get feature usage statistics
     */
    @Transactional(readOnly = true)
    public List<FeatureUsageDTO> getFeatureUsage(LocalDate startDate, LocalDate endDate) {
        LocalDateTime start = startDate.atStartOfDay();
        LocalDateTime end = endDate.atTime(23, 59, 59);

        List<Object[]> results = userActivityRepository.getFeatureUsageStats(start, end);
        long totalActions = results.stream().mapToLong(r -> (Long) r[1]).sum();

        List<FeatureUsageDTO> stats = new ArrayList<>();
        for (Object[] result : results) {
            ActivityType type = (ActivityType) result[0];
            Long count = (Long) result[1];

            stats.add(FeatureUsageDTO.builder()
                    .feature(type)
                    .usageCount(count)
                    .percentage(totalActions > 0 ? (double) count / totalActions * 100 : 0)
                    .build());
        }

        return stats;
    }

    /**
     * Get OS statistics
     */
    @Transactional(readOnly = true)
    public List<DeviceTypeStatsDTO> getOsStats(LocalDate startDate, LocalDate endDate) {
        LocalDateTime start = startDate.atStartOfDay();
        LocalDateTime end = endDate.atTime(23, 59, 59);

        List<Object[]> results = userActivityRepository.getOsStats(start, end);
        long total = results.stream().mapToLong(r -> (Long) r[1]).sum();

        List<DeviceTypeStatsDTO> stats = new ArrayList<>();
        for (Object[] result : results) {
            String os = (String) result[0];
            Long count = (Long) result[1];

            stats.add(DeviceTypeStatsDTO.builder()
                    .type(os != null ? os : "Unknown")
                    .count(count)
                    .percentage(total > 0 ? (double) count / total * 100 : 0)
                    .build());
        }
        return stats;
    }

    /**
     * Get Category statistics
     */
    @Transactional(readOnly = true)
    public List<CategoryPopularityDTO> getCategoryStats(LocalDate startDate, LocalDate endDate) {
        LocalDateTime start = startDate.atStartOfDay();
        LocalDateTime end = endDate.atTime(23, 59, 59);

        List<Object[]> results = userActivityRepository.getCategoryStats(start, end);
        long total = results.stream().mapToLong(r -> (Long) r[1]).sum();

        List<CategoryPopularityDTO> stats = new ArrayList<>();
        for (Object[] result : results) {
            String category = (String) result[0];
            Long count = (Long) result[1];

            stats.add(CategoryPopularityDTO.builder()
                    .category(category)
                    .viewCount(count)
                    .percentage(total > 0 ? (double) count / total * 100 : 0)
                    .build());
        }
        return stats;
    }

    /**
     * Get Location (Township) statistics
     */
    @Transactional(readOnly = true)
    public List<LocationStatsDTO> getLocationStats(LocalDate startDate, LocalDate endDate) {
        LocalDateTime start = startDate.atStartOfDay();
        LocalDateTime end = endDate.atTime(23, 59, 59);

        return userActivityRepository.getLocationStats(start, end);
    }
}
