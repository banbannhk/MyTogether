package org.th.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.th.dto.ApiResponse;
import org.th.dto.ShopDetailDTO;
import org.th.dto.ShopListDTO;
import org.th.dto.LocationCountDTO;
import org.th.entity.shops.Shop;
import org.th.service.ShopService;
import org.th.service.TrendingService;
import org.th.service.RecommendationService;
import org.th.service.UserActivityService;
import org.th.entity.enums.ActivityType;
import org.th.config.ratelimit.RateLimit;
import org.th.config.ratelimit.RateLimit.Tier;
import jakarta.servlet.http.HttpServletRequest;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/shops")
@RequiredArgsConstructor
@Tag(name = "Shops", description = "Shop discovery and search APIs")
public class ShopController {

        private final ShopService shopService;
        private final UserActivityService userActivityService;
        private final TrendingService trendingService;
        private final RecommendationService recommendationService;
        private final org.th.repository.ShopRepository shopRepository; // Direct access for aggregation

        /**
         * Get nearby shops based on user location
         */
        @GetMapping("/nearby")
        @RateLimit(tier = Tier.CPU_INTENSIVE)
        @Operation(summary = "Get nearby shops", description = "Find shops within a specified radius of the user's location")
        public ResponseEntity<ApiResponse<List<ShopListDTO>>> getNearbyShops(
                        @Parameter(description = "User's latitude") @RequestParam Double lat,
                        @Parameter(description = "User's longitude") @RequestParam Double lon,
                        @Parameter(description = "Search radius in km") @RequestParam(defaultValue = "5.0") Double radius,
                        HttpServletRequest request) {

                // Log activity
                userActivityService.logActivity(
                                ActivityType.VIEW_NEARBY,
                                null, null, null, lat, lon,
                                "radius=" + radius, request);

                List<Shop> shops = shopService.getNearbyShops(lat, lon, radius);

                List<ShopListDTO> shopDTOs = shops.stream()
                                .map(shopService::convertToListDTO)
                                .collect(Collectors.toList());

                return ResponseEntity.ok(ApiResponse.success(
                                "Found " + shops.size() + " shops within " + radius + " km",
                                shopDTOs));
        }

        /**
         * Get shop details by ID
         */
        @GetMapping("/{id}")
        @RateLimit(tier = Tier.IO_INTENSIVE)
        @Operation(summary = "Get shop details", description = "Get complete shop information including menu, reviews, and operating hours")
        public ResponseEntity<ApiResponse<ShopDetailDTO>> getShopById(
                        @Parameter(description = "Shop ID") @PathVariable Long id,
                        HttpServletRequest request) {

                Optional<Shop> shopOpt = shopService.getShopById(id);

                if (shopOpt.isEmpty()) {
                        return ResponseEntity.ok(ApiResponse.error("Shop not found"));
                }

                ShopDetailDTO shopDTO = shopService.convertToDetailDTO(shopOpt.get());

                // Log activity
                userActivityService.logActivity(
                                ActivityType.VIEW_SHOP,
                                null, id, shopDTO.getName(), null, null,
                                null, request);

                return ResponseEntity.ok(ApiResponse.success("Shop details retrieved successfully", shopDTO));
        }

        /**
         * Get shop details by slug
         */
        @GetMapping("/slug/{slug}")
        @RateLimit(tier = Tier.IO_INTENSIVE)
        @Operation(summary = "Get shop by slug", description = "Get shop details using URL-friendly slug")
        public ResponseEntity<ApiResponse<ShopDetailDTO>> getShopBySlug(
                        @Parameter(description = "Shop slug") @PathVariable String slug,
                        HttpServletRequest request) {

                Shop shop = shopService.getShopBySlug(slug);

                if (shop == null) {
                        return ResponseEntity.ok(ApiResponse.error("Shop not found"));
                }

                ShopDetailDTO shopDTO = shopService.convertToDetailDTO(shop);

                // Log activity
                userActivityService.logActivity(
                                ActivityType.VIEW_SHOP,
                                null, shopDTO.getId(), shopDTO.getName(), null, null,
                                "slug=" + slug, request);

                return ResponseEntity.ok(ApiResponse.success("Shop details retrieved successfully", shopDTO));
        }

