package org.th.service;

import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.th.entity.shops.Favorite;
import org.th.entity.shops.Review;
import org.th.entity.shops.Shop;
import org.th.repository.FavoriteRepository;
import org.th.repository.ReviewRepository;
import org.th.repository.ShopRepository;
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
    private final ReviewRepository reviewRepository;
    private final UserRepository userRepository;
    private final org.th.repository.UserActivityRepository userActivityRepository;

    /**
     * Get personalized recommendations for a user or device
     */
    @Cacheable("recommendations")
    @Transactional(readOnly = true)
    public List<Shop> getRecommendedShops(String username) {
        return getRecommendedShops(username, null);
    }

    @Transactional(readOnly = true)
    public List<Shop> getRecommendedShops(String username, String deviceId) {
        User user = null;
        if (username != null) {
            user = userRepository.findByUsername(username).orElse(null);
        }

        // If neither user nor deviceId is present, return empty
        if (user == null && deviceId == null) {
            return new ArrayList<>();
        }

        Long userId = (user != null) ? user.getId() : null;

        // 1. Get user's interaction history (Favorites, Reviews, Activities)
        List<Favorite> favorites = new ArrayList<>();
        List<Review> reviews = new ArrayList<>();

        // 2. Identify excluded IDs (shops user already knows)
        Set<Long> excludedIds = new HashSet<>();
        Set<String> preferredCategories = new HashSet<>();

        if (userId != null) {
            favorites = favoriteRepository.findByUserIdOrderByCreatedAtDesc(userId);
            reviews = reviewRepository.findByUserIdOrderByCreatedAtDesc(userId);

            // OPTIMIZATION: Fetch top categories directly from DB
            List<Object[]> topCategories = userActivityRepository.findTopCategoriesByUser(userId);
            for (Object[] row : topCategories) {
                String category = (String) row[0];
                if (category != null)
                    preferredCategories.add(category);
            }
        }

        if (deviceId != null) {
            // OPTIMIZATION: Fetch top categories for device
            List<Object[]> topCategories = userActivityRepository.findTopCategoriesByDevice(deviceId);
            for (Object[] row : topCategories) {
                String category = (String) row[0];
                if (category != null)
                    preferredCategories.add(category);
            }
        }

        for (Favorite fav : favorites) {
            excludedIds.add(fav.getShop().getId());
            preferredCategories.add(fav.getShop().getCategory());
        }

        for (Review review : reviews) {
            excludedIds.add(review.getShop().getId());
            if (review.getRating() >= 3) { // Only count categories from good reviews
                preferredCategories.add(review.getShop().getCategory());
            }
        }

        // Ensure list is not empty for NOT IN clause
        if (excludedIds.isEmpty()) {
            excludedIds.add(-1L);
        }

        List<Shop> recommendations = new ArrayList<>();

        // 3. Find shops in preferred categories
        if (!preferredCategories.isEmpty()) {
            List<Shop> categoryMatches = shopRepository.findByCategoryInAndIdNotIn(
                    new ArrayList<>(preferredCategories),
                    new ArrayList<>(excludedIds));
            recommendations.addAll(categoryMatches);
        }

        // 4. Fill remaining slots with Trending shops (if not enough category matches)
        if (recommendations.size() < 10) {
            List<Shop> trendingFillers = shopRepository.findTop10ByIsActiveTrueAndIdNotInOrderByTrendingScoreDesc(
                    new ArrayList<>(excludedIds));

            for (Shop shop : trendingFillers) {
                if (!recommendations.contains(shop)) {
                    recommendations.add(shop);
                }
                if (recommendations.size() >= 10)
                    break;
            }
        }

        List<Shop> finalRecommendations = recommendations.stream().limit(10).collect(Collectors.toList());

        if (finalRecommendations.isEmpty()) {
            return finalRecommendations;
        }

        // Fetch with photos to ensure no LazyInitializationException
        List<Long> ids = finalRecommendations.stream().map(Shop::getId).collect(Collectors.toList());
        // Use findByIdInWithPhotos from ShopRepository (need to expose it or cast)
        // Since RecommendationService has ShopRepository, we can just call it if it's
        // there.
        // But wait, findByIdInWithPhotos is in ShopRepository interface.
        return shopRepository.findByIdInWithPhotos(ids);
    }
}
