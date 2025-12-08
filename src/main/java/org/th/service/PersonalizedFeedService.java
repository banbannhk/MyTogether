package org.th.service;

import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.th.dto.feed.*;
import org.th.entity.User;
import org.th.entity.enums.*;
import org.th.entity.shops.Favorite;
import org.th.entity.shops.Shop;
import org.th.repository.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Core service for generating personalized feed
 */
@Service
@RequiredArgsConstructor
public class PersonalizedFeedService {

    private static final Logger logger = LoggerFactory.getLogger(PersonalizedFeedService.class);

    private final ShopRepository shopRepository;
    private final UserRepository userRepository;
    private final FavoriteRepository favoriteRepository;
    private final TimeContextService timeContextService;
    private final UserSegmentationService userSegmentationService;

    private static final int SECTION_LIMIT = 10;
    private static final double DEFAULT_RADIUS_KM = 5.0;

    /**
     * Generate complete personalized feed for a user
     */
    @Transactional(readOnly = true)
    public PersonalizedFeedDTO generatePersonalizedFeed(String username, Double latitude, Double longitude,
            Double radiusKm) {
        logger.info("Generating personalized feed for user: {}", username);

        User user = userRepository.findByUsername(username).orElse(null);
        if (user == null) {
            throw new RuntimeException("User not found: " + username);
        }

        // Use default radius if not provided
        double radius = (radiusKm != null && radiusKm > 0) ? radiusKm : DEFAULT_RADIUS_KM;
        boolean hasLocation = (latitude != null && longitude != null);

        // Get user context
        UserSegment userSegment = userSegmentationService.classifyUser(username);
        TimeContext timeContext = timeContextService.getCurrentTimeContext();

        // Build feed sections
        FeedSectionDTO forYouNow = buildForYouNowSection(user, latitude, longitude, radius, timeContext, hasLocation);
        FeedSectionDTO trendingNearby = buildTrendingNearbySection(latitude, longitude, radius, hasLocation);
        FeedSectionDTO basedOnFavorites = buildBasedOnFavoritesSection(user);
        FeedSectionDTO newShops = buildNewShopsSection(user, latitude, longitude, radius, hasLocation);

        // Build metadata
        FeedMetadataDTO metadata = FeedMetadataDTO.builder()
                .generatedAt(LocalDateTime.now())
                .userSegment(userSegment)
                .locationUsed(hasLocation)
                .timeContext(timeContext)
                .userLatitude(latitude)
                .userLongitude(longitude)
                .radiusKm(radius)
                .build();

        return PersonalizedFeedDTO.builder()
                .forYouNow(forYouNow)
                .trendingNearby(trendingNearby)
                .basedOnFavorites(basedOnFavorites)
                .newShops(newShops)
                .metadata(metadata)
                .build();
    }

    /**
     * Build "For You Now" section - personalized based on time, location, and
     * preferences
     */
    private FeedSectionDTO buildForYouNowSection(User user, Double latitude, Double longitude,
            double radius, TimeContext timeContext, boolean hasLocation) {
        List<Shop> shops = new ArrayList<>();

        // Get time-relevant categories
        List<String> timeCategories = timeContextService.getRelevantCategoriesForTime(timeContext);

        if (hasLocation) {
            // Get nearby shops in relevant categories (optimized - filtering in database)
            shops = shopRepository.findNearbyShopsByCategories(
                    latitude, longitude, radius, timeCategories, SECTION_LIMIT);
        } else {
            // Fallback: get shops by time-relevant categories
            shops = shopRepository.findByCategoryIn(timeCategories).stream()
                    .limit(SECTION_LIMIT)
                    .collect(Collectors.toList());
        }

        List<ShopFeedItemDTO> feedItems = shops.stream()
                .map(shop -> convertToFeedItem(shop, latitude, longitude,
                        "Perfect for " + timeContext.name().toLowerCase(),
                        timeContext.getLabelMm() + " အတွက် အထူးသင့်တော်သည်"))
                .collect(Collectors.toList());

        return FeedSectionDTO.builder()
                .title("For You Now")
                .titleMm("သင့်အတွက် ယခုအချိန်က")
                .description(timeContextService.getTimeContextDescription(timeContext))
                .sectionType(FeedSectionType.FOR_YOU)
                .shops(feedItems)
                .totalCount(feedItems.size())
                .build();
    }

