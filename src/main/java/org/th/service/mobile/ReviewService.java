package org.th.service.mobile;

import org.th.repository.*;
import org.th.entity.*;
import org.th.entity.shops.*;

import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.th.dto.CreateReviewRequest;
import org.th.dto.OwnerResponseRequest;
import org.th.dto.ReviewSummaryDTO;
import org.th.dto.ReviewCommentDTO;
import org.th.dto.ReviewPhotoDTO;
import org.th.entity.User;
import org.th.entity.shops.ShopReview;
import org.th.entity.shops.Shop;
import org.th.entity.shops.ReviewComment;
import org.th.entity.shops.ReviewPhoto;
import org.th.repository.ShopReviewRepository;
import org.th.repository.ShopRepository;
import org.th.repository.ReviewCommentRepository;
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
@lombok.extern.slf4j.Slf4j
public class ReviewService {

        private final ShopReviewRepository shopReviewRepository;
        private final ShopRepository shopRepository;
        private final ReviewCommentRepository reviewCommentRepository;

        /**
         * Get all reviews for a shop
         */
        @Cacheable(value = "shopReviews", key = "#shopId + '-' + #onlyVisible")
        public List<ReviewSummaryDTO> getShopReviews(Long shopId, boolean onlyVisible) {
                log.info("Fetching reviews for shop ID: {}", shopId);

                List<ShopReview> reviews = onlyVisible
                                ? shopReviewRepository.findByShopIdAndIsVisibleTrueOrderByCreatedAtDesc(shopId)
                                : shopReviewRepository.findByShopIdOrderByCreatedAtDesc(shopId);

                return reviews.stream()
                                .map(this::convertToDTO)
                                .collect(Collectors.toList());
        }

        /**
         * Create a new review
         */
        @Transactional
        public ReviewSummaryDTO createReview(CreateReviewRequest request, User user) {
                log.info("Creating review for shop {} by user {}", request.getShopId(), user.getUsername());

                // Check if user already reviewed this shop
                if (shopReviewRepository.existsByUserIdAndShopId(user.getId(), request.getShopId())) {
                        throw new IllegalStateException("You have already reviewed this shop");
                }

                // Get shop
                Shop shop = shopRepository.findById(request.getShopId())
                                .orElseThrow(() -> new IllegalArgumentException("Shop not found"));

                // Create review
                ShopReview review = new ShopReview();
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

                // Add photos if present
                if (request.getPhotoUrls() != null) {
                        for (String url : request.getPhotoUrls()) {
                                org.th.entity.shops.ReviewPhoto photo = new org.th.entity.shops.ReviewPhoto();
                                photo.setUrl(url);
                                photo.setThumbnailUrl(url); // Can be optimized later
                                photo.setShopReview(review);
                                review.getPhotos().add(photo);
                        }
                }

                ShopReview savedReview = shopReviewRepository.save(review);

                // Update shop rating
                updateShopRating(request.getShopId());

                log.info("Review created successfully with ID: {}", savedReview.getId());
                return convertToDTO(savedReview);
        }

        /**
         * Update a review
         */
        @Transactional
        public ReviewSummaryDTO updateReview(Long reviewId, CreateReviewRequest request, User user) {
                log.info("Updating review ID: {} by user {}", reviewId, user.getUsername());

                ShopReview review = shopReviewRepository.findById(reviewId)
                                .orElseThrow(() -> new IllegalArgumentException("Review not found"));

                // Check ownership
                if (!review.getUser().getId().equals(user.getId())) {
                        throw new SecurityException("You can only update your own reviews");
                }

                review.setRating(request.getRating());
                review.setComment(request.getComment());
                review.setCommentMm(request.getCommentMm());

                // TODO: Handle photo updates if needed

                ShopReview updatedReview = shopReviewRepository.save(review);

                // Update shop rating
                updateShopRating(review.getShop().getId());

                return convertToDTO(updatedReview);
        }

        /**
         * Delete a review
         */
        @Transactional
        public void deleteReview(Long reviewId, User user) {
                log.info("Deleting review ID: {} by user {}", reviewId, user.getUsername());

                ShopReview review = shopReviewRepository.findById(reviewId)
                                .orElseThrow(() -> new IllegalArgumentException("Review not found"));

                // Check ownership (or admin)
                if (!review.getUser().getId().equals(user.getId()) &&
                                !user.getRole().name().equals("ADMIN")) {
                        throw new SecurityException("You can only delete your own reviews");
                }

                Long shopId = review.getShop().getId();
                shopReviewRepository.delete(review);

                // Update shop rating
                updateShopRating(shopId);
        }

        /**
         * Add owner response to review
         */
        @Transactional
        @CacheEvict(value = "shopReviews", allEntries = true)
        public ReviewSummaryDTO addOwnerResponse(Long reviewId, OwnerResponseRequest request) {
                log.info("Adding owner response to review ID: {}", reviewId);

                ShopReview review = shopReviewRepository.findById(reviewId)
                                .orElseThrow(() -> new IllegalArgumentException("Review not found"));

                review.setOwnerResponse(request.getOwnerResponse());
                review.setOwnerResponseMm(request.getOwnerResponseMm());
                review.setOwnerResponseAt(LocalDateTime.now());

                ShopReview updatedReview = shopReviewRepository.save(review);
                return convertToDTO(updatedReview);
        }

        /**
         * Mark review as helpful
         */
        @Transactional
        @CacheEvict(value = "shopReviews", allEntries = true)
        public void markHelpful(Long reviewId) {
                log.info("Marking review ID: {} as helpful", reviewId);

                ShopReview review = shopReviewRepository.findById(reviewId)
                                .orElseThrow(() -> new IllegalArgumentException("Review not found"));

                review.setHelpfulCount(review.getHelpfulCount() + 1);
                shopReviewRepository.save(review);
        }

