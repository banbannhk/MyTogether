package org.th.service.mobile;

import org.th.repository.*;
import org.th.entity.*;
import org.th.entity.shops.*;

import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.th.entity.shops.Shop;
import org.th.entity.enums.ActivityType;
import org.th.repository.UserFavoriteRepository;
import org.th.repository.ShopRepository;
import org.th.repository.UserActivityRepository;
import org.th.repository.ShopReviewRepository;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@lombok.extern.slf4j.Slf4j
public class TrendingService {

        private final ShopRepository shopRepository;
        private final UserActivityRepository userActivityRepository;
        private final ShopReviewRepository shopReviewRepository;
        private final UserFavoriteRepository userFavoriteRepository;

        // Weights for scoring (Updated for Quality > Quantity)
        private static final double VIEW_WEIGHT = 1.0;
        private static final double FAVORITE_WEIGHT = 10.0; // Increased from 5.0
        private static final double REVIEW_WEIGHT = 20.0; // Increased from 10.0
        private static final double CONVERSION_WEIGHT = 100.0; // Increased from 50.0 (High intent actions)

        /**
         * Calculate trending scores for all shops
         * Runs every hour
         */
        /**
         * Calculate trending scores for all shops
         * Optimized: Uses aggregate queries to avoid N+5 problem
         * Runs every hour
         */
        @Scheduled(fixedRate = 3600000) // 1 hour in milliseconds
        @Transactional
        @org.springframework.cache.annotation.CacheEvict(value = "trendingShops", allEntries = true)
        public void updateTrendingScores() {
                log.info("Starting optimized trending score calculation...");
                long start = System.currentTimeMillis();

                List<Shop> allShops = shopRepository.findAll();
                LocalDateTime now = LocalDateTime.now();
                LocalDateTime oneDayAgo = now.minusDays(1);
                LocalDateTime sevenDaysAgo = now.minusDays(7);

                // 1. Bulk Fetch Stats (with different time windows for decay effect)
                // Views: Last 24 hours (very fresh)
                java.util.Map<Long, Long> freshViewsMap = getCountMap(
                                userActivityRepository.countActivityByTargetIdSince(ActivityType.VIEW_SHOP, oneDayAgo));

                // Views: Last 7 days (slower decay)
                java.util.Map<Long, Long> weeklyViewsMap = getCountMap(
                                userActivityRepository.countActivityByTargetIdSince(ActivityType.VIEW_SHOP,
                                                sevenDaysAgo));

                java.util.Map<Long, Long> favoritesMap = getCountMap(
                                userFavoriteRepository.countFavoritesByShopSince(sevenDaysAgo));

                java.util.Map<Long, Long> reviewsMap = getCountMap(
                                shopReviewRepository.countReviewsByShopSince(sevenDaysAgo));

                // Conversions: Last 7 days
                java.util.Map<Long, Long> conversionsMap = getCountMap(
                                userActivityRepository.countActivitiesByTargetIdSince(
                                                List.of(ActivityType.CLICK_DIRECTIONS, ActivityType.CLICK_CALL,
                                                                ActivityType.CLICK_SHARE),
                                                sevenDaysAgo));

                // 2. Calculate Scores in Memory
                for (Shop shop : allShops) {
                        try {
                                long freshViews = freshViewsMap.getOrDefault(shop.getId(), 0L);
                                long weeklyViews = weeklyViewsMap.getOrDefault(shop.getId(), 0L);
                                long favorites = favoritesMap.getOrDefault(shop.getId(), 0L);
                                long reviews = reviewsMap.getOrDefault(shop.getId(), 0L);
                                long conversions = conversionsMap.getOrDefault(shop.getId(), 0L);

                                // Simplified Decay: fresh views (last 24h) carry much more weight than weekly
                                // This simulates a "hot" vs "warm" trending effect
                                double baseScore = (freshViews * VIEW_WEIGHT * 3.0) +
                                                (weeklyViews * VIEW_WEIGHT) +
                                                (favorites * FAVORITE_WEIGHT) +
                                                (reviews * REVIEW_WEIGHT) +
                                                (conversions * CONVERSION_WEIGHT);

                                // Velocity Scoring: Apply Newness Boost
                                double multiplier = calculateNewnessMultiplier(shop.getCreatedAt());
                                double finalScore = baseScore * multiplier;

                                shop.setTrendingScore(finalScore);

                        } catch (Exception e) {
                                log.error("Error calculating score for shop {}: {}", shop.getId(), e.getMessage());
                        }
                }

                // 3. Batch Update
                shopRepository.saveAll(allShops);

                long duration = System.currentTimeMillis() - start;
                log.info("Trending score calculation completed for {} shops in {} ms.", allShops.size(), duration);
        }

        /**
         * Helper to convert List<Object[]> to Map<Long, Long>
         */
        private java.util.Map<Long, Long> getCountMap(List<Object[]> results) {
                java.util.Map<Long, Long> map = new java.util.HashMap<>();
                for (Object[] row : results) {
                        if (row[0] != null && row[1] != null) {
                                Long id = (Long) row[0];
                                Long count = ((Number) row[1]).longValue();
                                map.put(id, count);
                        }
                }
                return map;
        }

        /**
         * Get top trending shops (cached for 5 minutes)
         */
        @org.springframework.cache.annotation.Cacheable(value = "trendingShops", key = "'top10'")
        public List<Shop> getTopTrendingShops() {
                log.debug("Fetching top trending shops from database");
                List<Shop> trendingShops = shopRepository.findTop10ByOrderByTrendingScoreDesc();

                if (trendingShops.isEmpty()) {
                        return trendingShops;
                }

                // Fetch with photos to ensure no LazyInitializationException in Controller
                List<Long> ids = trendingShops.stream().map(Shop::getId).collect(java.util.stream.Collectors.toList());
                return shopRepository.findByIdInWithPhotos(ids);
        }

        /**
         * Calculate Newness Multiplier (Velocity Boost)
         * - < 14 Days: 2.0x (Launch Phase)
         * - < 30 Days: 1.5x (New Phase)
         * - Otherwise: 1.0x
         */
        private double calculateNewnessMultiplier(LocalDateTime createdAt) {
                if (createdAt == null) {
                        return 1.0;
                }
                long daysOld = java.time.temporal.ChronoUnit.DAYS.between(createdAt, LocalDateTime.now());
                if (daysOld <= 14) {
                        return 2.0;
                } else if (daysOld <= 30) {
                        return 1.5;
                }
                return 1.0;
        }
}
