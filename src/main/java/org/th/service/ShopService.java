package org.th.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.th.entity.shops.Shop;
import org.th.repository.ShopRepository;

import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class ShopService {

    private static final Logger logger = LoggerFactory.getLogger(ShopService.class);

    @Autowired
    private ShopRepository shopRepository;

    private static final Double DEFAULT_RADIUS_KM = 5.0;
    private static final Double MAX_RADIUS_KM = 50.0;

    /**
     * Get shops near a user's location
     * 
     * @param userLatitude  User's current latitude
     * @param userLongitude User's current longitude
     * @param radiusInKm    Search radius in kilometers (default: 5km, max: 50km)
     * @return List of nearby shops sorted by distance
     */
    public List<Shop> getNearbyShops(Double userLatitude, Double userLongitude, Double radiusInKm) {
        logger.info("Searching for shops near [{}, {}] within {} km", userLatitude, userLongitude, radiusInKm);

        // Validate and normalize radius
        Double radius = normalizeRadius(radiusInKm);

        // Query nearby shops
        List<Shop> nearbyShops = shopRepository.findNearbyShops(userLatitude, userLongitude, radius);

        logger.info("Found {} shops within {} km", nearbyShops.size(), radius);
        return nearbyShops;
    }

    /**
     * Get nearby shops filtered by category
     * 
     * @param userLatitude  User's current latitude
     * @param userLongitude User's current longitude
     * @param radiusInKm    Search radius in kilometers
     * @param category      Shop category (e.g., "Restaurant", "Cafe", "Market")
     * @return List of nearby shops in the specified category
     */
    public List<Shop> getNearbyShopsByCategory(Double userLatitude, Double userLongitude,
            Double radiusInKm, String category) {
        logger.info("Searching for {} shops near [{}, {}] within {} km",
                category, userLatitude, userLongitude, radiusInKm);

        Double radius = normalizeRadius(radiusInKm);

        List<Shop> nearbyShops = shopRepository.findNearbyShopsByCategory(
                userLatitude, userLongitude, radius, category);

        logger.info("Found {} {} shops within {} km", nearbyShops.size(), category, radius);
        return nearbyShops;
    }

    /**
     * Get nearby verified shops only
     * 
     * @param userLatitude  User's current latitude
     * @param userLongitude User's current longitude
     * @param radiusInKm    Search radius in kilometers
     * @return List of verified nearby shops
     */
    public List<Shop> getNearbyVerifiedShops(Double userLatitude, Double userLongitude, Double radiusInKm) {
        logger.info("Searching for verified shops near [{}, {}] within {} km",
                userLatitude, userLongitude, radiusInKm);

        List<Shop> nearbyShops = getNearbyShops(userLatitude, userLongitude, radiusInKm);

        // Filter for verified shops only
        List<Shop> verifiedShops = nearbyShops.stream()
                .filter(Shop::getIsVerified)
                .collect(Collectors.toList());

        logger.info("Found {} verified shops out of {} total shops",
                verifiedShops.size(), nearbyShops.size());
        return verifiedShops;
    }

    /**
     * Get nearby shops with minimum rating
     * 
     * @param userLatitude  User's current latitude
     * @param userLongitude User's current longitude
     * @param radiusInKm    Search radius in kilometers
     * @param minRating     Minimum average rating (0.0 to 5.0)
     * @return List of shops meeting rating criteria
     */
    public List<Shop> getNearbyShopsByRating(Double userLatitude, Double userLongitude,
            Double radiusInKm, Double minRating) {
        logger.info("Searching for shops with rating >= {} near [{}, {}] within {} km",
                minRating, userLatitude, userLongitude, radiusInKm);

        Double radius = normalizeRadius(radiusInKm);

        List<Shop> ratedShops = shopRepository.findNearbyShopsByRating(
                userLatitude, userLongitude, radius, minRating);

        logger.info("Found {} shops with rating >= {}", ratedShops.size(), minRating);
        return ratedShops;
    }

    /**
     * Get shop by ID
     *
     * @param shopId Shop ID
     * @return Shop entity
     */
    @Transactional(readOnly = true)
    public Optional<Shop> getShopById(Long shopId) {
        logger.info("Fetching shop with ID: {}", shopId);
        // Use optimized queries to fetch details separately (to avoid
        // MultipleBagFetchException)
        // Hibernate cannot fetch multiple List collections in a single query
        Optional<Shop> shopOpt = shopRepository.findByIdWithDetails(shopId);

        // If shop exists, fetch menu items and operating hours separately
        shopOpt.ifPresent(shop -> {
            // These queries load the collections into the persistence context
            shopRepository.findMenuCategoriesWithItems(shopId);
            shopRepository.findOperatingHoursByShopId(shopId);
        });

        return shopOpt;
    }

    /**
     * Get shop by slug
     * 
     * @param slug Shop slug (URL-friendly name)
     * @return Shop entity
     */
    public Shop getShopBySlug(String slug) {
        logger.info("Fetching shop with slug: {}", slug);
        return shopRepository.findBySlug(slug);
    }

    /**
     * Get all shops with pagination
     * 
     * @param pageable Pagination information
     * @return Page of shops
     */
    public Page<Shop> getAllShops(Pageable pageable) {
        logger.info("Fetching all shops page: {}", pageable.getPageNumber());
        return shopRepository.findAll(pageable);
    }

    /**
     * Get all shops (Deprecated: Use pagination version)
     * 
     * @return List of all shops
     */
    public List<Shop> getAllShops() {
        logger.info("Fetching all shops");
        return shopRepository.findAll();
    }

    /**
     * Get all shops in a specific category
     * 
     * @param category Shop category
     * @return List of shops in category
     */
    @Cacheable("shopsByCategory")
    public List<Shop> getShopsByCategory(String category) {
        logger.info("Fetching shops in category: {}", category);
        return shopRepository.findByCategory(category);
    }

    /**
     * Get all verified shops
     * 
     * @return List of verified shops
     */
    public List<Shop> getVerifiedShops() {
        logger.info("Fetching all verified shops");
        return shopRepository.findByIsActiveTrueAndIsVerifiedTrue();
    }

    /**
     * Get shops by township
     * 
     * @param township Township name
     * @return List of shops in township
     */
    @Cacheable("shopsByTownship")
    public List<Shop> getShopsByTownship(String township) {
        logger.info("Fetching shops in township: {}", township);
        return shopRepository.findByTownship(township);
    }

    /**
     * Save or update a shop
     * 
     * @param shop Shop entity to save
     * @return Saved shop entity
     */
    public Shop saveShop(Shop shop) {
        logger.info("Saving shop: {}", shop.getName());
        return shopRepository.save(shop);
    }

    /**
     * Delete a shop by ID
     * 
     * @param shopId Shop ID to delete
     */
    public void deleteShop(Long shopId) {
        logger.info("Deleting shop with ID: {}", shopId);
        shopRepository.deleteById(shopId);
    }

    /**
     * Normalize and validate radius
     * 
     * @param radiusInKm Requested radius
     * @return Normalized radius (default: 5km, max: 50km)
     */
    private Double normalizeRadius(Double radiusInKm) {
        if (radiusInKm == null || radiusInKm <= 0) {
            logger.debug("Invalid radius {}, using default {} km", radiusInKm, DEFAULT_RADIUS_KM);
            return DEFAULT_RADIUS_KM;
        }

        if (radiusInKm > MAX_RADIUS_KM) {
            logger.debug("Radius {} exceeds maximum, capping at {} km", radiusInKm, MAX_RADIUS_KM);
            return MAX_RADIUS_KM;
        }

        return radiusInKm;
    }

    /**
     * Calculate distance between two points using Haversine formula
     * Useful for client-side distance calculations or verification
     * 
     * @param lat1 Latitude of point 1
     * @param lon1 Longitude of point 1
     * @param lat2 Latitude of point 2
     * @param lon2 Longitude of point 2
     * @return Distance in kilometers
     */
    public static double calculateDistance(double lat1, double lon1, double lat2, double lon2) {
        final int EARTH_RADIUS_KM = 6371;

        double latDistance = Math.toRadians(lat2 - lat1);
        double lonDistance = Math.toRadians(lon2 - lon1);

        double a = Math.sin(latDistance / 2) * Math.sin(latDistance / 2)
                + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2))
                        * Math.sin(lonDistance / 2) * Math.sin(lonDistance / 2);

        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));

        return EARTH_RADIUS_KM * c;
    }

    // ==================== SEARCH METHODS ====================

    /**
     * Universal search - searches both shop names and menu items
     * 
     * @param keyword Search keyword
     * @return List of shops matching the keyword
     */
    public List<Shop> searchShops(String keyword) {
        logger.info("Searching shops with keyword: {}", keyword);
        List<Shop> results = shopRepository.searchShops(keyword);
        logger.info("Found {} shops matching '{}'", results.size(), keyword);
        return results;
    }

    /**
     * Search shops by shop name only
     * 
     * @param name Shop name to search
     * @return List of shops matching the name
     */
    public List<Shop> searchByShopName(String name) {
        logger.info("Searching shops by name: {}", name);
        List<Shop> results = shopRepository.searchByShopName(name);
        logger.info("Found {} shops matching name '{}'", results.size(), name);
        return results;
    }

    /**
     * Search shops by food/menu item name
     * 
     * @param foodName Food name to search
     * @return List of shops that have the matching food item
     */
    public List<Shop> searchByFoodName(String foodName) {
        logger.info("Searching shops by food name: {}", foodName);
        List<Shop> results = shopRepository.searchByMenuItemName(foodName);
        logger.info("Found {} shops with food matching '{}'", results.size(), foodName);
        return results;
    }

    // ==================== DTO MAPPING METHODS ====================

    /**
     * Convert Shop entity to ShopListDTO (lightweight view)
     * 
     * @param shop Shop entity
     * @return ShopListDTO
     */
    public org.th.dto.ShopListDTO convertToListDTO(Shop shop) {
        if (shop == null) {
            return null;
        }

        // Get primary photo URL
        String primaryPhotoUrl = shop.getPhotos().stream()
                .filter(photo -> photo.getIsPrimary() != null && photo.getIsPrimary())
                .findFirst()
                .map(org.th.entity.shops.ShopPhoto::getUrl)
                .orElse(shop.getPhotos().isEmpty() ? null : shop.getPhotos().get(0).getUrl());

        return org.th.dto.ShopListDTO.builder()
                .id(shop.getId())
                .name(shop.getName())
                .nameMm(shop.getNameMm())
                .nameEn(shop.getNameEn())
                .slug(shop.getSlug())
                .category(shop.getCategory())
                .subCategory(shop.getSubCategory())
                .address(shop.getAddress())
                .addressMm(shop.getAddressMm())
                .township(shop.getTownship())
                .city(shop.getCity())
                .latitude(shop.getLatitude())
                .longitude(shop.getLongitude())
                .ratingAvg(shop.getRatingAvg())
                .ratingCount(shop.getRatingCount())
                .primaryPhotoUrl(primaryPhotoUrl)
                .hasDelivery(shop.getHasDelivery())
                .hasParking(shop.getHasParking())
                .hasWifi(shop.getHasWifi())
                .isVerified(shop.getIsVerified())
                .build();
    }

    /**
     * Convert Shop entity to ShopDetailDTO (complete view with relationships)
     * 
     * @param shop Shop entity
     * @return ShopDetailDTO
     */
    public org.th.dto.ShopDetailDTO convertToDetailDTO(Shop shop) {
        if (shop == null) {
            return null;
        }

        // Convert photos
        List<org.th.dto.ShopPhotoDTO> photoDTOs = shop.getPhotos().stream()
                .map(photo -> org.th.dto.ShopPhotoDTO.builder()
                        .id(photo.getId())
                        .url(photo.getUrl())
                        .thumbnailUrl(photo.getThumbnailUrl())
                        .photoType(photo.getPhotoType())
                        .caption(photo.getCaption())
                        .captionMm(photo.getCaptionMm())
                        .captionEn(photo.getCaptionEn())
                        .isPrimary(photo.getIsPrimary())
                        .displayOrder(photo.getDisplayOrder())
                        .uploadedAt(photo.getUploadedAt())
                        .build())
                .collect(Collectors.toList());

        // Convert menu categories with items
        List<org.th.dto.MenuCategoryDTO> menuCategoryDTOs = shop.getMenuCategories().stream()
                .map(category -> {
                    List<org.th.dto.MenuItemDTO> itemDTOs = category.getItems().stream()
                            .map(item -> org.th.dto.MenuItemDTO.builder()
                                    .id(item.getId())
                                    .name(item.getName())
                                    .nameMm(item.getNameMm())
                                    .nameEn(item.getNameEn())
                                    .price(item.getPrice())
                                    .currency(item.getCurrency())
                                    .imageUrl(item.getImageUrl())
                                    .isAvailable(item.getIsAvailable())
                                    .isPopular(item.getIsPopular())
                                    .isVegetarian(item.getIsVegetarian())
                                    .isSpicy(item.getIsSpicy())
                                    .displayOrder(item.getDisplayOrder())
                                    .build())
                            .collect(Collectors.toList());

                    return org.th.dto.MenuCategoryDTO.builder()
                            .id(category.getId())
                            .name(category.getName())
                            .nameMm(category.getNameMm())
                            .nameEn(category.getNameEn())
                            .displayOrder(category.getDisplayOrder())
                            .isActive(category.getIsActive())
                            .items(itemDTOs)
                            .build();
                })
                .collect(Collectors.toList());

        // Convert recent reviews (limit to 10 most recent)
        List<org.th.dto.ReviewSummaryDTO> reviewDTOs = shop.getReviews().stream()
                .filter(review -> review.getIsVisible() != null && review.getIsVisible())
                .sorted((r1, r2) -> r2.getCreatedAt().compareTo(r1.getCreatedAt()))
                .limit(10)
                .map(review -> org.th.dto.ReviewSummaryDTO.builder()
                        .id(review.getId())
                        .rating(review.getRating())
                        .comment(review.getComment())
                        .commentMm(review.getCommentMm())
                        .reviewerName(review.getReviewerName())
                        .helpfulCount(review.getHelpfulCount())
                        .ownerResponse(review.getOwnerResponse())
                        .ownerResponseMm(review.getOwnerResponseMm())
                        .createdAt(review.getCreatedAt())
                        .build())
                .collect(Collectors.toList());

        // Convert operating hours
        List<org.th.dto.OperatingHourDTO> operatingHourDTOs = shop.getOperatingHours().stream()
                .map(hour -> org.th.dto.OperatingHourDTO.builder()
                        .id(hour.getId())
                        .dayOfWeek(hour.getDayOfWeek())
                        .openingTime(hour.getOpeningTime())
                        .closingTime(hour.getClosingTime())
                        .isClosed(hour.getIsClosed())
                        .build())
                .collect(Collectors.toList());

        return org.th.dto.ShopDetailDTO.builder()
                .id(shop.getId())
                .name(shop.getName())
                .nameMm(shop.getNameMm())
                .nameEn(shop.getNameEn())
                .slug(shop.getSlug())
                .category(shop.getCategory())
                .subCategory(shop.getSubCategory())
                .latitude(shop.getLatitude())
                .longitude(shop.getLongitude())
                .address(shop.getAddress())
                .addressMm(shop.getAddressMm())
                .township(shop.getTownship())
                .city(shop.getCity())
                .phone(shop.getPhone())
                .email(shop.getEmail())
                .description(shop.getDescription())
                .descriptionMm(shop.getDescriptionMm())
                .specialties(shop.getSpecialties())
                .hasDelivery(shop.getHasDelivery())
                .hasParking(shop.getHasParking())
                .hasWifi(shop.getHasWifi())
                .ratingAvg(shop.getRatingAvg())
                .ratingCount(shop.getRatingCount())
                .viewCount(shop.getViewCount())
                .isActive(shop.getIsActive())
                .isVerified(shop.getIsVerified())
                .primaryPhotoUrl(shop.getPhotos().stream()
                        .filter(photo -> photo.getIsPrimary() != null && photo.getIsPrimary())
                        .findFirst()
                        .map(org.th.entity.shops.ShopPhoto::getUrl)
                        .orElse(shop.getPhotos().isEmpty() ? null : shop.getPhotos().get(0).getUrl()))
                .photos(photoDTOs)
                .menuCategories(menuCategoryDTOs)
                .recentReviews(reviewDTOs)
                .operatingHours(operatingHourDTOs)
                .createdAt(shop.getCreatedAt())
                .updatedAt(shop.getUpdatedAt())
                .build();
    }
}
