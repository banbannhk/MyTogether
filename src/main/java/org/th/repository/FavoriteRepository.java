package org.th.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.th.entity.shops.Favorite;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface FavoriteRepository extends JpaRepository<Favorite, Long> {

    /**
     * Find all favorites for a user
     * 
     * @param userId User ID
     * @return List of user's favorites
     */
    List<Favorite> findByUserIdOrderByCreatedAtDesc(Long userId);

    /**
     * Find specific favorite by user and shop
     * 
     * @param userId User ID
     * @param shopId Shop ID
     * @return Optional favorite
     */
    Optional<Favorite> findByUserIdAndShopId(Long userId, Long shopId);

    /**
     * Check if user has favorited a shop
     * 
     * @param userId User ID
     * @param shopId Shop ID
     * @return true if exists
     */
    boolean existsByUserIdAndShopId(Long userId, Long shopId);

    /**
     * Delete favorite by user and shop
     * 
     * @param userId User ID
     * @param shopId Shop ID
     */
    void deleteByUserIdAndShopId(Long userId, Long shopId);

    /**
     * Count favorites for a shop
     * 
     * @param shopId Shop ID
     * @return Number of favorites
     */
    long countByShopId(Long shopId);

    /**
     * Count recent favorites for a shop after a specific time.
     * 
     * @param shopId Shop ID
     * @param after  LocalDateTime to filter favorites created after
     * @return Number of recent favorites
     */
    long countByShopIdAndCreatedAtAfter(Long shopId, LocalDateTime after);

    /**
     * Count total favorites by user
     * 
     * @param userId User ID
     * @return Number of favorites
     */
    long countByUserId(Long userId);

    /**
     * Find all favorites for a user with shop data eagerly loaded (fixes N+1 query)
     * Uses JOIN FETCH to load shop in single query
     * 
     * @param userId User ID
     * @return List of user's favorites with shops loaded
     */
    @Query("SELECT f FROM Favorite f JOIN FETCH f.shop s LEFT JOIN FETCH s.photos WHERE f.user.id = :userId ORDER BY f.createdAt DESC")
    List<Favorite> findByUserIdWithShopAndPhotos(@Param("userId") Long userId);

    /**
     * Bulk count favorites by Shop ID since a date
     */
    @Query("SELECT f.shop.id, COUNT(f) FROM Favorite f " +
            "WHERE f.createdAt >= :since " +
            "GROUP BY f.shop.id")
    List<Object[]> countFavoritesByShopSince(@Param("since") LocalDateTime since);
}
