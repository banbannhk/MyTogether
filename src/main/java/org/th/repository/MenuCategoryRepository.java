package org.th.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.th.entity.shops.MenuCategory;

import java.util.Optional;

@Repository
public interface MenuCategoryRepository extends JpaRepository<MenuCategory, Long> {

    /**
     * Find menu category by ID with items and photos eagerly loaded
     */
    @Query("SELECT DISTINCT mc FROM MenuCategory mc " +
            "LEFT JOIN FETCH mc.items mi " +
            "LEFT JOIN FETCH mc.photos " +
            "LEFT JOIN FETCH mi.photos " +
            "WHERE mc.id = :id")
    Optional<MenuCategory> findByIdWithItems(@Param("id") Long id);

    /**
     * Search menu categories
     */
    @Query("SELECT DISTINCT mc FROM MenuCategory mc " +
            "JOIN FETCH mc.shop s " +
            "LEFT JOIN FETCH mc.photos " +
            "WHERE s.isActive = true AND mc.isActive = true AND " +
            "(LOWER(mc.name) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
            "LOWER(mc.nameMm) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
            "LOWER(mc.nameEn) LIKE LOWER(CONCAT('%', :keyword, '%')))")
    java.util.List<MenuCategory> searchMenuCategories(@Param("keyword") String keyword);

    /**
     * Find menu categories for a shop with items
     */
    @Query("SELECT DISTINCT mc FROM MenuCategory mc " +
            "LEFT JOIN FETCH mc.items " +
            "WHERE mc.shop.id = :shopId")
    java.util.List<MenuCategory> findByShopIdWithItems(@Param("shopId") Long shopId);
}
