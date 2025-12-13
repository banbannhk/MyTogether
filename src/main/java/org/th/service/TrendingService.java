package org.th.service;

import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.th.entity.shops.Shop;
import org.th.entity.enums.ActivityType;
import org.th.repository.FavoriteRepository;
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
        private final FavoriteRepository favoriteRepository;

        // Weights for scoring
        private static final double VIEW_WEIGHT = 1.0;
        private static final double FAVORITE_WEIGHT = 5.0;
        private static final double REVIEW_WEIGHT = 10.0;
        private static final double CONVERSION_WEIGHT = 50.0; // High intent actions

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

                // 1. Bulk Fetch Stats (Maps of ShopId -> Count)
                java.util.Map<Long, Long> viewsMap = getCountMap(
                                userActivityRepository.countActivityByTargetIdSince(ActivityType.VIEW_SHOP, oneDayAgo));

                java.util.Map<Long, Long> favoritesMap = getCountMap(
                                favoriteRepository.countFavoritesByShopSince(sevenDaysAgo));

                java.util.Map<Long, Long> reviewsMap = getCountMap(
                                shopReviewRepository.countReviewsByShopSince(sevenDaysAgo));

                // Conversions: Directions, Calls, Shares
                java.util.Map<Long, Long> conversionsMap = getCountMap(
                                userActivityRepository.countActivitiesByTargetIdSince(
                                                List.of(ActivityType.CLICK_DIRECTIONS, ActivityType.CLICK_CALL,
                                                                ActivityType.CLICK_SHARE),
                                                sevenDaysAgo));

                // 2. Calculate Scores in Memory
                for (Shop shop : allShops) {
                        try {
                                long views = viewsMap.getOrDefault(shop.getId(), 0L);
                                long favorites = favoritesMap.getOrDefault(shop.getId(), 0L);
                                long reviews = reviewsMap.getOrDefault(shop.getId(), 0L);
                                long conversions = conversionsMap.getOrDefault(shop.getId(), 0L);

                                // Calculate Score
                                double score = (views * VIEW_WEIGHT) +
                                                (favorites * FAVORITE_WEIGHT) +
                                                (reviews * REVIEW_WEIGHT) +
                                                (conversions * CONVERSION_WEIGHT);

                                shop.setTrendingScore(score);

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
}
