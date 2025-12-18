package org.th.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.th.entity.UserMenuFavorite;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserMenuFavoriteRepository extends JpaRepository<UserMenuFavorite, Long> {

    /**
     * Find all menu favorites for a specific user
     */
    @Query("SELECT umf FROM UserMenuFavorite umf " +
            "JOIN FETCH umf.menuItem mi " +
            "JOIN FETCH mi.shop s " +
            "WHERE umf.user.id = :userId " +
            "ORDER BY umf.createdAt DESC")
    List<UserMenuFavorite> findByUserIdWithItemAndShop(@Param("userId") Long userId);

    /**
     * Check if user has favorited a specific menu item
     */
    Optional<UserMenuFavorite> findByUserIdAndMenuItemId(Long userId, Long menuItemId);

    /**
     * Check if favorite exists
     */
    boolean existsByUserIdAndMenuItemId(Long userId, Long menuItemId);

    /**
     * Delete a favorite
     */
    void deleteByUserIdAndMenuItemId(Long userId, Long menuItemId);

    /**
     * Count favorites for a menu item
     */
    long countByMenuItemId(Long menuItemId);

    /**
     * Count favorites by user
     */
    long countByUserId(Long userId);
}
