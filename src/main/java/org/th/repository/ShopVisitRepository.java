package org.th.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.th.entity.ShopVisit;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface ShopVisitRepository extends JpaRepository<ShopVisit, Long> {

    /**
     * Find all visits for a user
     */
    @Query("SELECT sv FROM ShopVisit sv " +
           "JOIN FETCH sv.shop s " +
           "WHERE sv.user.id = :userId " +
           "ORDER BY sv.visitedAt DESC")
    List<ShopVisit> findByUserIdWithShop(@Param("userId") Long userId);

    /**
     * Find visits to a specific shop by user
     */
    @Query("SELECT sv FROM ShopVisit sv " +
           "WHERE sv.user.id = :userId AND sv.shop.id = :shopId " +
           "ORDER BY sv.visitedAt DESC")
    List<ShopVisit> findByUserIdAndShopId(@Param("userId") Long userId,
                                           @Param("shopId") Long shopId);

    /**
     * Count total visits for a user
     */
    @Query("SELECT COUNT(sv) FROM ShopVisit sv WHERE sv.user.id = :userId")
    Long countByUserId(@Param("userId") Long userId);

    /**
     * Count visits to a specific shop by user
     */
    @Query("SELECT COUNT(sv) FROM ShopVisit sv " +
           "WHERE sv.user.id = :userId AND sv.shop.id = :shopId")
    Long countByUserIdAndShopId(@Param("userId") Long userId,
                                  @Param("shopId") Long shopId);

    /**
     * Find user's most visited shops
     */
    @Query("SELECT sv.shop, COUNT(sv) as visitCount " +
           "FROM ShopVisit sv " +
           "WHERE sv.user.id = :userId " +
           "GROUP BY sv.shop " +
           "ORDER BY visitCount DESC")
    List<Object[]> findMostVisitedShopsByUser(@Param("userId") Long userId);

    /**
     * Find recent visits within date range
     */
    @Query("SELECT sv FROM ShopVisit sv " +
           "WHERE sv.user.id = :userId " +
           "AND sv.visitedAt BETWEEN :startDate AND :endDate " +
           "ORDER BY sv.visitedAt DESC")
    List<ShopVisit> findByUserIdAndDateRange(@Param("userId") Long userId,
                                               @Param("startDate") LocalDateTime startDate,
                                               @Param("endDate") LocalDateTime endDate);

    /**
     * Check if user has visited shop before
     */
    boolean existsByUserIdAndShopId(Long userId, Long shopId);
}
