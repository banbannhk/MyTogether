package org.th.service;

import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.th.entity.shops.Favorite;
import org.th.entity.shops.Shop;
import org.th.entity.shops.ShopReview;
import org.th.repository.FavoriteRepository;
import org.th.repository.ShopRepository;
import org.th.repository.ShopReviewRepository;
import org.th.repository.UserRepository;
import org.th.entity.User;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class RecommendationService {

    private final ShopRepository shopRepository;
    private final FavoriteRepository favoriteRepository;
    private final ShopReviewRepository shopReviewRepository;
    private final UserRepository userRepository;
    private final org.th.repository.UserActivityRepository userActivityRepository;

    /**
     * Get personalized recommendations for a user or device
     */
    @Cacheable("recommendations")
    /**
     * Get personalized recommendations for a user or device using Weighted Scoring
     * Scoring:
     * - Favorite: 5 points
     * - Positive Review (>3): 4 points
     * - Search Query Match: 3 points
     * - Frequent View (>2): 2 points
     */
    @Transactional(readOnly = true)
    public List<Shop> getRecommendedShops(String username, String deviceId) {
        User user = null;
        if (username != null) {
            user = userRepository.findByUsername(username).orElse(null);
        }

        if (user == null && deviceId == null) {
            return new ArrayList<>();
        }

        Long userId = (user != null) ? user.getId() : null;

        // 1. Gather Signals
        Set<Long> excludedIds = new HashSet<>();
        Set<String> preferredCategories = new HashSet<>();
        Set<String> searchKeywords = new HashSet<>();

        // 1a. User Signals
        if (userId != null) {
            List<Favorite> favorites = favoriteRepository.findByUserIdOrderByCreatedAtDesc(userId);
            favorites.forEach(f -> {
                excludedIds.add(f.getShop().getId()); // Don't recommend what they already favorited
                preferredCategories.add(f.getShop().getCategory());
            });

            List<ShopReview> reviews = shopReviewRepository.findByUserIdOrderByCreatedAtDesc(userId);
            reviews.forEach(r -> {
                excludedIds.add(r.getShop().getId()); // Don't recommend what they already reviewed
                if (r.getRating() >= 3)
                    preferredCategories.add(r.getShop().getCategory());
            });

            // Top Search Queries
            List<Object[]> topSearches = userActivityRepository.findTopSearchQueriesByUser(userId);
            topSearches.stream().limit(5).forEach(obj -> searchKeywords.add((String) obj[0]));
        }

        // 1b. Device Signals (for both guest and user)
        if (deviceId != null) {
            // Top Categories by View
            // Note: We don't exclude viewed shops because we might want to re-recommend
            // them if not favorited yet?
            // Actually, for "Fresh" recommendations, let's exclusion viewed shops if viewed
            // VERY recently?
            // For now, let's keep it simple and NOT exclude views, only Favorites/Reviews.

            List<Object[]> topCategories = userActivityRepository.findTopCategoriesByDevice(deviceId);
            topCategories.stream().limit(5).forEach(obj -> preferredCategories.add((String) obj[0]));

            List<Object[]> topSearches = userActivityRepository.findTopSearchQueriesByDevice(deviceId);
            topSearches.stream().limit(5).forEach(obj -> searchKeywords.add((String) obj[0]));
        }

        if (excludedIds.isEmpty())
            excludedIds.add(-1L);
        if (preferredCategories.isEmpty() && searchKeywords.isEmpty()) {
            return shopRepository
                    .findTop10ByIsActiveTrueAndIdNotInOrderByTrendingScoreDesc(new ArrayList<>(excludedIds));
        }

        // 2. Fetch Candidates (Optimized Single Query)
        // We look for shops in preferred categories OR matching search keywords
        // This is a bit complex for a single JPA query if we want to mix both.
        // Strategy: Fetch candidates from categories, then filter/sort in memory.

        // 2. Fetch Candidates (Optimized for Performance)
        // We fetch only IDs first to avoid loading unnecessary entity data (and large
        // blobs/relations)
        List<Long> candidateIds = new ArrayList<>();
        if (!preferredCategories.isEmpty()) {
            candidateIds.addAll(shopRepository.findIdsByCategoryInAndIdNotIn(
                    new ArrayList<>(preferredCategories), new ArrayList<>(excludedIds)));
        }

        // Limit results to top 10 distinct matches
        List<Long> finalIds = candidateIds.stream()
                .distinct()
                .limit(10)
                .collect(Collectors.toList());

        if (finalIds.isEmpty()) {
            // Fallback: Use trending shops optimization
            return shopRepository
                    .findTop10ByIsActiveTrueAndIdNotInOrderByTrendingScoreDesc(new ArrayList<>(excludedIds));
        }

        // 3. Final Fetch: Load only what we need with Photos to prevent N+1
        return shopRepository.findByIdInWithPhotos(finalIds);
    }
}
