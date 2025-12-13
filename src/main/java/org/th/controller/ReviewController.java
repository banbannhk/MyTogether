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
            @Valid @RequestBody org.th.dto.CreateMenuItemReviewRequest request,
            @AuthenticationPrincipal User user) {

        try {
            org.th.dto.MenuItemReviewDTO review = menuItemReviewService.addReview(itemId, request, user);
            return ResponseEntity.ok(ApiResponse.success("Dish rated successfully", review));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    /**
     * Add a comment to a review (Shop)
     */
    @PostMapping("/{reviewId}/comments")
    @RateLimit(tier = Tier.WRITE)
    @Operation(summary = "Add comment to review", description = "Add a comment to a shop review")
    public ResponseEntity<ApiResponse<org.th.dto.ReviewCommentDTO>> addShopReviewComment(
            @Parameter(description = "Review ID") @PathVariable Long reviewId,
            @RequestBody org.th.dto.CreateReplyRequest request,
            @AuthenticationPrincipal User user) {

        org.th.dto.ReviewCommentDTO comment = reviewService.addComment(reviewId, request.getContent(), user, null);
        return ResponseEntity.ok(ApiResponse.success("Comment added successfully", comment));
    }

    /**
     * Reply to a comment
     */
    @PostMapping("/comments/{commentId}/reply")
    @RateLimit(tier = Tier.WRITE)
    @Operation(summary = "Reply to comment", description = "Reply to an existing comment")
    public ResponseEntity<ApiResponse<org.th.dto.ReviewCommentDTO>> replyToComment(
            @Parameter(description = "Parent Comment ID") @PathVariable Long commentId,
            @RequestBody org.th.dto.CreateReplyRequest request,
            @AuthenticationPrincipal User user) {

        // For replies, we need the root review ID, but our service currently asks for
        // it.
        // Ideally we should just reply to the comment.
        // Refactoring service to handle this would be best, but for now we might need
        // to look up the comment first.
        // Let's assume we can pass null for reviewId if parentId is present, or update
        // service.
        // Actually, ReviewService.addComment requires reviewId.

        // Quick fix: Look up comment in service or pass null if service handles it.
        // Checking ReviewService code... it requires reviewId to find the review.
        // We should add a dedicated replyToComment method in service or lookup parent
        // to find reviewId.

        // Implemented simpler approach: The RequestBody should probably contain both if
        // needed,
        // but typically /comments/{id}/reply implies context.
        // Let's defer to a new service method: replyToComment(Long parentCommentId,
        // String content, User user)

        org.th.dto.ReviewCommentDTO reply = reviewService.replyToComment(commentId, request.getContent(), user);
        return ResponseEntity.ok(ApiResponse.success("Reply added successfully", reply));
    }

    /**
     * Edit a comment
     */
    @PutMapping("/comments/{commentId}")
    @RateLimit(tier = Tier.WRITE)
    @Operation(summary = "Edit comment", description = "Edit your existing comment")
    public ResponseEntity<ApiResponse<org.th.dto.ReviewCommentDTO>> editComment(
            @Parameter(description = "Comment ID") @PathVariable Long commentId,
            @RequestBody org.th.dto.CreateReplyRequest request,
            @AuthenticationPrincipal User user) {

        org.th.dto.ReviewCommentDTO comment = reviewService.editComment(commentId, request.getContent(), user);
        return ResponseEntity.ok(ApiResponse.success("Comment updated successfully", comment));
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

    /**
     * Add a comment to a review (Menu Item)
     */
    @PostMapping("/menu/{reviewId}/comments")
    @RateLimit(tier = Tier.WRITE)
    @Operation(summary = "Add comment to dish review", description = "Add a comment to a menu item review")
    public ResponseEntity<ApiResponse<org.th.dto.ReviewCommentDTO>> addDishReviewComment(
            @Parameter(description = "Review ID") @PathVariable Long reviewId,
            @RequestBody org.th.dto.CreateReplyRequest request,
            @AuthenticationPrincipal User user) {

        org.th.dto.ReviewCommentDTO comment = menuItemReviewService.addComment(reviewId, request.getContent(), user);
        return ResponseEntity.ok(ApiResponse.success("Comment added successfully", comment));
    }

    /**
     * Delete a comment (Generic)
     */
    @DeleteMapping("/comments/{commentId}")
    @RateLimit(tier = Tier.WRITE)
    @Operation(summary = "Delete comment", description = "Delete your own comment")
    public ResponseEntity<ApiResponse<Void>> deleteComment(
            @Parameter(description = "Comment ID") @PathVariable Long commentId,
            @AuthenticationPrincipal User user) {

        try {
            reviewService.deleteComment(commentId, user);
            return ResponseEntity.ok(ApiResponse.success("Comment deleted successfully", null));
        } catch (SecurityException e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }
}
