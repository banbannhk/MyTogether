package org.th.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.th.dto.ApiResponse;
import org.th.dto.ShopListDTO;
import org.th.entity.User;
import org.th.service.FavoriteService;
import org.th.config.ratelimit.RateLimit;
import org.th.config.ratelimit.RateLimit.Tier;

import java.util.List;

@RestController
@RequestMapping("/api/favorites")
@RequiredArgsConstructor
@Tag(name = "Favorites", description = "User favorites management APIs")
public class FavoriteController {

    private final FavoriteService favoriteService;

    /**
     * Get user's favorite shops
     */
    @GetMapping
    @RateLimit(tier = Tier.IO_INTENSIVE)
    @Operation(summary = "Get favorites", description = "Get all favorite shops for the authenticated user")
    public ResponseEntity<ApiResponse<List<ShopListDTO>>> getFavorites(
            @AuthenticationPrincipal User user) {

        List<ShopListDTO> favorites = favoriteService.getUserFavorites(user);
        return ResponseEntity.ok(ApiResponse.success(
                "Found " + favorites.size() + " favorites", favorites));
    }

    /**
     * Add shop to favorites
     */
    @PostMapping("/{shopId}")
    @RateLimit(tier = Tier.WRITE)
    @Operation(summary = "Add to favorites", description = "Add a shop to user's favorites")
    public ResponseEntity<ApiResponse<Void>> addToFavorites(
            @Parameter(description = "Shop ID") @PathVariable Long shopId,
            @Parameter(description = "Optional notes") @RequestParam(required = false) String notes,
            @AuthenticationPrincipal User user) {

        try {
            favoriteService.addToFavorites(shopId, user, notes);
            return ResponseEntity.ok(ApiResponse.success("Shop added to favorites", null));
        } catch (IllegalStateException e) {
            return ResponseEntity.ok(ApiResponse.error(e.getMessage()));
        }
    }

    /**
     * Remove shop from favorites
     */
    @DeleteMapping("/{shopId}")
    @RateLimit(tier = Tier.WRITE)
    @Operation(summary = "Remove from favorites", description = "Remove a shop from user's favorites")
    public ResponseEntity<ApiResponse<Void>> removeFromFavorites(
            @Parameter(description = "Shop ID") @PathVariable Long shopId,
            @AuthenticationPrincipal User user) {

        favoriteService.removeFromFavorites(shopId, user);
        return ResponseEntity.ok(ApiResponse.success("Shop removed from favorites", null));
    }

    /**
     * Check if shop is favorited
     */
    @GetMapping("/check/{shopId}")
    @RateLimit(tier = Tier.IO_INTENSIVE)
    @Operation(summary = "Check if favorited", description = "Check if a shop is in user's favorites")
    public ResponseEntity<ApiResponse<Boolean>> isFavorited(
            @Parameter(description = "Shop ID") @PathVariable Long shopId,
            @AuthenticationPrincipal User user) {

        boolean isFavorited = favoriteService.isFavorited(shopId, user);
        return ResponseEntity.ok(ApiResponse.success("Favorite status retrieved", isFavorited));
    }

    /**
     * Update favorite notes
     */
    @PutMapping("/{shopId}/notes")
    @RateLimit(tier = Tier.WRITE)
    @Operation(summary = "Update notes", description = "Update notes for a favorited shop")
    public ResponseEntity<ApiResponse<Void>> updateNotes(
            @Parameter(description = "Shop ID") @PathVariable Long shopId,
            @RequestParam String notes,
            @RequestParam(required = false) String notesMm,
            @AuthenticationPrincipal User user) {

        try {
            favoriteService.updateNotes(shopId, user, notes, notesMm);
            return ResponseEntity.ok(ApiResponse.success("Notes updated successfully", null));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.ok(ApiResponse.error(e.getMessage()));
        }
    }
}
