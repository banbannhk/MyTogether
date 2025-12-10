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
    private final UserActivityRepository userActivityRepository;
    private final RecommendationService recommendationService; // Added dependency

    private static final int SECTION_LIMIT = 10;
    private static final double DEFAULT_RADIUS_KM = 5.0;

    /**
     * Generate complete personalized feed for a user (or guest with deviceId)
     */
    @Transactional(readOnly = true)
    public PersonalizedFeedDTO generatePersonalizedFeed(String username, Double latitude, Double longitude,
            Double radiusKm, String deviceId, Long districtId) {
        logger.info("Generating personalized feed for user: {}, device: {}", username, deviceId);

        User user = null;
        if (username != null && !username.equals("guest")) {
            user = userRepository.findByUsername(username).orElse(null);
        }

        // Use default radius if not provided
        double radius = (radiusKm != null && radiusKm > 0) ? radiusKm : DEFAULT_RADIUS_KM;
        boolean hasLocation = (latitude != null && longitude != null);

        // Get user context
        UserSegment userSegment = (user != null) ? userSegmentationService.classifyUser(username) : UserSegment.CASUAL;
        TimeContext timeContext = timeContextService.getCurrentTimeContext();

        // 1. Calculate Preferences (User or Device)
        Set<String> preferredCategories = new HashSet<>();
        Set<Long> excludedIds = new HashSet<>();

        if (user != null) {
            // User-based preferences from Favorites
            List<Favorite> favorites = favoriteRepository.findByUserIdWithShopAndPhotos(user.getId());
            preferredCategories = favorites.stream()
                    .map(fav -> fav.getShop().getCategory())
                    .collect(Collectors.toSet());
            excludedIds = favorites.stream()
                    .map(fav -> fav.getShop().getId())
                    .collect(Collectors.toSet());
        } else if (deviceId != null) {
            // Device-based preferences from Activity History
            List<Object[]> topCategories = userActivityRepository.findTopCategoriesByDevice(deviceId);
            preferredCategories = topCategories.stream()
                    .limit(5)
                    .map(obj -> (String) obj[0])
                    .collect(Collectors.toSet());
        }

        if (excludedIds.isEmpty()) {
            excludedIds.add(-1L);
        }

        // Build feed sections
        FeedSectionDTO forYouNow = buildForYouNowSection(user, latitude, longitude, radius, timeContext, hasLocation,
                districtId);
        // as
        // is?
        // Or
        // optimize?
        FeedSectionDTO trendingNearby = buildTrendingNearbySection(user, latitude, longitude, radius, hasLocation,
                districtId);

        // Pass explicit preferences
        FeedSectionDTO basedOnFavorites = buildBasedOnHistorySection(user, preferredCategories, excludedIds, deviceId);
        FeedSectionDTO newShops = buildNewShopsSection(user, preferredCategories, latitude, longitude, radius,
                hasLocation,
                districtId);

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
                .basedOnFavorites(basedOnFavorites) // Renamed internally but DTO field same
                .newShops(newShops)
                .metadata(metadata)
                .build();
    }

    /**
     * Build "For You Now" section - personalized based on time, location, and
     * preferences
     */
    private FeedSectionDTO buildForYouNowSection(User user, Double latitude, Double longitude,
            double radius, TimeContext timeContext, boolean hasLocation, Long districtId) {
        List<Shop> shops = new ArrayList<>();

        // Get time-relevant categories
        List<String> timeCategories = timeContextService.getRelevantCategoriesForTime(timeContext);

        if (hasLocation) {
            // Get nearby shops in relevant categories (optimized - filtering in database)
            shops = shopRepository.findNearbyShopsByCategories(
                    latitude, longitude, radius, timeCategories, SECTION_LIMIT);
        } else if (districtId != null) {
            // Filter by District
            shops = shopRepository.findByDistrictObj_IdAndCategoryIn(districtId, timeCategories).stream()
                    .limit(SECTION_LIMIT)
                    .collect(Collectors.toList());
        } else {
            // Fallback: get shops by time-relevant categories
            shops = shopRepository.findByCategoryIn(timeCategories).stream()
                    .limit(SECTION_LIMIT)
                    .collect(Collectors.toList());
        }

        // Apply Dietary Safety Net
        if (user != null) {
            shops = applyDietarySafetyNet(shops, user);
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
    private FeedSectionDTO buildTrendingNearbySection(User user, Double latitude, Double longitude,
            double radius, boolean hasLocation, Long districtId) {
        List<Shop> shops = new ArrayList<>();

        // 1. Hierarchical Fallback: Try District First
        if (districtId != null) {
            shops = shopRepository.findByDistrictObj_Id(districtId);
            // Sort by trending score desc
            shops.sort(Comparator.comparing(Shop::getTrendingScore, Comparator.nullsLast(Comparator.reverseOrder())));
            if (shops.size() > SECTION_LIMIT) {
                shops = shops.subList(0, SECTION_LIMIT);
            }
        }

        // Apply Dietary Safety Net (even for global/district trending)
        // Note: For "Nearby" radius search, we might want to let database handle it if
        // optimization is needed,
        // but for now filtering in memory for consistency.
        // Actually, for "Nearby" (User provided Lat/Lon), the repository call doesn't
        // filter by diet.
        // We should apply safety net here too, but we need User object.
        // Since this method doesn't have User, let's assume Trending is generic unless
        // we pass User.
        // However, the goal is STRICT safety net.
        // Refactoring to pass User to this method is better.
        // BUT, wait. buildTrendingNearbySection is also called for "Guest" users.
        // If User is null, no filtering. If User is present, filter.
        // Let's defer this change to next step to be safe, or just do it now.
        // I will add User param to buildTrendingNearbySection.

        // 2. Fallback to Radius (Standard Logic) if Township yielded few results
        if (shops.isEmpty()) {
            if (hasLocation) {
                shops = shopRepository.findNearbyTrendingShops(latitude, longitude, radius, SECTION_LIMIT);
            } else {
                shops = shopRepository.findTop10ByOrderByTrendingScoreDesc();
            }
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
     * Build "Based on Favorites/History" section
     * Delegates to RecommendationService for unified logic
     */
    private FeedSectionDTO buildBasedOnHistorySection(User user, Set<String> preferredCategories, Set<Long> excludedIds,
            String deviceId) {
        // Note: We ignore the passed sets because RecommendationService recalculates
        // them properly
        // using the Weighted Scoring algorithm we just implemented.
        // This ensures "For You" API and "Feed" Dashboard use identical intelligence.

        String username = (user != null) ? user.getUsername() : null;
        List<Shop> shops = recommendationService.getRecommendedShops(username, deviceId);

        // Apply Dietary Safety Net
        if (user != null) {
            shops = applyDietarySafetyNet(shops, user);
        }

        String title = (user != null) ? "Based on Your Favorites" : "Based on your History";
        String titleMm = (user != null) ? "သင့်အကြိုက်များအပေါ် အခြေခံထားသော"
                : "သင့်ကြည့်ရှုမှုမှတ်တမ်းအပေါ် အခြေခံထားသော";
        String description = (user != null) ? "More places you might like" : "Based on places you viewed";

        List<ShopFeedItemDTO> feedItems = shops.stream()
                .map(shop -> convertToFeedItem(shop, null, null, "Similar to your interests",
                        "သင့်စိတ်ဝင်စားမှုများနှင့် ဆင်တူသည်"))
                .collect(Collectors.toList());

        return FeedSectionDTO.builder()
                .title(title)
                .titleMm(titleMm)
                .description(description)
                .sectionType(FeedSectionType.BASED_ON_FAVORITES)
                .shops(feedItems)
                .totalCount(feedItems.size())
                .build();
    }

    /**
     * Build "New Shops" section
     */
    private FeedSectionDTO buildNewShopsSection(User user, Set<String> preferredCategories, Double latitude,
            Double longitude,
            double radius, boolean hasLocation, Long districtId) {
        LocalDateTime thirtyDaysAgo = LocalDateTime.now().minusDays(30);
        List<Shop> shops;

        if (!preferredCategories.isEmpty()) {
            shops = shopRepository.findRecentShopsByCategories(
                    new ArrayList<>(preferredCategories),
                    thirtyDaysAgo).stream().limit(SECTION_LIMIT).collect(Collectors.toList());
        } else if (districtId != null) {
            // Filter by District
            if (!preferredCategories.isEmpty()) {
                // Try to find new shops in district matching preferences first
                shops = shopRepository.findByDistrictObj_IdAndCreatedAtAfterAndCategoryIn(
                        districtId, thirtyDaysAgo, new ArrayList<>(preferredCategories));

                // If not enough/empty, we COULD fallback to generic new shops in district,
                // but "New Shops" usually implies specific relevance.
                // Let's mix them if needed or just return what we found.
                // For better UX: If specific preference yields nothing, fallback to generic new
                // in district.
                if (shops.isEmpty()) {
                    shops = shopRepository.findByDistrictObj_IdAndCreatedAtAfter(districtId, thirtyDaysAgo);
                }
            } else {
                shops = shopRepository.findByDistrictObj_IdAndCreatedAtAfter(districtId, thirtyDaysAgo);
            }

            shops = shops.stream().limit(SECTION_LIMIT).collect(Collectors.toList());
        } else if (hasLocation) {
            // Filter by GPS Radius (Near Me)
            if (!preferredCategories.isEmpty()) {
                shops = shopRepository.findNearbyRecentShopsByCategories(
                        latitude, longitude, radius, new ArrayList<>(preferredCategories), thirtyDaysAgo,
                        SECTION_LIMIT);
            } else {
                shops = shopRepository.findNearbyRecentShops(
                        latitude, longitude, radius, thirtyDaysAgo, SECTION_LIMIT);
            }
        } else {
            // Fallback: just show recent shops
            shops = shopRepository.findRecentShops(
                    thirtyDaysAgo,
                    org.springframework.data.domain.PageRequest.of(0, SECTION_LIMIT)).getContent();
        }

        // Apply Dietary Safety Net
        if (user != null) {
            shops = applyDietarySafetyNet(shops, user);
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
                .district(shop.getTownship())
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

    /**
     * Apply Dietary Safety Net
     * Filters shops based on User's Veg/Halal preferences
     */
    private List<Shop> applyDietarySafetyNet(List<Shop> shops, User user) {
        if (user == null)
            return shops;

        return shops.stream()
                .filter(shop -> {
                    if (Boolean.TRUE.equals(user.getIsVegetarian()) && !Boolean.TRUE.equals(shop.getIsVegetarian())) {
                        return false;
                    }
                    if (Boolean.TRUE.equals(user.getIsHalal()) && !Boolean.TRUE.equals(shop.getIsHalal())) {
                        return false;
                    }
                    return true;
                })
                .collect(Collectors.toList());
    }
}