    /**
     * Build "Trending Nearby" section
     */
    private FeedSectionDTO buildTrendingNearbySection(Double latitude, Double longitude,
            double radius, boolean hasLocation) {
        List<Shop> shops;

        if (hasLocation) {
            shops = shopRepository.findNearbyTrendingShops(latitude, longitude, radius, SECTION_LIMIT);
        } else {
            shops = shopRepository.findTop10ByOrderByTrendingScoreDesc();
        }

        List<ShopFeedItemDTO> feedItems = shops.stream()
                .map(shop -> convertToFeedItem(shop, latitude, longitude, "Trending in your area",
                        "သင့်ဒေသတွင် ရေပန်းစားနေသည်"))
                .collect(Collectors.toList());

        return FeedSectionDTO.builder()
                .title("Trending Nearby")
                .titleMm("အနီးနားရှိ ရေပန်းစားနေသော")
                .description(hasLocation ? "Popular spots near you" : "Trending shops")
                .sectionType(FeedSectionType.TRENDING_NEARBY)
                .shops(feedItems)
                .totalCount(feedItems.size())
                .build();
    }

    /**
     * Build "Based on Favorites" section
     */
    private FeedSectionDTO buildBasedOnFavoritesSection(User user) {
        // Use JOIN FETCH to avoid N+1 query
        List<Favorite> favorites = favoriteRepository.findByUserIdWithShopAndPhotos(user.getId());

        // Extract preferred categories
        Set<String> preferredCategories = favorites.stream()
                .map(fav -> fav.getShop().getCategory())
                .collect(Collectors.toSet());

        // Get excluded shop IDs (already favorited)
        Set<Long> excludedIds = favorites.stream()
                .map(fav -> fav.getShop().getId())
                .collect(Collectors.toSet());

        if (excludedIds.isEmpty()) {
            excludedIds.add(-1L); // Dummy ID to avoid empty list
        }

        List<Shop> shops = new ArrayList<>();
        if (!preferredCategories.isEmpty()) {
            shops = shopRepository.findByCategoryInAndIdNotIn(
                    new ArrayList<>(preferredCategories),
                    new ArrayList<>(excludedIds)).stream().limit(SECTION_LIMIT).collect(Collectors.toList());
        }

        // Fallback to trending if no favorites
        if (shops.isEmpty()) {
            shops = shopRepository.findTop10ByOrderByTrendingScoreDesc();
        }

        List<ShopFeedItemDTO> feedItems = shops.stream()
                .map(shop -> convertToFeedItem(shop, null, null, "Similar to your favorites",
                        "သင့်ကြိုက်နှစ်သက်မှုများနှင့် ဆင်တူသည်"))
                .collect(Collectors.toList());

        return FeedSectionDTO.builder()
                .title("Based on Your Favorites")
                .titleMm("သင့်အကြိုက်များအပေါ် အခြေခံထားသော")
                .description(preferredCategories.isEmpty() ? "Discover new places" : "More places you might like")
                .sectionType(FeedSectionType.BASED_ON_FAVORITES)
                .shops(feedItems)
                .totalCount(feedItems.size())
                .build();
    }

    /**
     * Build "New Shops" section
     */
    private FeedSectionDTO buildNewShopsSection(User user, Double latitude, Double longitude,
            double radius, boolean hasLocation) {
        LocalDateTime thirtyDaysAgo = LocalDateTime.now().minusDays(30);

        // Get user's preferred categories
        // Use JOIN FETCH to avoid N+1 query
        List<Favorite> favorites = favoriteRepository.findByUserIdWithShopAndPhotos(user.getId());
        Set<String> preferredCategories = favorites.stream()
                .map(fav -> fav.getShop().getCategory())
                .collect(Collectors.toSet());

        List<Shop> shops;
        if (!preferredCategories.isEmpty()) {
            shops = shopRepository.findRecentShopsByCategories(
                    new ArrayList<>(preferredCategories),
                    thirtyDaysAgo).stream().limit(SECTION_LIMIT).collect(Collectors.toList());
        } else {
            // Fallback: all recent shops
            // Use paginated query instead of loading all shops
            shops = shopRepository.findRecentShops(
                    thirtyDaysAgo,
                    org.springframework.data.domain.PageRequest.of(0, SECTION_LIMIT)).getContent();
        }

        List<ShopFeedItemDTO> feedItems = shops.stream()
                .map(shop -> convertToFeedItem(shop, latitude, longitude, "Recently added",
                        "မကြာသေးမီက ထည့်သွင်းထားသည်"))
                .collect(Collectors.toList());

        return FeedSectionDTO.builder()
                .title("New Shops")
                .titleMm("ဆိုင်အသစ်များ")
                .description("Recently added places")
                .sectionType(FeedSectionType.NEW_SHOPS)
                .shops(feedItems)
                .totalCount(feedItems.size())
                .build();
    }

