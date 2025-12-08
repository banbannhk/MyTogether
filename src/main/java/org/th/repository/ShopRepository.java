package org.th.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Slice;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.th.entity.shops.Shop;
import org.th.entity.shops.MenuCategory;
import org.th.dto.LocationCountDTO;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface ShopRepository extends JpaRepository<Shop, Long> {

        /**
         * Find all distinct shop categories
         */
        @Query("SELECT DISTINCT s.category FROM Shop s WHERE s.isActive = true AND s.category IS NOT NULL ORDER BY s.category")
        List<String> findDistinctCategories();

        /**
         * Find shop by ID with photos only (avoid MultipleBagFetchException)
         * Other collections are fetched separately
         */
        @Query("SELECT DISTINCT s FROM Shop s " +
                        "LEFT JOIN FETCH s.photos " +
                        "WHERE s.id = :id")
        Optional<Shop> findByIdWithDetails(@Param("id") Long id);

        /**
         * Fetch menu categories with their items for a shop
         * This is called after findByIdWithDetails to load collections separately
         */
        @Query("SELECT DISTINCT mc FROM MenuCategory mc " +
                        "LEFT JOIN FETCH mc.items " +
                        "WHERE mc.shop.id = :shopId")
        List<MenuCategory> findMenuCategoriesWithItems(@Param("shopId") Long shopId);

        /**
         * Fetch operating hours for a shop
         * This is called after findByIdWithDetails to load collections separately
         */
        @Query("SELECT oh FROM OperatingHour oh " +
                        "WHERE oh.shop.id = :shopId")
        List<org.th.entity.shops.OperatingHour> findOperatingHoursByShopId(@Param("shopId") Long shopId);

        /**
         * Find all shops with pagination
         */
        /**
         * Find all shops with pagination (Standard Page)
         */
        Page<Shop> findAll(Pageable pageable);

        /**
         * Find active shops with Slice (No Count Query - Faster)
         */
        Slice<Shop> findByIsActiveTrue(Pageable pageable);

        /**
         * Find shops within a specified radius (in kilometers) from a given location
         * Uses the Haversine formula to calculate distances
         *
         * @param latitude   User's latitude
         * @param longitude  User's longitude
         * @param radiusInKm Search radius in kilometers
         * @return List of shops sorted by distance (nearest first)
         */
        @Query(value = "SELECT * FROM ( " +
                        "SELECT *, " +
                        "(6371 * acos(cos(radians(:latitude)) * cos(radians(latitude)) * " +
                        "cos(radians(longitude) - radians(:longitude)) + sin(radians(:latitude)) * " +
                        "sin(radians(latitude)))) AS distance " +
                        "FROM shops " +
                        "WHERE is_active = true" +
                        ") AS shops_with_distance " +
                        "WHERE distance < :radiusInKm " +
                        "ORDER BY distance", nativeQuery = true)
        List<Shop> findNearbyShops(@Param("latitude") Double latitude,
                        @Param("longitude") Double longitude,
                        @Param("radiusInKm") Double radiusInKm);

        /**
         * Find shops within a radius filtered by category
         *
         * @param latitude   User's latitude
         * @param longitude  User's longitude
         * @param radiusInKm Search radius in kilometers
         * @param category   Shop category filter
         * @return List of shops in the specified category sorted by distance
         */
        @Query(value = "SELECT * FROM ( " +
                        "SELECT *, " +
                        "(6371 * acos(cos(radians(:latitude)) * cos(radians(latitude)) * " +
                        "cos(radians(longitude) - radians(:longitude)) + sin(radians(:latitude)) * " +
                        "sin(radians(latitude)))) AS distance " +
                        "FROM shops " +
                        "WHERE is_active = true AND category = :category" +
                        ") AS shops_with_distance " +
                        "WHERE distance < :radiusInKm " +
                        "ORDER BY distance", nativeQuery = true)
        List<Shop> findNearbyShopsByCategory(@Param("latitude") Double latitude,
                        @Param("longitude") Double longitude,
                        @Param("radiusInKm") Double radiusInKm,
                        @Param("category") String category);

        /**
         * Find shops within a radius with minimum rating
         *
         * @param latitude   User's latitude
         * @param longitude  User's longitude
         * @param radiusInKm Search radius in kilometers
         * @param minRating  Minimum average rating
         * @return List of shops meeting rating criteria sorted by distance
         */
        @Query(value = "SELECT * FROM ( " +
                        "SELECT *, " +
                        "(6371 * acos(cos(radians(:latitude)) * cos(radians(latitude)) * " +
                        "cos(radians(longitude) - radians(:longitude)) + sin(radians(:latitude)) * " +
                        "sin(radians(latitude)))) AS distance " +
                        "FROM shops " +
                        "WHERE is_active = true AND rating_avg >= :minRating" +
                        ") AS shops_with_distance " +
                        "WHERE distance < :radiusInKm " +
                        "ORDER BY distance", nativeQuery = true)
        List<Shop> findNearbyShopsByRating(@Param("latitude") Double latitude,
                        @Param("longitude") Double longitude,
                        @Param("radiusInKm") Double radiusInKm,
                        @Param("minRating") Double minRating);

        /**
         * Find shops by slug (URL-friendly name)
         * 
         * @param slug Shop slug
         * @return Shop with matching slug
         */
        Shop findBySlug(String slug);

        /**
         * Search shops by name (supports Myanmar and English)
         * 
         * @param keyword Search keyword
         * @return List of shops matching the name
         */
        @Query("SELECT s FROM Shop s WHERE " +
                        "LOWER(s.name) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
                        "LOWER(s.nameMm) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
                        "LOWER(s.nameEn) LIKE LOWER(CONCAT('%', :keyword, '%'))")
        List<Shop> searchByShopName(@Param("keyword") String keyword);

        /**
         * Search shops by menu item/food name
         * 
         * @param foodName Food/menu item name to search for
         * @return List of shops that have matching menu items
         */
        @Query("SELECT DISTINCT s FROM Shop s " +
                        "JOIN s.menuCategories mc " +
                        "JOIN mc.items mi " +
                        "WHERE s.isActive = true AND " +
                        "(LOWER(mi.name) LIKE LOWER(CONCAT('%', :foodName, '%')) OR " +
                        "LOWER(mi.nameMm) LIKE LOWER(CONCAT('%', :foodName, '%')) OR " +
                        "LOWER(mi.nameEn) LIKE LOWER(CONCAT('%', :foodName, '%')))")
        List<Shop> searchByMenuItemName(@Param("foodName") String foodName);

        /**
         * Universal search - searches both shop name and menu items
         *
         * @param keyword Search keyword
         * @return List of shops matching either shop name or menu items
         */
        @Query("SELECT DISTINCT s FROM Shop s " +
                        "LEFT JOIN s.menuCategories mc " +
                        "LEFT JOIN mc.items mi " +
                        "WHERE s.isActive = true AND " +
                        "((LOWER(s.name) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
                        "LOWER(s.nameMm) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
                        "LOWER(s.nameEn) LIKE LOWER(CONCAT('%', :keyword, '%'))) OR " +
                        "(LOWER(mi.name) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
                        "LOWER(mi.nameMm) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
                        "LOWER(mi.nameEn) LIKE LOWER(CONCAT('%', :keyword, '%'))))")
        List<Shop> searchShops(@Param("keyword") String keyword);

        /**
         * Find top 10 shops by trending score in descending order.
         * 
         * @return List of top 10 trending shops.
         */
        List<Shop> findTop10ByOrderByTrendingScoreDesc();

        /**
         * Find shops by category ordered by rating
         * 
         * @param category Shop category
         * @return List of shops in category
         */
        List<Shop> findByCategoryOrderByRatingAvgDesc(String category);

        /**
         * Find shops by category
         * 
         * @param category Shop category
         * @return List of shops in category
         */
        List<Shop> findByCategory(String category);

        /**
         * Find active and verified shops
         * 
         * @return List of verified shops
         */
        List<Shop> findByIsActiveTrueAndIsVerifiedTrue();

        /**
         * Find shops by township
         * 
         * @param township Township name
         * @return List of shops in township
         */
        List<Shop> findByTownship(String township);

        /**
         * Count active shops by township
         */
        @Query("SELECT new org.th.dto.LocationCountDTO(s.township, COUNT(s)) " +
                        "FROM Shop s WHERE s.isActive = true AND s.township IS NOT NULL " +
                        "GROUP BY s.township ORDER BY COUNT(s) DESC")
        List<LocationCountDTO> countShopsByTownship();

        /**
         * Find shops in given categories, excluding specific IDs
         * Used for recommendations
         */
        @Query("SELECT s.id FROM Shop s WHERE s.isActive = true " +
                        "AND s.category IN :categories " +
                        "AND s.id NOT IN :excludedIds " +
                        "ORDER BY s.trendingScore DESC, s.ratingAvg DESC")
        List<Long> findIdsByCategoryInAndIdNotIn(
                        @Param("categories") List<String> categories,
                        @Param("excludedIds") List<Long> excludedIds);

        /**
         * Find top shops by trending score (fallback for recommendations)
         */
        List<Shop> findTop10ByIsActiveTrueAndIdNotInOrderByTrendingScoreDesc(List<Long> excludedIds);

        /**
         * Find nearby trending shops within radius
         */
        @Query(value = "SELECT *, " +
                        "(6371 * acos(cos(radians(:latitude)) * cos(radians(latitude)) * " +
                        "cos(radians(longitude) - radians(:longitude)) + sin(radians(:latitude)) * " +
                        "sin(radians(latitude)))) AS distance " +
                        "FROM shops " +
                        "WHERE is_active = true " +
                        "HAVING distance < :radiusInKm " +
                        "ORDER BY trending_score DESC, distance ASC " +
                        "LIMIT :limit", nativeQuery = true)
        List<Shop> findNearbyTrendingShops(
                        @Param("latitude") Double latitude,
                        @Param("longitude") Double longitude,
                        @Param("radiusInKm") Double radiusInKm,
                        @Param("limit") int limit);

        /**
         * Find recently added shops in specific categories
         */
        @Query("SELECT s FROM Shop s WHERE s.isActive = true " +
                        "AND s.category IN :categories " +
                        "AND s.createdAt >= :since " +
                        "ORDER BY s.createdAt DESC")
        List<Shop> findRecentShopsByCategories(
                        @Param("categories") List<String> categories,
                        @Param("since") LocalDateTime since);

        /**
         * Find shops by categories (for time-based recommendations)
         */
        @Query("SELECT s FROM Shop s WHERE s.isActive = true " +
                        "AND s.category IN :categories " +
                        "ORDER BY s.trendingScore DESC, s.ratingAvg DESC")
        List<Shop> findByCategoryIn(@Param("categories") List<String> categories);

        /**
         * Find nearby shops filtered by categories (optimized - no in-memory filtering)
         * Combines location and category filtering in database
         */
        @Query(value = "SELECT *, " +
                        "(6371 * acos(cos(radians(:latitude)) * cos(radians(latitude)) * " +
                        "cos(radians(longitude) - radians(:longitude)) + sin(radians(:latitude)) * " +
                        "sin(radians(latitude)))) AS distance " +
                        "FROM shops " +
                        "WHERE is_active = true AND category IN :categories " +
                        "HAVING distance < :radiusInKm " +
                        "ORDER BY distance LIMIT :limit", nativeQuery = true)
        List<Shop> findNearbyShopsByCategories(
                        @Param("latitude") Double latitude,
                        @Param("longitude") Double longitude,
                        @Param("radiusInKm") Double radiusInKm,
                        @Param("categories") List<String> categories,
                        @Param("limit") int limit);

        /**
         * Find recent shops with pagination (optimized - no findAll())
         */
        @Query("SELECT s FROM Shop s WHERE s.isActive = true " +
                        "AND s.createdAt >= :since " +
                        "ORDER BY s.createdAt DESC")
        Page<Shop> findRecentShops(@Param("since") LocalDateTime since, Pageable pageable);

        /**
         * Find shops by IDs with photos eagerly loaded (fixes N+1 in DTO conversion)
         */
        @Query("SELECT DISTINCT s FROM Shop s LEFT JOIN FETCH s.photos WHERE s.id IN :ids")
        List<Shop> findByIdInWithPhotos(@Param("ids") List<Long> ids);

        /**
         * Fuzzy search for shops using pg_trgm similarity (Shop Name Only)
         * Requires pg_trgm extension: CREATE EXTENSION IF NOT EXISTS pg_trgm;
         */
        @Query(value = "SELECT * FROM shops s " +
                        "WHERE is_active = true AND (" +
                        "SIMILARITY(s.name, :keyword) > 0.2 OR " +
                        "SIMILARITY(s.name_mm, :keyword) > 0.2 OR " +
                        "SIMILARITY(s.name_en, :keyword) > 0.2) " +
                        "ORDER BY GREATEST(" +
                        "SIMILARITY(s.name, :keyword), " +
                        "SIMILARITY(s.name_mm, :keyword), " +
                        "SIMILARITY(s.name_en, :keyword)) DESC " +
                        "LIMIT 10", nativeQuery = true)
        List<Shop> findShopsFuzzy(@Param("keyword") String keyword);
}
