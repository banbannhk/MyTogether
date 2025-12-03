package org.th.service;

import lombok.RequiredArgsConstructor;
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

    /**
     * Get personalized recommendations for a user
     */
    @Transactional(readOnly = true)
    public List<Shop> getRecommendedShops(String username) {
        User user = userRepository.findByUsername(username).orElse(null);
        if (user == null)
            return new ArrayList<>();

        Long userId = user.getId();

        // 1. Get user's interaction history
        List<Favorite> favorites = favoriteRepository.findByUserIdOrderByCreatedAtDesc(userId);
        List<Review> reviews = reviewRepository.findByUserIdOrderByCreatedAtDesc(userId);

        // 2. Identify excluded IDs (shops user already knows)
        Set<Long> excludedIds = new HashSet<>();
        Set<String> preferredCategories = new HashSet<>();

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

        return recommendations.stream().limit(10).collect(Collectors.toList());
    }
}
