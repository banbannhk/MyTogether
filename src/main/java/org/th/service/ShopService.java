package org.th.service;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.th.entity.shops.Shop;
import org.th.entity.shops.ShopReview;
import org.th.repository.ShopRepository;

import org.springframework.cache.annotation.Caching;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@Transactional
@lombok.extern.slf4j.Slf4j
public class ShopService {

    @Autowired
    private ShopRepository shopRepository;

    @Autowired
    private org.th.service.MenuCategoryService menuCategoryService;

    private static final Double DEFAULT_RADIUS_KM = 5.0;
    private static final Double MAX_RADIUS_KM = 50.0;

    /**
     * Helper to fetch shops with photos to avoid N+1 and LazyInit exceptions
     * 
     * @param shops Initial list of shops (which might be proxies or lack photos)
     * @return List of shops with photos fully loaded
     */
    private List<Shop> fetchWithPhotos(List<Shop> shops) {
        if (shops.isEmpty()) {
            return shops;
        }
        List<Long> shopIds = shops.stream().map(Shop::getId).collect(Collectors.toList());
        return shopRepository.findByIdInWithPhotos(shopIds);
    }

    /**
     * Get shops near a user's location
     * 
     * @param userLatitude  User's current latitude
     * @param userLongitude User's current longitude
     * @param radiusInKm    Search radius in kilometers (default: 5km, max: 50km)
     * @return List of shops with photos fully loaded
     */
    public List<Shop> getNearbyShops(Double userLatitude, Double userLongitude, Double radiusInKm) {
        log.info("Searching for shops near [{}, {}] within {} km", userLatitude, userLongitude, radiusInKm);

        // Validate and normalize radius
        Double radius = normalizeRadius(radiusInKm);

        // Query nearby shops (Returns IDs or basic entities)
        List<Shop> nearbyShops = shopRepository.findNearbyShops(userLatitude, userLongitude, radius);

        // Fetch with photos to be safe for DTO conversion
        List<Shop> initializedShops = fetchWithPhotos(nearbyShops);

        log.info("Found {} shops within {} km", initializedShops.size(), radius);
        return initializedShops;
    }

