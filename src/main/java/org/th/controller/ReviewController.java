package org.th.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.th.dto.ApiResponse;
import org.th.dto.CreateReviewRequest;
import org.th.dto.OwnerResponseRequest;
import org.th.dto.ReviewSummaryDTO;
import org.th.entity.User;
import org.th.service.ReviewService;
import org.th.config.ratelimit.RateLimit;
import org.th.config.ratelimit.RateLimit.Tier;

import java.util.List;

@RestController
@RequestMapping("/api/reviews")
@RequiredArgsConstructor
@Tag(name = "Reviews", description = "Shop review management APIs")
public class ReviewController {

    private final ReviewService reviewService;
    private final org.th.service.MenuItemReviewService menuItemReviewService;

    /**
     * Get reviews for a shop
     */
    @GetMapping("/shop/{shopId}")
    @RateLimit(tier = Tier.PUBLIC)
    @Operation(summary = "Get shop reviews", description = "Get all reviews for a specific shop")
    public ResponseEntity<ApiResponse<List<ReviewSummaryDTO>>> getShopReviews(
            @Parameter(description = "Shop ID") @PathVariable Long shopId,
            @Parameter(description = "Show only visible reviews") @RequestParam(defaultValue = "true") boolean onlyVisible) {

        List<ReviewSummaryDTO> reviews = reviewService.getShopReviews(shopId, onlyVisible);
        return ResponseEntity.ok(ApiResponse.success(
                "Found " + reviews.size() + " reviews", reviews));
    }

    /**
     * Create a new review
     */
    @PostMapping
    @RateLimit(tier = Tier.WRITE)
    @Operation(summary = "Create review", description = "Add a new review for a shop (requires authentication)")
    public ResponseEntity<ApiResponse<ReviewSummaryDTO>> createReview(
            @Valid @RequestBody CreateReviewRequest request,
            @AuthenticationPrincipal User user) {

        try {
            ReviewSummaryDTO review = reviewService.createReview(request, user);
            return ResponseEntity.ok(ApiResponse.success("Review created successfully", review));
        } catch (IllegalStateException e) {
            return ResponseEntity.ok(ApiResponse.error(e.getMessage()));
        }
    }

    /**
     * Update a review
     */
    @PutMapping("/{id}")
    @RateLimit(tier = Tier.WRITE)
    @Operation(summary = "Update review", description = "Update your own review")
    public ResponseEntity<ApiResponse<ReviewSummaryDTO>> updateReview(
            @Parameter(description = "Review ID") @PathVariable Long id,
            @Valid @RequestBody CreateReviewRequest request,
            @AuthenticationPrincipal User user) {

        try {
            ReviewSummaryDTO review = reviewService.updateReview(id, request, user);
            return ResponseEntity.ok(ApiResponse.success("Review updated successfully", review));
        } catch (SecurityException e) {
            return ResponseEntity.ok(ApiResponse.error(e.getMessage()));
        }
    }

    /**
     * Delete a review
     */
    @DeleteMapping("/{id}")
    @RateLimit(tier = Tier.WRITE)
    @Operation(summary = "Delete review", description = "Delete your own review")
    public ResponseEntity<ApiResponse<Void>> deleteReview(
            @Parameter(description = "Review ID") @PathVariable Long id,
            @AuthenticationPrincipal User user) {

        try {
            reviewService.deleteReview(id, user);
            return ResponseEntity.ok(ApiResponse.success("Review deleted successfully", null));
        } catch (SecurityException e) {
            return ResponseEntity.ok(ApiResponse.error(e.getMessage()));
        }
    }

    /**
     * Add owner response to a review
     */
    @PostMapping("/{id}/owner-response")
    @RateLimit(tier = Tier.WRITE)
    @Operation(summary = "Add owner response", description = "Shop owner responds to a review")
    public ResponseEntity<ApiResponse<ReviewSummaryDTO>> addOwnerResponse(
            @Parameter(description = "Review ID") @PathVariable Long id,
            @Valid @RequestBody OwnerResponseRequest request) {

        ReviewSummaryDTO review = reviewService.addOwnerResponse(id, request);
        return ResponseEntity.ok(ApiResponse.success("Response added successfully", review));
    }

    /**
     * Mark review as helpful
     */
    @PostMapping("/{id}/helpful")
    @RateLimit(tier = Tier.IO_INTENSIVE)
    @Operation(summary = "Mark helpful", description = "Mark a review as helpful")
    public ResponseEntity<ApiResponse<Void>> markHelpful(
            @Parameter(description = "Review ID") @PathVariable Long id) {

        reviewService.markHelpful(id);
        return ResponseEntity.ok(ApiResponse.success("Review marked as helpful", null));
    }

    /**
     * Get user's own reviews
     */
    @GetMapping("/my-reviews")
    @RateLimit(tier = Tier.IO_INTENSIVE)
    @Operation(summary = "Get my reviews", description = "Get all reviews by the authenticated user")
    public ResponseEntity<ApiResponse<List<ReviewSummaryDTO>>> getMyReviews(
            @AuthenticationPrincipal User user) {

        List<ReviewSummaryDTO> reviews = reviewService.getUserReviews(user.getId());
        return ResponseEntity.ok(ApiResponse.success(
                "Found " + reviews.size() + " reviews", reviews));
    }

    /**
     * Rate a specific menu item (Dish-level rating)
     */
    @PostMapping("/menu/{itemId}")
    @RateLimit(tier = Tier.WRITE)
    @Operation(summary = "Rate a dish", description = "Rate a specific menu item")
    public ResponseEntity<ApiResponse<org.th.dto.MenuItemReviewDTO>> rateDish(
            @Parameter(description = "Menu Item ID") @PathVariable Long itemId,
            @RequestParam Integer rating,
            @RequestParam(required = false) String comment,
            @AuthenticationPrincipal User user) {

        try {
            org.th.dto.MenuItemReviewDTO review = menuItemReviewService.addReview(itemId, rating, comment, user);
            return ResponseEntity.ok(ApiResponse.success("Dish rated successfully", review));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    /**
     * Get reviews for a menu item
     */
    @GetMapping("/menu/{itemId}")
    @RateLimit(tier = Tier.PUBLIC)
    @Operation(summary = "Get dish reviews", description = "Get reviews for a specific menu item")
    public ResponseEntity<ApiResponse<List<org.th.dto.MenuItemReviewDTO>>> getDishReviews(
            @Parameter(description = "Menu Item ID") @PathVariable Long itemId) {

        List<org.th.dto.MenuItemReviewDTO> reviews = menuItemReviewService.getReviews(itemId);
        return ResponseEntity.ok(ApiResponse.success("Dish reviews retrieved", reviews));
    }
}
