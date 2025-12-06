package org.th.service;

import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.th.entity.shops.Shop;
import org.th.entity.enums.ActivityType;
import org.th.repository.FavoriteRepository;
import org.th.repository.ReviewRepository;
import org.th.repository.ShopRepository;
import org.th.repository.UserActivityRepository;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class TrendingService {

    private static final Logger logger = LoggerFactory.getLogger(TrendingService.class);

    private final ShopRepository shopRepository;
    private final UserActivityRepository userActivityRepository;
    private final ReviewRepository reviewRepository;
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
    @Scheduled(fixedRate = 3600000) // 1 hour in milliseconds
    @Transactional
    @org.springframework.cache.annotation.CacheEvict(value = "trendingShops", allEntries = true)
    public void updateTrendingScores() {
        logger.info("Starting trending score calculation...");

        List<Shop> allShops = shopRepository.findAll();
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime oneDayAgo = now.minusDays(1);
        LocalDateTime sevenDaysAgo = now.minusDays(7);

        for (Shop shop : allShops) {
            try {
                // 1. Recent Views (Last 24h)
                long views = userActivityRepository.countByTargetIdAndActivityTypeAndCreatedAtAfter(
                        shop.getId(), ActivityType.VIEW_SHOP, oneDayAgo);

                // 2. Recent Favorites (Last 7d)
                long favorites = favoriteRepository.countByShopIdAndCreatedAtAfter(
                        shop.getId(), sevenDaysAgo);

                // 3. Recent Reviews (Last 7d)
                long reviews = reviewRepository.countByShopIdAndCreatedAtAfter(
                        shop.getId(), sevenDaysAgo);

                // 4. Conversion Events (Last 7d) - High Intent
                long directions = userActivityRepository.countByTargetIdAndActivityTypeAndCreatedAtAfter(
                        shop.getId(), ActivityType.CLICK_DIRECTIONS, sevenDaysAgo);
                long calls = userActivityRepository.countByTargetIdAndActivityTypeAndCreatedAtAfter(
                        shop.getId(), ActivityType.CLICK_CALL, sevenDaysAgo);
                long shares = userActivityRepository.countByTargetIdAndActivityTypeAndCreatedAtAfter(
                        shop.getId(), ActivityType.CLICK_SHARE, sevenDaysAgo);

                long conversions = directions + calls + shares;

                // Calculate Score
                double score = (views * VIEW_WEIGHT) +
                        (favorites * FAVORITE_WEIGHT) +
                        (reviews * REVIEW_WEIGHT) +
                        (conversions * CONVERSION_WEIGHT);

                shop.setTrendingScore(score);

            } catch (Exception e) {
                logger.error("Error calculating score for shop {}: {}", shop.getId(), e.getMessage());
            }
        }

        shopRepository.saveAll(allShops);
        logger.info("Trending score calculation completed for {} shops.", allShops.size());
    }

    /**
     * Get top trending shops (cached for 5 minutes)
     */
    @org.springframework.cache.annotation.Cacheable(value = "trendingShops", key = "'top10'")
    public List<Shop> getTopTrendingShops() {
        logger.debug("Fetching top trending shops from database");
        return shopRepository.findTop10ByOrderByTrendingScoreDesc();
    }
}
