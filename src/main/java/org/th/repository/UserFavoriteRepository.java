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

       List<UserFavorite> findByUserIdOrderByCreatedAtDesc(Long userId);

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
        * Count favorites for a shop
        */
       long countByShopId(Long shopId);

       /**
        * Count recent favorites for a shop
        */
       long countByShopIdAndCreatedAtAfter(Long shopId, java.time.LocalDateTime after);

       /**
        * Bulk count favorites by Shop ID since a date
        */
       @Query("SELECT uf.shop.id, COUNT(uf) FROM UserFavorite uf " +
                     "WHERE uf.createdAt >= :since " +
                     "GROUP BY uf.shop.id")
       List<Object[]> countFavoritesByShopSince(@Param("since") java.time.LocalDateTime since);

       /**
        * Find favorites with shop and photos eagerly loaded
        */
       @Query("SELECT uf FROM UserFavorite uf " +
                     "JOIN FETCH uf.shop s " +
                     "LEFT JOIN FETCH s.photos " +
                     "WHERE uf.user.id = :userId " +
                     "ORDER BY uf.createdAt DESC")
       List<UserFavorite> findByUserIdWithShopAndPhotos(@Param("userId") Long userId);

       /**
        * Delete a favorite
        */
       void deleteByUserIdAndShopId(Long userId, Long shopId);
}