        /**
         * Get shops by category (with optional location filtering)
         */
        @GetMapping("/category/{category}")
        @RateLimit(tier = Tier.CPU_INTENSIVE)
        @Operation(summary = "Get shops by category", description = "Find shops in a specific category, optionally filtered by location")
        public ResponseEntity<ApiResponse<List<ShopListDTO>>> getShopsByCategory(
                        @Parameter(description = "Shop category") @PathVariable String category,
                        @Parameter(description = "User's latitude (optional)") @RequestParam(required = false) Double lat,
                        @Parameter(description = "User's longitude (optional)") @RequestParam(required = false) Double lon,
                        @Parameter(description = "Search radius in kilometers") @RequestParam(required = false, defaultValue = "10.0") Double radius,
                        HttpServletRequest request) {

                // Log activity
                userActivityService.logActivity(
                                ActivityType.VIEW_CATEGORY,
                                null, null, category, lat, lon,
                                null, request);

                List<Shop> shops;

                // If location provided, search nearby with category filter
                if (lat != null && lon != null) {
                        shops = shopService.getNearbyShopsByCategory(lat, lon, radius, category);
                } else {
                        // Otherwise, get all shops in category
                        shops = shopService.getShopsByCategory(category);
                }

                List<ShopListDTO> shopDTOs = shops.stream()
                                .map(shopService::convertToListDTO)
                                .collect(Collectors.toList());

                return ResponseEntity.ok(ApiResponse.success(
                                "Found " + shops.size() + " " + category + " shops",
                                shopDTOs));
        }

        /**
         * Universal search - searches shop names and food items
         */
        @GetMapping("/search")
        @RateLimit(tier = Tier.CPU_INTENSIVE)
        @Operation(summary = "Search shops", description = "Search shops by name or food/menu items (supports Myanmar and English)")
        public ResponseEntity<ApiResponse<List<ShopListDTO>>> searchShops(
                        @Parameter(description = "Search keyword") @RequestParam String q,
                        @Parameter(description = "User's latitude (optional for distance calculation)") @RequestParam(required = false) Double lat,
                        @Parameter(description = "User's longitude (optional for distance calculation)") @RequestParam(required = false) Double lon,
                        HttpServletRequest request) {

                // Log activity
                userActivityService.logActivity(
                                ActivityType.SEARCH_QUERY,
                                q, null, null, lat, lon,
                                "type=universal", request);

                List<Shop> shops = shopService.searchShops(q);

                // If location provided, calculate distances
                if (lat != null && lon != null) {
                        // Sort by distance from user location
                        shops.sort((s1, s2) -> {
                                double dist1 = ShopService.calculateDistance(
                                                lat, lon,
                                                s1.getLatitude().doubleValue(),
                                                s1.getLongitude().doubleValue());
                                double dist2 = ShopService.calculateDistance(
                                                lat, lon,
                                                s2.getLatitude().doubleValue(),
                                                s2.getLongitude().doubleValue());
                                return Double.compare(dist1, dist2);
                        });
                }

                List<ShopListDTO> shopDTOs = shops.stream()
                                .map(shopService::convertToListDTO)
                                .collect(Collectors.toList());

                return ResponseEntity.ok(ApiResponse.success(
                                "Found " + shops.size() + " shops matching '" + q + "'",
                                shopDTOs));
        }

        /**
         * Search shops by shop name only
         */
        @GetMapping("/search/name")
        @RateLimit(tier = Tier.MODERATE)
        @Operation(summary = "Search by shop name", description = "Search shops by name only (excludes menu items)")
        public ResponseEntity<ApiResponse<List<ShopListDTO>>> searchByShopName(
                        @Parameter(description = "Shop name keyword") @RequestParam String name,
                        HttpServletRequest request) {

                // Log activity
                userActivityService.logActivity(
                                ActivityType.SEARCH_QUERY,
                                name, null, null, null, null,
                                "type=shop_name", request);

                List<Shop> shops = shopService.searchByShopName(name);
                List<ShopListDTO> shopDTOs = shops.stream()
                                .map(shopService::convertToListDTO)
                                .collect(Collectors.toList());

                return ResponseEntity.ok(ApiResponse.success(
                                "Found " + shops.size() + " shops",
                                shopDTOs));
        }

        /**
         * Search shops by food/menu item name
         */
        @GetMapping("/search/food")
        @RateLimit(tier = Tier.MODERATE)
        @Operation(summary = "Search by food name", description = "Find shops that serve a specific food or menu item")
        public ResponseEntity<ApiResponse<List<ShopListDTO>>> searchByFoodName(
                        @Parameter(description = "Food/menu item name") @RequestParam String food,
                        HttpServletRequest request) {

                // Log activity
                userActivityService.logActivity(
                                ActivityType.SEARCH_QUERY,
                                food, null, null, null, null,
                                "type=food_name", request);

                List<Shop> shops = shopService.searchByFoodName(food);
                List<ShopListDTO> shopDTOs = shops.stream()
                                .map(shopService::convertToListDTO)
                                .collect(Collectors.toList());

                return ResponseEntity.ok(ApiResponse.success(
                                "Found " + shops.size() + " shops serving '" + food + "'",
                                shopDTOs));
        }

