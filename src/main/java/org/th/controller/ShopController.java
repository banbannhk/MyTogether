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
        private final org.th.service.MenuCategoryService menuCategoryService;

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
                                .map(s -> shopService.convertToListDTO(s, lat, lon))
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
                        @Parameter(description = "User's latitude") @RequestParam Double lat,
                        @Parameter(description = "User's longitude") @RequestParam Double lon,
                        HttpServletRequest request) {

                Optional<ShopDetailDTO> shopDetailOpt = shopService.getShopDetailsById(id, lat, lon);

                if (shopDetailOpt.isEmpty()) {
                        return ResponseEntity.status(404).body(ApiResponse.error("Shop not found"));
                }

                ShopDetailDTO shop = shopDetailOpt.get();

                // Log activity
                userActivityService.logActivity(
                                ActivityType.VIEW_SHOP,
                                String.valueOf(id),
                                shop.getId(),
                                shop.getName(),
                                null, null,
                                "source=id", request);

                return ResponseEntity.ok(ApiResponse.success("Shop details found", shop));
        }

        /**
         * Get shop menu
         */
        @GetMapping("/{id}/menu")
        @RateLimit(tier = Tier.IO_INTENSIVE)
        @Operation(summary = "Get shop menu", description = "Get full menu for a shop")
        public ResponseEntity<ApiResponse<List<org.th.dto.MenuCategoryDTO>>> getShopMenu(
                        @Parameter(description = "Shop ID") @PathVariable Long id) {

                List<org.th.dto.MenuCategoryDTO> menu = menuCategoryService.getMenuCategoriesByShopId(id);
                return ResponseEntity.ok(ApiResponse.success("Shop menu retrieved", menu));
        }

        /**
         * Get shop details by slug
         */
        @GetMapping("/slug/{slug}")
        @RateLimit(tier = Tier.IO_INTENSIVE)
        @Operation(summary = "Get shop by slug", description = "Get shop details using URL-friendly slug")
        public ResponseEntity<ApiResponse<ShopDetailDTO>> getShopBySlug(
                        @Parameter(description = "Shop Slug") @PathVariable String slug,
                        @Parameter(description = "User's latitude") @RequestParam Double lat,
                        @Parameter(description = "User's longitude") @RequestParam Double lon,
                        HttpServletRequest request) {

                ShopDetailDTO shop = shopService.getShopDetailsBySlug(slug, lat, lon);

                if (shop == null) {
                        return ResponseEntity.status(404).body(ApiResponse.error("Shop not found"));
                }

                // Log activity
                userActivityService.logActivity(
                                ActivityType.VIEW_SHOP,
                                slug,
                                shop.getId(),
                                shop.getName(),
                                null, null,
                                "source=slug", request);

                return ResponseEntity.ok(ApiResponse.success("Shop details found", shop));
        }

        /**
         * Get all shop categories
         */
        @GetMapping("/categories")
        @RateLimit(tier = Tier.PUBLIC)
        @Operation(summary = "Get all categories", description = "Get list of all available shop categories")
        public ResponseEntity<ApiResponse<List<String>>> getAllCategories() {
                List<String> categories = shopService.getAllCategories();
                return ResponseEntity.ok(ApiResponse.success("Categories retrieved", categories));
        }

        /**
         * Universal search - searches shop names and food items
         */
        @GetMapping("/search")
        @RateLimit(tier = Tier.CPU_INTENSIVE)
        @Operation(summary = "Search shops", description = "Search shops by name or food/menu items (supports Myanmar and English)")
        public ResponseEntity<ApiResponse<org.th.dto.SearchResponseDTO>> searchShops(
                        @Parameter(description = "Search keyword") @RequestParam String q,
                        @Parameter(description = "User's latitude") @RequestParam Double lat,
                        @Parameter(description = "User's longitude") @RequestParam Double lon,
                        HttpServletRequest request) {

                // Log activity
                userActivityService.logActivity(
                                ActivityType.SEARCH_QUERY,
                                q, null, null, lat, lon,
                                "type=universal", request);

                // Perform combined search
                org.th.dto.SearchResponseDTO results = shopService.searchCombined(q);

                // If location provided, calculate distances for shops
                if (lat != null && lon != null && results.getShops() != null) {
                        // Sort shops by distance from user location
                        results.getShops().sort((s1, s2) -> {
                                if (s1.getLatitude() == null || s1.getLongitude() == null)
                                        return 1;
                                if (s2.getLatitude() == null || s2.getLongitude() == null)
                                        return -1;

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

                return ResponseEntity.ok(ApiResponse.success(
                                "Found " + results.getShops().size() + " shops, " +
                                                results.getCategories().size() + " categories, " +
                                                results.getMenus().size() + " menu items matching '" + q + "'",
                                results));
        }

        @GetMapping("/trending")
        @RateLimit(tier = RateLimit.Tier.PUBLIC)
        @Operation(summary = "Get trending shops", description = "Get top 10 trending shops based on recent activity")
        public ResponseEntity<ApiResponse<List<ShopListDTO>>> getTrendingShops(
                        @Parameter(description = "User's latitude") @RequestParam Double lat,
                        @Parameter(description = "User's longitude") @RequestParam Double lon,
                        HttpServletRequest request) {

                // Optional: Log viewing trending page
                userActivityService.logActivity(
                                ActivityType.VIEW_CATEGORY, // Using VIEW_CATEGORY as proxy for "Browsing List"
                                null, null, "Trending", lat, lon,
                                null, request);

                List<Shop> trendingShops = trendingService.getTopTrendingShops();
                List<ShopListDTO> dtos = trendingShops.stream()
                                .map(s -> shopService.convertToListDTO(s, lat, lon))
                                .collect(Collectors.toList());
                return ResponseEntity.ok(ApiResponse.success("Trending shops retrieved", dtos));
        }

        @GetMapping("/foryou")
        @RateLimit(tier = RateLimit.Tier.MODERATE)
        @Operation(summary = "Personalized recommendations", description = "Get recommended shops based on user history or device activity")
        public ResponseEntity<ApiResponse<List<ShopListDTO>>> getRecommendations(
                        @Parameter(description = "User's latitude") @RequestParam Double lat,
                        @Parameter(description = "User's longitude") @RequestParam Double lon,
                        HttpServletRequest request) { // Inject Request to get headers

                org.springframework.security.core.Authentication auth = org.springframework.security.core.context.SecurityContextHolder
                                .getContext().getAuthentication();

                String username = null;
                boolean isAuthenticated = auth != null && auth.isAuthenticated()
                                && !"anonymousUser".equals(auth.getPrincipal());

                if (isAuthenticated && auth != null) {
                        username = auth.getName();
                }

                String deviceId = request.getHeader("X-Device-ID");

                // If neither user nor deviceId, fallback to trending
                if (username == null && deviceId == null) {
                        return getTrendingShops(lat, lon, request); // call overload
                }

                List<Shop> recommendations = recommendationService.getRecommendedShops(username, deviceId);

                if (recommendations.isEmpty()) {
                        return getTrendingShops(lat, lon, request); // Fallback if no specific recommendations
                }

                List<ShopListDTO> dtos = recommendations.stream()
                                .map(s -> shopService.convertToListDTO(s, lat, lon))
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
                        @Parameter(description = "User's latitude") @RequestParam Double lat,
                        @Parameter(description = "User's longitude") @RequestParam Double lon,
                        HttpServletRequest request) {

                ActivityType type;
                try {
                        type = ActivityType.valueOf("CLICK_" + action.toUpperCase());
                } catch (IllegalArgumentException e) {
                        return ResponseEntity.badRequest().body(ApiResponse.error("Invalid action type"));
                }

                userActivityService.logActivity(
                                type,
                                null, id, null, lat, lon,
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
                        @Parameter(description = "Page size") @RequestParam(defaultValue = "20") int size,
                        @Parameter(description = "User's latitude") @RequestParam Double lat,
                        @Parameter(description = "User's longitude") @RequestParam Double lon) {

                org.springframework.data.domain.Pageable pageable = org.springframework.data.domain.PageRequest.of(page,
                                size);
                org.springframework.data.domain.Slice<ShopListDTO> dtoSlice = shopService.getAllShops(pageable, lat,
                                lon);

                return ResponseEntity.ok(ApiResponse.success(
                                "Retrieved shops page " + page,
                                dtoSlice));
        }
}
