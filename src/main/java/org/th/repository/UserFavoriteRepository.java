package org.th.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.th.entity.UserFavorite;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserFavoriteRepository extends JpaRepository<UserFavorite, Long> {

    /**
     * Find all favorites for a specific user
     */
    @Query("SELECT uf FROM UserFavorite uf " +
           "JOIN FETCH uf.shop s " +
           "WHERE uf.user.id = :userId " +
           "ORDER BY uf.createdAt DESC")
    List<UserFavorite> findByUserIdWithShop(@Param("userId") Long userId);

    /**
     * Check if user has favorited a specific shop
     */
    @Query("SELECT uf FROM UserFavorite uf " +
           "WHERE uf.user.id = :userId AND uf.shop.id = :shopId")
    Optional<UserFavorite> findByUserIdAndShopId(@Param("userId") Long userId,
                                                   @Param("shopId") Long shopId);

    /**
     * Count total favorites for a user
     */
    @Query("SELECT COUNT(uf) FROM UserFavorite uf WHERE uf.user.id = :userId")
    Long countByUserId(@Param("userId") Long userId);

    /**
     * Find favorite shops by user and category
     */
    @Query("SELECT uf FROM UserFavorite uf " +
           "JOIN FETCH uf.shop s " +
           "WHERE uf.user.id = :userId AND s.category = :category " +
           "ORDER BY uf.createdAt DESC")
    List<UserFavorite> findByUserIdAndCategory(@Param("userId") Long userId,
                                                 @Param("category") String category);

    /**
     * Check if favorite exists
     */
    boolean existsByUserIdAndShopId(Long userId, Long shopId);

    /**
     * Delete a favorite
     */
    void deleteByUserIdAndShopId(Long userId, Long shopId);
}