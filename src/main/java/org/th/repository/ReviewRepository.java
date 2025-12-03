package org.th.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.th.entity.shops.Review;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface ReviewRepository extends JpaRepository<Review, Long> {

    /**
     * Find all reviews for a specific shop
     * 
     * @param shopId Shop ID
     * @return List of reviews
     */
    List<Review> findByShopIdOrderByCreatedAtDesc(Long shopId);

    /**
     * Find visible reviews for a shop
     * 
     * @param shopId Shop ID
     * @return List of visible reviews
     */
    List<Review> findByShopIdAndIsVisibleTrueOrderByCreatedAtDesc(Long shopId);

    /**
     * Find reviews by a specific user
     * 
     * @param userId User ID
     * @return List of user's reviews
     */
    List<Review> findByUserIdOrderByCreatedAtDesc(Long userId);

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
    @Query("SELECT AVG(r.rating) FROM Review r WHERE r.shop.id = :shopId AND r.isVisible = true")
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
}
