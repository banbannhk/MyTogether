package org.th.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.th.entity.shops.ShopReview;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface ShopReviewRepository extends JpaRepository<ShopReview, Long> {

    /**
     * Find all reviews for a specific shop
     * 
     * @param shopId Shop ID
     * @return List of reviews
     */
    List<ShopReview> findByShopIdOrderByCreatedAtDesc(Long shopId);

    /**
     * Find visible reviews for a shop
     * 
     * @param shopId Shop ID
     * @return List of visible reviews
     */
    List<ShopReview> findByShopIdAndIsVisibleTrueOrderByCreatedAtDesc(Long shopId);

    /**
     * Find top 10 recent visible reviews for a shop
     * 
     * @param shopId Shop ID
     * @return List of top 10 reviews
     */
    List<ShopReview> findTop10ByShopIdAndIsVisibleTrueOrderByCreatedAtDesc(Long shopId);

    /**
     * Find reviews by a specific user
     * 
     * @param userId User ID
     * @return List of user's reviews
     */
    List<ShopReview> findByUserIdOrderByCreatedAtDesc(Long userId);

    /**
     * Count reviews for a shop
     * 
     * @param shopId Shop ID
     * @return Number of reviews
     */
    long countByShopId(Long shopId);

    /**
     * Calculate average rating for a shop
     * 
     * @param shopId Shop ID
     * @return Average rating
     */
    @Query("SELECT AVG(r.rating) FROM ShopReview r WHERE r.shop.id = :shopId AND r.isVisible = true")
    Double calculateAverageRating(@Param("shopId") Long shopId);

    /**
     * Check if user has already reviewed a shop
     * 
     * @param userId User ID
     * @param shopId Shop ID
     * @return true if exists
     */
    boolean existsByUserIdAndShopId(Long userId, Long shopId);

    /**
     * Count recent reviews for a shop
     */
    long countByShopIdAndCreatedAtAfter(Long shopId, LocalDateTime after);

    /**
     * Count total reviews by user
     * 
     * @param userId User ID
     * @return Number of reviews
     */
    long countByUserId(Long userId);

    /**
     * Bulk count reviews by Shop ID since a date
     */
    @Query("SELECT r.shop.id, COUNT(r) FROM ShopReview r " +
            "WHERE r.createdAt >= :since " +
            "GROUP BY r.shop.id")
    List<Object[]> countReviewsByShopSince(@Param("since") LocalDateTime since);
}
