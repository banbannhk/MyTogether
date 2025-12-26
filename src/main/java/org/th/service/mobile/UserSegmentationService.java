package org.th.service.mobile;

import org.th.repository.*;
import org.th.entity.*;
import org.th.entity.shops.*;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.th.entity.User;
import org.th.entity.enums.UserSegment;
import org.th.repository.UserActivityRepository;
import org.th.repository.UserRepository;
import org.th.repository.ShopReviewRepository;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

/**
 * Service for user behavior analysis and segmentation
 */
@Service
@RequiredArgsConstructor
public class UserSegmentationService {

    private final UserRepository userRepository;
    private final UserActivityRepository userActivityRepository;
    private final org.th.repository.UserMenuFavoriteRepository userMenuFavoriteRepository; // Injected
    // Removed unused FavoriteRepository
    private final ShopReviewRepository shopReviewRepository;

    /**
     * Classify user based on activity patterns (RFM-style scoring)
     */
    public UserSegment classifyUser(String username) {
        User user = userRepository.findByUsername(username).orElse(null);
        if (user == null) {
            return UserSegment.CASUAL;
        }

        Long userId = user.getId();
        LocalDateTime now = LocalDateTime.now();

        // Check if new user (registered within last 7 days)
        long daysSinceRegistration = ChronoUnit.DAYS.between(user.getCreatedAt(), now);
        if (daysSinceRegistration <= 7) {
            return UserSegment.NEW_USER;
        }

        // Use consolidated query (1 query instead of 4)
        LocalDateTime thirtyDaysAgo = now.minusDays(30);
        org.th.dto.UserEngagementDTO engagement = userRepository.getUserEngagement(userId, thirtyDaysAgo);

        long totalActivities = engagement.getTotalActivities();
        long totalFavorites = engagement.getTotalFavorites();

        // Add Menu Favorites to total favorites count
        long totalMenuFavorites = userMenuFavoriteRepository.countByUserId(userId);
        totalFavorites += totalMenuFavorites;

        long totalReviews = engagement.getTotalReviews();
        long recentActivities = engagement.getRecentActivities();

        if (recentActivities == 0 && totalActivities > 0) {
            return UserSegment.DORMANT;
        }

        // Calculate engagement score
        double engagementScore = calculateEngagementScore(totalActivities, totalFavorites, totalReviews);

        // Classify based on engagement
        if (engagementScore >= 50) {
            return UserSegment.POWER_USER;
        } else {
            return UserSegment.CASUAL;
        }
    }

    /**
     * Calculate engagement score (0-100)
     * Weights: Activities (40%), Favorites (30%), Reviews (30%)
     */
    private double calculateEngagementScore(long activities, long favorites, long reviews) {
        // Normalize values (cap at reasonable maximums)
        double activityScore = Math.min(activities / 100.0, 1.0) * 40;
        double favoriteScore = Math.min(favorites / 20.0, 1.0) * 30;
        double reviewScore = Math.min(reviews / 10.0, 1.0) * 30;

        return activityScore + favoriteScore + reviewScore;
    }

    /**
     * Get a human-readable description for the user segment
     */
    public String getUserSegmentDescription(UserSegment segment) {
        return switch (segment) {
            case NEW_USER -> "Welcome! Discovering new places";
            case CASUAL -> "Occasional explorer";
            case POWER_USER -> "Food enthusiast";
            case DORMANT -> "Welcome back!";
        };
    }
}
