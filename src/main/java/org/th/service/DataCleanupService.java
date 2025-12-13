package org.th.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.th.entity.enums.ActivityType;
import org.th.repository.UserActivityRepository;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class DataCleanupService {

    private final UserActivityRepository userActivityRepository;

    /**
     * Run daily at 3 AM
     * Deletes "Noise" (Views) older than 90 days.
     * Deletes "Abandoned" (Anonymous Activity) older than 90 days.
     */
    @Scheduled(cron = "0 0 3 * * ?")
    @Transactional
    public void cleanupOldData() {
        log.info("Starting Daily Data Cleanup...");
        LocalDateTime cutoff = LocalDateTime.now().minusDays(90);

        // 1. Delete old Views (Noise)
        List<ActivityType> viewTypes = List.of(ActivityType.VIEW_SHOP, ActivityType.VIEW_CATEGORY);
        userActivityRepository.deleteByActivityTypeInAndCreatedAtBefore(viewTypes, cutoff);
        log.info("Deleted old View activities created before {}", cutoff);

        // 2. Delete old Anonymous Data (Unclaimed)
        userActivityRepository.deleteByUserIdIsNullAndCreatedAtBefore(cutoff);
        log.info("Deleted old Anonymous activities created before {}", cutoff);

        log.info("Daily Data Cleanup Completed.");
    }
}