    /**
     * Get all unique shop categories
     * Cached for 1 hour (configured in CacheConfig)
     */
    @org.springframework.cache.annotation.Cacheable("categories")
    public List<String> getAllCategories() {
        return shopRepository.findDistinctCategories();
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
        log.info("Searching for {} shops near [{}, {}] within {} km",
                category, userLatitude, userLongitude, radiusInKm);

        Double radius = normalizeRadius(radiusInKm);

        List<Shop> nearbyShops = shopRepository.findNearbyShopsByCategory(
                userLatitude, userLongitude, radius, category);

        // Fetch with photos
        List<Shop> initializedShops = fetchWithPhotos(nearbyShops);

        log.info("Found {} {} shops within {} km", initializedShops.size(), category, radius);
        return initializedShops;
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
        log.info("Searching for verified shops near [{}, {}] within {} km",
                userLatitude, userLongitude, radiusInKm);

        List<Shop> nearbyShops = getNearbyShops(userLatitude, userLongitude, radiusInKm);

        // Filter for verified shops only
        List<Shop> verifiedShops = nearbyShops.stream()
                .filter(Shop::getIsVerified)
                .collect(Collectors.toList());

        log.info("Found {} verified shops out of {} total shops",
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
        log.info("Searching for shops with rating >= {} near [{}, {}] within {} km",
                minRating, userLatitude, userLongitude, radiusInKm);

        Double radius = normalizeRadius(radiusInKm);

        List<Shop> ratedShops = shopRepository.findNearbyShopsByRating(
                userLatitude, userLongitude, radius, minRating);

        // Fetch with photos
        List<Shop> initializedShops = fetchWithPhotos(ratedShops);

        log.info("Found {} shops with rating >= {}", initializedShops.size(), minRating);
        return initializedShops;
    }

    /**
     * Get shop by ID
     *
     * @param shopId Shop ID
     * @return Shop entity
     */

    @Transactional(readOnly = true)
    public Optional<Shop> getShopById(Long shopId) {
        // Use optimized queries to fetch details separately (to avoid
        // MultipleBagFetchException)
        // Hibernate cannot fetch multiple List collections in a single query
        Optional<Shop> shopOpt = shopRepository.findByIdWithDetails(shopId);

        // If shop exists, fully initialize lazy collections for Caching/Controller use
        shopOpt.ifPresent(shop -> {
            // Only initialize critical data for the main view
            if (shop.getPhotos() != null) {
                shop.getPhotos().size(); // Initialize photos (already fetched by findByIdWithDetails)
            }
            if (shop.getOperatingHours() != null) {
                shop.getOperatingHours().size(); // Initialize operating hours (fetched by findByIdWithDetails)
            }

            // DECOUPLED: Menu and Reviews are NOT initialized here.
            // They are fetched via separate API calls.
        });

        return shopOpt;
    }

    /**
     * Get shop details (DTO) by ID
     * Optimized: Fetches only Top 10 reviews and caches the DTO
     * 
     * @param shopId Shop ID
     * @return ShopDetailDTO or empty
     */
    @Transactional(readOnly = true)
    // Removed @Cacheable because response now depends on user location (ETA)
    public Optional<org.th.dto.ShopDetailDTO> getShopDetailsById(Long shopId, Double lat, Double lon) {
        Optional<Shop> shopOpt = getShopById(shopId);

        if (shopOpt.isEmpty()) {
            return Optional.empty();
        }

        Shop shop = shopOpt.get();

        // DECOUPLED: Pass empty list for reviews (fetched separately)
        // Pass lat/lon for ETA calculation
        return Optional.of(convertToDetailDTO(shop, lat, lon));
    }

    /**
     * Get shop by slug
     * 
     * @param slug Shop slug (URL-friendly name)
     * @return Shop entity
     */
    // removed @Cacheable - we cache the DTO instead in getShopDetailsBySlug
    public Shop getShopBySlug(String slug) {
        log.info("Fetching shop with slug: {}", slug);
        Shop shop = shopRepository.findBySlug(slug);

        // Initialize lazy collections
        if (shop != null) {
            if (shop.getPhotos() != null) {
                shop.getPhotos().size();
            }
            // Reviews and Menu are NOT initialized here
        }
        return shop;
    }

    /**
     * Get shop details (DTO) by Slug
     * Optimized: Fetches only Top 10 reviews and caches the DTO
     * 
     * @param slug Shop slug
     * @return ShopDetailDTO or null
     */
    @Transactional(readOnly = true)
    // Removed @Cacheable because response now depends on user location (ETA)
    public org.th.dto.ShopDetailDTO getShopDetailsBySlug(String slug, Double lat, Double lon) {
        Shop shop = getShopBySlug(slug);

        if (shop == null) {
            return null;
        }

        // DECOUPLED: Pass empty list for reviews (fetched separately)
        // Pass lat/lon for ETA calculation
        return convertToDetailDTO(shop, lat, lon);
    }

    /**
     * Get all shops with pagination (Optimized with Slice + DTO Projection +
     * Caching)
     * 
     * @param pageable Pagination information
     * @return Slice of ShopListDTO (no total count)
     */
    @Transactional(readOnly = true)
    // Removed @Cacheable because list now depends on user location for ETA
    public Slice<org.th.dto.ShopListDTO> getAllShops(Pageable pageable, Double lat, Double lon) {
        log.info("Fetching all shops slice: {}", pageable.getPageNumber());

        // 1. Fetch Slice of Shops (No Count Query)
        Slice<Shop> shopSlice = shopRepository.findByIsActiveTrue(pageable);

        // 2. Optimization: Fetch eager photos for this slice if needed
        // Note: Even though we fetched them, we need to ensure we use the *managed*
        // entities
        // or explicitly map them.
        if (!shopSlice.isEmpty()) {
            List<Long> shopIds = shopSlice.getContent().stream()
                    .map(Shop::getId)
                    .collect(Collectors.toList());

            // This fetches shops + photos into the persistence context.
            // Since we are in a transaction, standard Hibernate behavior *should*
            // associate these with the L1 cache.
            // However, to be 100% safe against "Multiple Bag Fetch" / detached entity
            // issues,
            // we will fetch the fully loaded list and use THAT for mapping.
            List<Shop> loadedShops = shopRepository.findByIdInWithPhotos(shopIds);

            // Create a map for easy lookup
            java.util.Map<Long, Shop> shopMap = loadedShops.stream()
                    .collect(Collectors.toMap(Shop::getId, s -> s));

            // Map the ORIGINAL slice (preserving order/metadata) using the LOADED entities
            return shopSlice.map(s -> {
                Shop loaded = shopMap.getOrDefault(s.getId(), s);
                return convertToListDTO(loaded, lat, lon);
            });
        }

        return shopSlice.map(s -> convertToListDTO(s, lat, lon));
    }

    /**
     * Get all shops (Deprecated: Use pagination version)
     * 
     * @return List of all shops
     */
    public List<Shop> getAllShops() {
        log.info("Fetching all shops");
        return shopRepository.findAll();
    }

    /**
     * Get all shops in a category
     * 
     * @param category Shop category
     * @return List of shops in the category
     */
    @Cacheable("shopsByCategory")
    public List<Shop> getShopsByCategory(String category) {
        log.info("Fetching all shops in category: {}", category);
        List<Shop> shops = shopRepository.findByCategoryOrderByRatingAvgDesc(category);

        // Fetch with photos
        List<Shop> initializedShops = fetchWithPhotos(shops);

        return initializedShops;
    }

    /**
     * Get all verified shops
     * 
     * @return List of verified shops
     */
    public List<Shop> getVerifiedShops() {
        log.info("Fetching all verified shops");
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
        log.info("Fetching shops in township: {}", township);
        return shopRepository.findByTownship(township);
    }

    /**
     * Save or update a shop
     * 
     * @param shop Shop entity to save
     * @return Saved shop entity
     */
    @Caching(evict = {
            @CacheEvict(value = "homeShops", allEntries = true),
            @CacheEvict(value = "shopDetails", key = "#shop.id"),
            @CacheEvict(value = "shopDetails", key = "#shop.slug")
    })
    public Shop saveShop(Shop shop) {
        log.info("Saving shop: {}", shop.getName());
        return shopRepository.save(shop);
    }

    /**
     * Delete a shop by ID
     * 
     * @param shopId Shop ID to delete
     */
    @Caching(evict = {
            @CacheEvict(value = "homeShops", allEntries = true),
            @CacheEvict(value = "shopDetails", allEntries = true)
    })
    public void deleteShop(Long shopId) {
        log.info("Deleting shop with ID: {}", shopId);
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
            log.debug("Invalid radius {}, using default {} km", radiusInKm, DEFAULT_RADIUS_KM);
            return DEFAULT_RADIUS_KM;
        }

        if (radiusInKm > MAX_RADIUS_KM) {
            log.debug("Radius {} exceeds maximum, capping at {} km", radiusInKm, MAX_RADIUS_KM);
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

    /**
     * Calculate ETA Range based on "Heavy Traffic Baseline"
     * Assumes average speed of 15 km/h (Daytime Traffic)
     * Provides a tight realistic range (approx +/- 15%)
     * 
     * @param distanceKm Distance in kilometers
     * @return int[] {minMinutes, maxMinutes}
     */
    private int[] calculateEtaRangeV2(double distanceKm) {
        // Base Speed: 15 km/h (Bangkok Heavy Traffic Average)
        final double BASE_SPEED_KMH = 15.0;

        // Calculate Base Time (minutes)
        double baseMinutes = (distanceKm / BASE_SPEED_KMH) * 60;

        // Apply Variance (+/- 15%)
        // 5km -> 20 mins -> 17 - 23 mins (Gap: 6 mins)
        // 2km -> 8 mins -> 7 - 9 mins

        int minEta = (int) Math.ceil(baseMinutes * 0.85);
        int maxEta = (int) Math.ceil(baseMinutes * 1.15);

        // Safety adjustments
        if (minEta < 1)
            minEta = 1;
        if (maxEta <= minEta)
            maxEta = minEta + 3; // Ensure at least 3 min range

        return new int[] { minEta, maxEta };
    }

    // ==================== SEARCH METHODS ====================

    /**
     * Search shops by name or food
     * 
     * @param keyword Search term
     * @return List of shops matching the keyword
     */
    public List<Shop> searchShops(String keyword) {
        log.info("Searching shops with keyword: {}", keyword);
        // 1. Standard Search (Exact/Like)
        List<Shop> results = shopRepository.searchShops(keyword);

        // 2. Fuzzy Fallback (if few results)
        if (results.size() < 3) {
            log.info("Few results found ({}). Attempting fuzzy search for: {}", results.size(), keyword);
            try {
                List<Shop> fuzzyResults = shopRepository.findShopsFuzzy(keyword);

                // Merge results (avoiding duplicates)
                for (Shop s : fuzzyResults) {
                    if (results.stream().noneMatch(r -> r.getId().equals(s.getId()))) {
                        results.add(s);
                    }
                }
            } catch (Exception e) {
                // Graceful fallback if pg_trgm is not enabled or other DB error
                log.warn("Fuzzy search failed (likely missing pg_trgm extension): {}", e.getMessage());
            }
        }

        List<Shop> initializedResults = fetchWithPhotos(results);

        log.info("Found {} shops matching '{}'", initializedResults.size(), keyword);
        return initializedResults;
    }

    /**
     * Search shops by shop name only
     * 
     * @param name Shop name to search
     * @return List of shops matching the name
     */
    public List<Shop> searchByShopName(String name) {
        log.info("Searching shops by name: {}", name);
        List<Shop> results = shopRepository.searchByShopName(name);

        List<Shop> initializedResults = fetchWithPhotos(results);

        log.info("Found {} shops matching name '{}'", initializedResults.size(), name);
        return initializedResults;
    }

    /**
     * Search shops by food/menu item name
     * 
     * @param foodName Food name to search
     * @return List of shops that have the matching food item
     */
    public List<Shop> searchByFoodName(String foodName) {
        log.info("Searching shops by food name: {}", foodName);
        List<Shop> results = shopRepository.searchByMenuItemName(foodName);

        List<Shop> initializedResults = fetchWithPhotos(results);

        log.info("Found {} shops with food matching '{}'", initializedResults.size(), foodName);
        return initializedResults;
    }

    /**
     * Combined search for shops, categories, and menu items
     * 
     * @param keyword Search keyword
     * @return SearchResponseDTO containing combined results
     */
    public org.th.dto.SearchResponseDTO searchCombined(String keyword) {
        log.info("Performing combined search for: {}", keyword);

        // 1. Search Shops
        List<Shop> shops = searchShops(keyword);
        List<org.th.dto.ShopListDTO> shopDTOs = shops.stream()
                .map(this::convertToListDTO)
                .collect(Collectors.toList());

        // 2. Search Categories
        List<org.th.dto.MenuCategoryDTO> categoryDTOs = menuCategoryService.searchMenuCategories(keyword);

        // 3. Search Menu Items
        List<org.th.entity.shops.MenuItem> menuItems = shopRepository.searchMenuItems(keyword);
        List<org.th.dto.MenuItemDTO> menuDTOs = menuItems.stream()
                .map(item -> {
                    // Map photos
                    List<org.th.dto.PhotoDTO> itemPhotos = item.getPhotos().stream()
                            .map(photo -> org.th.dto.PhotoDTO.builder()
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

                    return org.th.dto.MenuItemDTO.builder()
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
                            .photos(itemPhotos)
                            .build();
                })
                .collect(Collectors.toList());

        return org.th.dto.SearchResponseDTO.builder()
                .shops(shopDTOs)
                .categories(categoryDTOs)
                .menus(menuDTOs)
                .build();
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
        String primaryPhotoUrl = null;
        try {
            if (shop.getPhotos() != null && org.hibernate.Hibernate.isInitialized(shop.getPhotos())) {
                primaryPhotoUrl = shop.getPhotos().stream()
                        .filter(photo -> photo.getIsPrimary() != null && photo.getIsPrimary())
                        .findFirst()
                        .map(org.th.entity.shops.ShopPhoto::getUrl)
                        .orElse(shop.getPhotos().isEmpty() ? null : shop.getPhotos().get(0).getUrl());
            }
        } catch (Exception e) {
            // Log warning but proceed (graceful degradation)
            log.warn("Could not access photos for shop {}: {}", shop.getId(), e.getMessage());
        }

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
                .district(shop.getTownship())
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
                .isHalal(shop.getIsHalal())
                .isVegetarian(shop.getIsVegetarian())
                .pricePreference(shop.getPricePreference())
                .pricePreferenceMm(shop.getPricePreference() != null ? shop.getPricePreference().getLabelMm() : null)
                .build();
    }

    /**
     * Convert Shop entity to ShopListDTO with Location/ETA
     * 
     * @param shop    Shop entity
     * @param userLat User Latitude (nullable)
     * @param userLon User Longitude (nullable)
     * @return ShopListDTO with distance and ETA
     */
    public org.th.dto.ShopListDTO convertToListDTO(Shop shop, Double userLat, Double userLon) {
        org.th.dto.ShopListDTO dto = convertToListDTO(shop);

        if (dto != null && userLat != null && userLon != null
                && shop.getLatitude() != null && shop.getLongitude() != null) {

            double distance = calculateDistance(
                    userLat, userLon,
                    shop.getLatitude().doubleValue(), shop.getLongitude().doubleValue());
            // Round to 1 decimal place
            distance = Math.round(distance * 10.0) / 10.0;

            int[] etaRange = calculateEtaRangeV2(distance);

            dto.setDistance(distance);
            dto.setMinEta(etaRange[0]);
            dto.setMaxEta(etaRange[1]);
            dto.setEstimatedTime(etaRange[0] + " - " + etaRange[1] + " min");
        }

        return dto;
    }

    /**
     * Convert to DetailDTO (with reviews)
     * 
     * @param shop       Shop entity
     * @param topReviews List of reviews to include
     * @return ShopDetailDTO
     */
    public org.th.dto.ShopDetailDTO convertToDetailDTO(Shop shop, List<ShopReview> topReviews) {
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

        // Convert menu categories ONLY if loaded (otherwise skip/return empty)
        // In the optimized flow, this will likely be empty/null, which is correct
        // (Decoupled)
        List<org.th.dto.MenuCategoryDTO> menuCategoryDTOs = null;
        if (shop.getMenuCategories() != null && org.hibernate.Hibernate.isInitialized(shop.getMenuCategories())) {
            menuCategoryDTOs = shop.getMenuCategories().stream()
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
        }

        // Convert provided reviews if present
        List<org.th.dto.ReviewSummaryDTO> reviewDTOs = null;
        if (topReviews != null) {
            reviewDTOs = topReviews.stream()
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
        }

        // Convert operating hours
        // Convert operating hours
        List<org.th.dto.OperatingHourDTO> operatingHourDTOs = new java.util.ArrayList<>();
        if (shop.getOperatingHours() != null) {
            operatingHourDTOs = shop.getOperatingHours().stream()
                    .sorted(java.util.Comparator.comparing(org.th.entity.shops.OperatingHour::getDayOfWeek))
                    .map(hour -> org.th.dto.OperatingHourDTO.builder()
                            .id(hour.getId())
                            .dayOfWeek(hour.getDayOfWeek())
                            .openingTime(hour.getOpeningTime())
                            .closingTime(hour.getClosingTime())
                            .isClosed(hour.getIsClosed())
                            .build())
                    .collect(Collectors.toList());
        }

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
                .district(shop.getTownship())
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
                .isHalal(shop.getIsHalal())
                .isVegetarian(shop.getIsVegetarian())
                .pricePreference(shop.getPricePreference())
                .pricePreferenceMm(shop.getPricePreference() != null ? shop.getPricePreference().getLabelMm() : null)
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

    /**
     * Legacy converter that fetches reviews from entity (Avoid using if possible)
     */
    public org.th.dto.ShopDetailDTO convertToDetailDTO(Shop shop) {
        if (shop == null)
            return null;

        // Check if reviews are initialized, if not return empty list or fail
        // For safety in legacy calls, we might want to return empty if not initialized
        // But for now, we'll try to use the getter which might throw LazyInit if not
        // careful
        // Better: Fetch top 10 using repository if we can, but we don't have ID easily
        // visible here without side effects
        // So we will just filter the list assuming it's loaded, OR handle the
        // exception?
        // Actually, let's just use the strict limited list from the entity

        List<ShopReview> reviews = java.util.Collections.emptyList();

        try {
            if (shop.getReviews() != null) {
                reviews = shop.getReviews().stream()
                        .filter(r -> r.getIsVisible() != null && r.getIsVisible())
                        .sorted((r1, r2) -> r2.getCreatedAt().compareTo(r1.getCreatedAt()))
                        .limit(10)
                        .collect(Collectors.toList());
            }
        } catch (Exception e) {
            // LazyInit or similar
            log.warn("Could not load reviews for convertToDetailDTO: {}", e.getMessage());
        }

        return convertToDetailDTO(shop, reviews);
    }

    /**
     * Convert Shop entity to ShopDetailDTO with Location/ETA
     */
    public org.th.dto.ShopDetailDTO convertToDetailDTO(Shop shop, Double userLat, Double userLon) {
        // Reuse basic conversion
        org.th.dto.ShopDetailDTO dto = convertToDetailDTO(shop, null);

        if (dto != null && userLat != null && userLon != null
                && shop.getLatitude() != null && shop.getLongitude() != null) {

            double distance = calculateDistance(
                    userLat, userLon,
                    shop.getLatitude().doubleValue(), shop.getLongitude().doubleValue());
            distance = Math.round(distance * 10.0) / 10.0;

            int[] etaRange = calculateEtaRangeV2(distance);

            dto.setDistance(distance);
            dto.setMinEta(etaRange[0]);
            dto.setMaxEta(etaRange[1]);
            dto.setEstimatedTime(etaRange[0] + " - " + etaRange[1] + " min");
        }

        return dto;
    }
}