        @GetMapping("/trending")
        @RateLimit(tier = RateLimit.Tier.PUBLIC)
        @Operation(summary = "Get trending shops", description = "Get top 10 trending shops based on recent activity")
        public ResponseEntity<ApiResponse<List<ShopListDTO>>> getTrendingShops() {
                List<Shop> trendingShops = trendingService.getTopTrendingShops();
                List<ShopListDTO> dtos = trendingShops.stream()
                                .map(shopService::convertToListDTO)
                                .collect(Collectors.toList());
                return ResponseEntity.ok(ApiResponse.success("Trending shops retrieved", dtos));
        }

        @GetMapping("/townships")
        @RateLimit(tier = RateLimit.Tier.PUBLIC)
        @Operation(summary = "Explore by township", description = "Get shop counts by township/district")
        public ResponseEntity<ApiResponse<List<LocationCountDTO>>> getTownshipCounts() {
                List<LocationCountDTO> counts = shopRepository.countShopsByTownship();
                return ResponseEntity.ok(ApiResponse.success("Township counts retrieved", counts));
        }

        @GetMapping("/foryou")
        @RateLimit(tier = RateLimit.Tier.MODERATE)
        @Operation(summary = "Personalized recommendations", description = "Get recommended shops based on user history or device activity")
        public ResponseEntity<ApiResponse<List<ShopListDTO>>> getRecommendations(
                        HttpServletRequest request) { // Inject Request to get headers

                org.springframework.security.core.Authentication auth = org.springframework.security.core.context.SecurityContextHolder
                                .getContext().getAuthentication();

                String username = null;
                boolean isAuthenticated = auth != null && auth.isAuthenticated()
                                && !"anonymousUser".equals(auth.getPrincipal());

                if (isAuthenticated) {
                        username = auth.getName();
                }

                String deviceId = request.getHeader("X-Device-ID");

                // If neither user nor deviceId, fallback to trending
                if (username == null && deviceId == null) {
                        return getTrendingShops();
                }

                List<Shop> recommendations = recommendationService.getRecommendedShops(username, deviceId);

                if (recommendations.isEmpty()) {
                        return getTrendingShops(); // Fallback if no specific recommendations
                }

                List<ShopListDTO> dtos = recommendations.stream()
                                .map(shopService::convertToListDTO)
                                .collect(Collectors.toList());
                return ResponseEntity.ok(ApiResponse.success("Recommended shops for you", dtos));
        }

        /**
         * Track conversion events (High Intent)
         */
        @PostMapping("/{id}/track")
        @RateLimit(tier = RateLimit.Tier.WRITE)
        @Operation(summary = "Track conversion event", description = "Log high-intent actions like clicking directions, call, or share")
        public ResponseEntity<ApiResponse<Void>> trackConversion(
                        @Parameter(description = "Shop ID") @PathVariable Long id,
                        @Parameter(description = "Action type (DIRECTIONS, CALL, WEBSITE, SHARE)") @RequestParam String action,
                        HttpServletRequest request) {

                ActivityType type;
                try {
                        type = ActivityType.valueOf("CLICK_" + action.toUpperCase());
                } catch (IllegalArgumentException e) {
                        return ResponseEntity.badRequest().body(ApiResponse.error("Invalid action type"));
                }

                userActivityService.logActivity(
                                type,
                                null, id, null, null, null,
                                "conversion_event", request);

                return ResponseEntity.ok(ApiResponse.success("Event tracked", null));
        }

        /**
         * Get all shops with pagination (Slice - Faster)
         */
        @GetMapping
        @RateLimit(tier = Tier.PUBLIC)
        @Operation(summary = "Get all shops", description = "Get paginated list of all shops (Slice - No Total Count)")
        public ResponseEntity<ApiResponse<org.springframework.data.domain.Slice<ShopListDTO>>> getAllShops(
                        @Parameter(description = "Page number (0-indexed)") @RequestParam(defaultValue = "0") int page,
                        @Parameter(description = "Page size") @RequestParam(defaultValue = "20") int size) {

                org.springframework.data.domain.Pageable pageable = org.springframework.data.domain.PageRequest.of(page,
                                size);
                org.springframework.data.domain.Slice<ShopListDTO> dtoSlice = shopService.getAllShops(pageable);

                return ResponseEntity.ok(ApiResponse.success(
                                "Retrieved shops page " + page,
                                dtoSlice));
        }
}
