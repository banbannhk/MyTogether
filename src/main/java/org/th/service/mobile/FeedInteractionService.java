package org.th.service.mobile;

import org.th.repository.*;
import org.th.entity.*;
import org.th.entity.shops.*;

import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.th.dto.analytics.FeedPerformanceDTO;
import org.th.dto.feed.ShopFeedItemDTO;
import org.th.entity.FeedInteraction;
import org.th.entity.User;
import org.th.entity.enums.FeedInteractionAction;
import org.th.entity.enums.FeedSectionType;
import org.th.repository.FeedInteractionRepository;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Service for tracking feed interactions
 */
@Service
@RequiredArgsConstructor
@lombok.extern.slf4j.Slf4j
public class FeedInteractionService {

    private final FeedInteractionRepository feedInteractionRepository;

    /**
     * Track feed section view (async to avoid blocking)
     */
    @Async("trackingExecutor")
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void trackFeedView(User user, String deviceId, FeedSectionType sectionType,
            List<ShopFeedItemDTO> shops, String sessionId) {
        try {
            if (shops == null || shops.isEmpty()) {
                return;
            }

            List<FeedInteraction> interactions = new ArrayList<>();

            for (int i = 0; i < shops.size(); i++) {
                ShopFeedItemDTO shop = shops.get(i);
                FeedInteraction interaction = FeedInteraction.builder()
                        .user(user)
                        .deviceId(deviceId)
                        .sessionId(sessionId)
                        .sectionType(sectionType)
                        .shopId(shop.getId())
                        .shopName(shop.getName())
                        .position(i + 1) // 1-indexed position
                        .action(FeedInteractionAction.VIEWED)
                        .timestamp(LocalDateTime.now())
                        .build();

                interactions.add(interaction);
            }

            feedInteractionRepository.saveAll(interactions);
            log.debug("Tracked {} feed views for section {}", interactions.size(), sectionType);

        } catch (Exception e) {
            log.error("Failed to track feed view: {}", e.getMessage());
        }
    }

    /**
     * Track shop click from feed (async)
     */
    @Async("trackingExecutor")
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void trackShopClick(User user, String deviceId, FeedSectionType sectionType,
            Long shopId, String shopName, Integer position, String sessionId) {
        try {
            FeedInteraction interaction = FeedInteraction.builder()
                    .user(user)
                    .deviceId(deviceId)
                    .sessionId(sessionId)
                    .sectionType(sectionType)
                    .shopId(shopId)
                    .shopName(shopName)
                    .position(position)
                    .action(FeedInteractionAction.CLICKED)
                    .timestamp(LocalDateTime.now())
                    .build();

            feedInteractionRepository.save(interaction);
            log.debug("Tracked shop click: {} from section {} at position {}",
                    shopId, sectionType, position);

        } catch (Exception e) {
            log.error("Failed to track shop click: {}", e.getMessage());
        }
    }

    /**
     * Get feed performance analytics for date range
     */
    @Transactional(readOnly = true)
    public List<FeedPerformanceDTO> getFeedAnalytics(LocalDateTime start, LocalDateTime end) {
        List<FeedPerformanceDTO> performanceList = new ArrayList<>();

        for (FeedSectionType sectionType : FeedSectionType.values()) {
            FeedPerformanceDTO performance = getSectionPerformance(sectionType, start, end);
            performanceList.add(performance);
        }

        return performanceList;
    }

    /**
     * Get performance metrics for a specific section
     */
    @Transactional(readOnly = true)
    public FeedPerformanceDTO getSectionPerformance(FeedSectionType sectionType,
            LocalDateTime start, LocalDateTime end) {
        // Count views and clicks
        long totalViews = feedInteractionRepository.countBySectionTypeAndTimestampBetween(
                sectionType, start, end);

        long totalClicks = feedInteractionRepository.countByActionAndSectionType(
                FeedInteractionAction.CLICKED, sectionType);

        // Calculate CTR
        double ctr = totalViews > 0 ? (totalClicks * 100.0 / totalViews) : 0.0;

        // Get average click position
        Double avgPosition = feedInteractionRepository.getAverageClickPositionBySection(
                sectionType, start, end);

        // Get top clicked shops
        List<Object[]> topShopsData = feedInteractionRepository.getMostClickedShopsBySection(
                sectionType, start, end);

        List<FeedPerformanceDTO.TopShopDTO> topShops = topShopsData.stream()
                .limit(10)
                .map(row -> FeedPerformanceDTO.TopShopDTO.builder()
                        .shopId((Long) row[0])
                        .shopName((String) row[1])
                        .clickCount((Long) row[2])
                        .build())
                .collect(Collectors.toList());

        return FeedPerformanceDTO.builder()
                .sectionType(sectionType)
                .totalViews(totalViews)
                .totalClicks(totalClicks)
                .clickThroughRate(ctr)
                .averageClickPosition(avgPosition != null ? avgPosition : 0.0)
                .topShops(topShops)
                .build();
    }

    /**
     * Get interactions for a specific session
     */
    @Transactional(readOnly = true)
    public List<FeedInteraction> getSessionInteractions(String sessionId) {
        return feedInteractionRepository.findBySessionIdOrderByTimestampAsc(sessionId);
    }
}
