package org.th.service;

import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.th.dto.CreateReviewRequest;
import org.th.dto.OwnerResponseRequest;
import org.th.dto.ReviewSummaryDTO;
import org.th.entity.User;
import org.th.entity.shops.Review;
import org.th.entity.shops.Shop;
import org.th.repository.ReviewRepository;
import org.th.repository.ShopRepository;

import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ReviewService {

    private static final Logger logger = LoggerFactory.getLogger(ReviewService.class);

    private final ReviewRepository reviewRepository;
    private final ShopRepository shopRepository;

    /**
     * Get all reviews for a shop
     */
    @Cacheable(value = "shopReviews", key = "#shopId + '-' + #onlyVisible")
    public List<ReviewSummaryDTO> getShopReviews(Long shopId, boolean onlyVisible) {
        logger.info("Fetching reviews for shop ID: {}", shopId);

        List<Review> reviews = onlyVisible
                ? reviewRepository.findByShopIdAndIsVisibleTrueOrderByCreatedAtDesc(shopId)
                : reviewRepository.findByShopIdOrderByCreatedAtDesc(shopId);

        return reviews.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    /**
     * Create a new review
     */
    @Transactional
    public ReviewSummaryDTO createReview(CreateReviewRequest request, User user) {
        logger.info("Creating review for shop {} by user {}", request.getShopId(), user.getUsername());

        // Check if user already reviewed this shop
        if (reviewRepository.existsByUserIdAndShopId(user.getId(), request.getShopId())) {
            throw new IllegalStateException("You have already reviewed this shop");
        }

        // Get shop
        Shop shop = shopRepository.findById(request.getShopId())
                .orElseThrow(() -> new IllegalArgumentException("Shop not found"));

        // Create review
        Review review = new Review();
        review.setShop(shop);
        review.setUser(user);
        review.setRating(request.getRating());
        review.setComment(request.getComment());
        review.setCommentMm(request.getCommentMm());
        review.setReviewerName(user.getFullName() != null ? user.getFullName() : user.getUsername());
        review.setReviewerEmail(user.getEmail());
        review.setIsVisible(true);
        review.setIsVerified(false);
        review.setHelpfulCount(0);

        Review savedReview = reviewRepository.save(review);

        // Update shop rating
        updateShopRating(request.getShopId());

        logger.info("Review created successfully with ID: {}", savedReview.getId());
        return convertToDTO(savedReview);
    }

    /**
     * Update a review
     */
    @Transactional
    public ReviewSummaryDTO updateReview(Long reviewId, CreateReviewRequest request, User user) {
        logger.info("Updating review ID: {} by user {}", reviewId, user.getUsername());

        Review review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new IllegalArgumentException("Review not found"));

        // Check ownership
        if (!review.getUser().getId().equals(user.getId())) {
            throw new SecurityException("You can only update your own reviews");
        }

        review.setRating(request.getRating());
        review.setComment(request.getComment());
        review.setCommentMm(request.getCommentMm());

        Review updatedReview = reviewRepository.save(review);

        // Update shop rating
        updateShopRating(review.getShop().getId());

        return convertToDTO(updatedReview);
    }

    /**
     * Delete a review
     */
    @Transactional
    public void deleteReview(Long reviewId, User user) {
        logger.info("Deleting review ID: {} by user {}", reviewId, user.getUsername());

        Review review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new IllegalArgumentException("Review not found"));

        // Check ownership (or admin)
        if (!review.getUser().getId().equals(user.getId()) &&
                !user.getRole().name().equals("ADMIN")) {
            throw new SecurityException("You can only delete your own reviews");
        }

        Long shopId = review.getShop().getId();
        reviewRepository.delete(review);

        // Update shop rating
        updateShopRating(shopId);
    }

    /**
     * Add owner response to review
     */
    @Transactional
    @CacheEvict(value = "shopReviews", allEntries = true)
    public ReviewSummaryDTO addOwnerResponse(Long reviewId, OwnerResponseRequest request) {
        logger.info("Adding owner response to review ID: {}", reviewId);

        Review review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new IllegalArgumentException("Review not found"));

        review.setOwnerResponse(request.getOwnerResponse());
        review.setOwnerResponseMm(request.getOwnerResponseMm());
        review.setOwnerResponseAt(LocalDateTime.now());

        Review updatedReview = reviewRepository.save(review);
        return convertToDTO(updatedReview);
    }

    /**
     * Mark review as helpful
     */
    @Transactional
    @CacheEvict(value = "shopReviews", allEntries = true)
    public void markHelpful(Long reviewId) {
        logger.info("Marking review ID: {} as helpful", reviewId);

        Review review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new IllegalArgumentException("Review not found"));

        review.setHelpfulCount(review.getHelpfulCount() + 1);
        reviewRepository.save(review);
    }

    /**
     * Get user's reviews
     */
    public List<ReviewSummaryDTO> getUserReviews(Long userId) {
        logger.info("Fetching reviews for user ID: {}", userId);

        List<Review> reviews = reviewRepository.findByUserIdOrderByCreatedAtDesc(userId);
        return reviews.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    /**
     * Update shop rating based on reviews
     */
    @Caching(evict = {
            @CacheEvict(value = "shopDetails", key = "#shopId"),
            @CacheEvict(value = "homeShops", allEntries = true),
            @CacheEvict(value = "shopReviews", allEntries = true)
    })
    private void updateShopRating(Long shopId) {
        Double avgRating = reviewRepository.calculateAverageRating(shopId);
        long count = reviewRepository.countByShopId(shopId);

        Shop shop = shopRepository.findById(shopId).orElseThrow();
        shop.setRatingAvg(
                avgRating != null ? BigDecimal.valueOf(avgRating).setScale(2, RoundingMode.HALF_UP) : BigDecimal.ZERO);
        shop.setRatingCount((int) count);
        shopRepository.save(shop);

        logger.info("Updated shop {} rating: {} ({} reviews)", shopId, avgRating, count);
    }

    /**
     * Convert Review entity to DTO
     */
    private ReviewSummaryDTO convertToDTO(Review review) {
        return ReviewSummaryDTO.builder()
                .id(review.getId())
                .rating(review.getRating())
                .comment(review.getComment())
                .commentMm(review.getCommentMm())
                .reviewerName(review.getReviewerName())
                .helpfulCount(review.getHelpfulCount())
                .ownerResponse(review.getOwnerResponse())
                .ownerResponseMm(review.getOwnerResponseMm())
                .createdAt(review.getCreatedAt())
                .build();
    }
}