    /**
     * Convert Shop entity to ShopFeedItemDTO with badges and relevance
     */
    private ShopFeedItemDTO convertToFeedItem(Shop shop, Double userLat, Double userLon, String matchReason,
            String matchReasonMm) {
        List<ShopBadge> badges = calculateShopBadges(shop);
        List<String> badgeLabelsMm = badges.stream().map(ShopBadge::getLabelMm).collect(Collectors.toList());
        Double distance = null;

        if (userLat != null && userLon != null) {
            distance = calculateDistance(
                    userLat, userLon,
                    shop.getLatitude().doubleValue(),
                    shop.getLongitude().doubleValue());
        }

        return ShopFeedItemDTO.builder()
                .id(shop.getId())
                .name(shop.getName())
                .nameMm(shop.getNameMm())
                .category(shop.getCategory())
                .subCategory(shop.getSubCategory())
                .ratingAvg(shop.getRatingAvg())
                .ratingCount(shop.getRatingCount())
                .address(shop.getAddress())
                .township(shop.getTownship())
                .latitude(shop.getLatitude())
                .longitude(shop.getLongitude())
                .distanceKm(distance)
                .badges(badges)
                .badgeLabelsMm(badgeLabelsMm)
                .relevanceScore(calculateRelevanceScore(shop))
                .matchReason(matchReason)
                .matchReasonMm(matchReasonMm)
                .hasDelivery(shop.getHasDelivery())
                .hasParking(shop.getHasParking())
                .hasWifi(shop.getHasWifi())
                .trendingScore(shop.getTrendingScore())
                .build();
    }

    /**
     * Calculate dynamic badges for a shop
     */
    private List<ShopBadge> calculateShopBadges(Shop shop) {
        List<ShopBadge> badges = new ArrayList<>();

        // TRENDING_NOW: High trending score
        if (shop.getTrendingScore() != null && shop.getTrendingScore() > 50) {
            badges.add(ShopBadge.TRENDING_NOW);
        }

        // NEW: Created within last 30 days
        if (shop.getCreatedAt() != null) {
            long daysOld = ChronoUnit.DAYS.between(shop.getCreatedAt(), LocalDateTime.now());
            if (daysOld <= 30) {
                badges.add(ShopBadge.NEW);
            }
        }

        // HIDDEN_GEM: High rating but low view count
        if (shop.getRatingAvg() != null && shop.getRatingAvg().compareTo(new BigDecimal("4.0")) >= 0 &&
                shop.getViewCount() != null && shop.getViewCount() < 100) {
            badges.add(ShopBadge.HIDDEN_GEM);
        }

        // CROWD_FAVORITE: High rating count
        if (shop.getRatingCount() != null && shop.getRatingCount() > 50) {
            badges.add(ShopBadge.CROWD_FAVORITE);
        }

        // RISING_STAR: New shop with good trending score
        if (shop.getCreatedAt() != null && shop.getTrendingScore() != null) {
            long daysOld = ChronoUnit.DAYS.between(shop.getCreatedAt(), LocalDateTime.now());
            if (daysOld <= 60 && shop.getTrendingScore() > 30) {
                badges.add(ShopBadge.RISING_STAR);
            }
        }

        return badges;
    }

    /**
     * Calculate relevance score (0-100)
     */
    private Double calculateRelevanceScore(Shop shop) {
        double score = 0.0;

        // Rating contribution (40%)
        if (shop.getRatingAvg() != null) {
            score += (shop.getRatingAvg().doubleValue() / 5.0) * 40;
        }

        // Trending contribution (40%)
        if (shop.getTrendingScore() != null) {
            score += Math.min(shop.getTrendingScore() / 100.0, 1.0) * 40;
        }

        // Review count contribution (20%)
        if (shop.getRatingCount() != null) {
            score += Math.min(shop.getRatingCount() / 50.0, 1.0) * 20;
        }

        return Math.min(score, 100.0);
    }

    /**
     * Calculate distance between two coordinates using Haversine formula
     */
    private Double calculateDistance(double lat1, double lon1, double lat2, double lon2) {
        final int EARTH_RADIUS_KM = 6371;

        double dLat = Math.toRadians(lat2 - lat1);
        double dLon = Math.toRadians(lon2 - lon1);

        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2) +
                Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2)) *
                        Math.sin(dLon / 2) * Math.sin(dLon / 2);

        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));

        return EARTH_RADIUS_KM * c;
    }
}