        /**
         * Get user's reviews
         */
        public List<ReviewSummaryDTO> getUserReviews(Long userId) {
                log.info("Fetching reviews for user ID: {}", userId);

                List<ShopReview> reviews = shopReviewRepository.findByUserIdOrderByCreatedAtDesc(userId);
                return reviews.stream()
                                .map(this::convertToDTO)
                                .collect(Collectors.toList());
        }

        /**
         * Add comment/reply to a review
         */
        @Transactional
        @org.springframework.cache.annotation.CacheEvict(value = "shopReviews", allEntries = true)
        public org.th.dto.ReviewCommentDTO addComment(Long reviewId, String content, User user, Long parentCommentId) {
                ShopReview review = shopReviewRepository.findById(reviewId)
                                .orElseThrow(() -> new IllegalArgumentException("Review not found"));

                org.th.entity.shops.ReviewComment comment = new org.th.entity.shops.ReviewComment();
                comment.setContent(content);
                comment.setUser(user);
                comment.setShopReview(review);

                if (parentCommentId != null) {
                        org.th.entity.shops.ReviewComment parent = reviewCommentRepository.findById(parentCommentId)
                                        .orElseThrow(() -> new IllegalArgumentException("Parent comment not found"));
                        comment.setParentComment(parent);
                }

                org.th.entity.shops.ReviewComment saved = reviewCommentRepository.save(comment);
                return convertToCommentDTO(saved);
        }

        @Transactional
        @org.springframework.cache.annotation.CacheEvict(value = "shopReviews", allEntries = true)
        public org.th.dto.ReviewCommentDTO replyToComment(Long parentCommentId, String content, User user) {
                org.th.entity.shops.ReviewComment parent = reviewCommentRepository.findById(parentCommentId)
                                .orElseThrow(() -> new IllegalArgumentException("Parent comment not found"));

                org.th.entity.shops.ReviewComment reply = new org.th.entity.shops.ReviewComment();
                reply.setContent(content);
                reply.setUser(user);
                reply.setParentComment(parent);

                // Inherit context
                reply.setShopReview(parent.getShopReview());
                reply.setMenuItemReview(parent.getMenuItemReview());

                org.th.entity.shops.ReviewComment saved = reviewCommentRepository.save(reply);
                return convertToCommentDTO(saved);
        }

        @Transactional
        @org.springframework.cache.annotation.CacheEvict(value = "shopReviews", allEntries = true)
        public void deleteComment(Long commentId, User user) {
                org.th.entity.shops.ReviewComment comment = reviewCommentRepository.findById(commentId)
                                .orElseThrow(() -> new IllegalArgumentException("Comment not found"));

                if (!comment.getUser().getId().equals(user.getId()) && !user.getRole().name().equals("ADMIN")) {
                        throw new SecurityException("You can only delete your own comments");
                }

                reviewCommentRepository.delete(comment);
        }

        @Transactional
        @org.springframework.cache.annotation.CacheEvict(value = "shopReviews", allEntries = true)
        public org.th.dto.ReviewCommentDTO editComment(Long commentId, String content, User user) {
                org.th.entity.shops.ReviewComment comment = reviewCommentRepository.findById(commentId)
                                .orElseThrow(() -> new IllegalArgumentException("Comment not found"));

                if (!comment.getUser().getId().equals(user.getId())) {
                        throw new SecurityException("You can only edit your own comments");
                }

                comment.setContent(content);
                org.th.entity.shops.ReviewComment saved = reviewCommentRepository.save(comment);
                return convertToCommentDTO(saved);
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
                Double avgRating = shopReviewRepository.calculateAverageRating(shopId);
                long count = shopReviewRepository.countByShopId(shopId);

                Shop shop = shopRepository.findById(shopId).orElseThrow();
                shop.setRatingAvg(
                                avgRating != null ? BigDecimal.valueOf(avgRating).setScale(2, RoundingMode.HALF_UP)
                                                : BigDecimal.ZERO);
                shop.setRatingCount((int) count);
                shopRepository.save(shop);

                log.info("Updated shop {} rating: {} ({} reviews)", shopId, avgRating, count);
        }

        /**
         * Convert ShopReview entity to DTO
         */
        private ReviewSummaryDTO convertToDTO(ShopReview review) {
                List<ReviewPhotoDTO> photos = review.getPhotos().stream()
                                .map(p -> ReviewPhotoDTO.builder()
                                                .id(p.getId())
                                                .url(p.getUrl())
                                                .thumbnailUrl(p.getThumbnailUrl())
                                                .build())
                                .collect(Collectors.toList());

                List<ReviewCommentDTO> comments = review.getComments().stream()
                                .filter(c -> c.getParentComment() == null) // Top level comments
                                .map(this::convertToCommentDTO)
                                .collect(Collectors.toList());

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
                                .photos(photos)
                                .comments(comments)
                                .build();
        }

        private ReviewCommentDTO convertToCommentDTO(ReviewComment comment) {
                List<ReviewCommentDTO> replies = comment.getReplies().stream()
                                .map(this::convertToCommentDTO)
                                .collect(Collectors.toList());

                return ReviewCommentDTO.builder()
                                .id(comment.getId())
                                .content(comment.getContent())
                                .userName(comment.getUser().getFullName())
                                .createdAt(comment.getCreatedAt())
                                .replies(replies)
                                .build();
        }
}
