package org.th.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.th.entity.shops.MenuItemReview;

import java.util.List;

@Repository
public interface MenuItemReviewRepository extends JpaRepository<MenuItemReview, Long> {

    List<MenuItemReview> findByMenuItemIdOrderByCreatedAtDesc(Long menuItemId);

    @Query("SELECT AVG(r.rating) FROM MenuItemReview r WHERE r.menuItem.id = :menuItemId")
    Double calculateAverageRating(@Param("menuItemId") Long menuItemId);

    boolean existsByUserIdAndMenuItemId(Long userId, Long menuItemId);
}
