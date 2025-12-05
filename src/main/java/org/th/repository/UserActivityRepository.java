package org.th.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.th.dto.analytics.DailyActivityStatsDTO;
import org.th.dto.analytics.ShopPopularityDTO;
import org.th.dto.analytics.LocationStatsDTO;
import org.th.entity.UserActivity;
import org.th.entity.enums.ActivityType;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface UserActivityRepository extends JpaRepository<UserActivity, Long> {

        List<UserActivity> findByUserIdOrderByCreatedAtDesc(Long userId);

        List<UserActivity> findByDeviceIdOrderByCreatedAtDesc(String deviceId);

        List<UserActivity> findByActivityTypeAndCreatedAtAfter(ActivityType type, LocalDateTime after);

        long countByActivityType(ActivityType type);

        /**
         * Get daily activity stats for a specific device
         */
        @Query("SELECT new org.th.dto.analytics.DailyActivityStatsDTO(" +
                        "CAST(a.createdAt AS LocalDate), a.deviceId, COUNT(a), " +
                        "SUM(CASE WHEN a.activityType = 'SEARCH_QUERY' THEN 1 ELSE 0 END), " +
                        "SUM(CASE WHEN a.activityType = 'VIEW_SHOP' THEN 1 ELSE 0 END), " +
                        "SUM(CASE WHEN a.activityType = 'VIEW_NEARBY' THEN 1 ELSE 0 END)) " +
                        "FROM UserActivity a " +
                        "WHERE a.deviceId = :deviceId AND a.createdAt BETWEEN :start AND :end " +
                        "GROUP BY CAST(a.createdAt AS LocalDate), a.deviceId " +
                        "ORDER BY CAST(a.createdAt AS LocalDate) DESC")
        List<DailyActivityStatsDTO> getDailyStatsByDevice(
                        @Param("deviceId") String deviceId,
                        @Param("start") LocalDateTime start,
                        @Param("end") LocalDateTime end);

        /**
         * Get most popular shops by view count
         */
        @Query("SELECT new org.th.dto.analytics.ShopPopularityDTO(" +
                        "a.targetId, a.targetName, COUNT(a), COUNT(DISTINCT a.deviceId)) " +
                        "FROM UserActivity a " +
                        "WHERE a.activityType = 'VIEW_SHOP' AND a.createdAt BETWEEN :start AND :end " +
                        "GROUP BY a.targetId, a.targetName " +
                        "ORDER BY COUNT(a) DESC")
        List<ShopPopularityDTO> getMostPopularShops(
                        @Param("start") LocalDateTime start,
                        @Param("end") LocalDateTime end);

        /**
         * Count usage by activity type
         */
        @Query("SELECT a.activityType, COUNT(a) FROM UserActivity a " +
                        "WHERE a.createdAt BETWEEN :start AND :end " +
                        "GROUP BY a.activityType")
        List<Object[]> getFeatureUsageStats(
                        @Param("start") LocalDateTime start,
                        @Param("end") LocalDateTime end);

        /**
         * Get stats by OS Name
         */
        @Query("SELECT a.osName, COUNT(a) FROM UserActivity a " +
                        "WHERE a.createdAt BETWEEN :start AND :end " +
                        "GROUP BY a.osName")
        List<Object[]> getOsStats(
                        @Param("start") LocalDateTime start,
                        @Param("end") LocalDateTime end);

        /**
         * Get stats by Category
         */
        @Query("SELECT a.targetName, COUNT(a) FROM UserActivity a " +
                        "WHERE a.activityType = 'VIEW_CATEGORY' AND a.createdAt BETWEEN :start AND :end " +
                        "GROUP BY a.targetName " +
                        "ORDER BY COUNT(a) DESC")
        List<Object[]> getCategoryStats(
                        @Param("start") LocalDateTime start,
                        @Param("end") LocalDateTime end);

        /**
         * Get stats by Location (Township)
         * Joins with Shop table to get township
         */
        @Query("SELECT new org.th.dto.analytics.LocationStatsDTO(s.township, COUNT(a)) " +
                        "FROM UserActivity a JOIN Shop s ON a.targetId = s.id " +
                        "WHERE a.activityType = 'VIEW_SHOP' AND a.createdAt BETWEEN :start AND :end " +
                        "GROUP BY s.township " +
                        "ORDER BY COUNT(a) DESC")
        List<LocationStatsDTO> getLocationStats(
                        @Param("start") LocalDateTime start,
                        @Param("end") LocalDateTime end);

        long countByTargetIdAndActivityTypeAndCreatedAtAfter(
                        Long targetId, ActivityType type, LocalDateTime after);

        /**
         * Find user's most searched keywords (for personalization)
         */
        @Query("SELECT a.searchQuery, COUNT(a) as count " +
                        "FROM UserActivity a " +
                        "WHERE a.user.id = :userId AND a.activityType = 'SEARCH_QUERY' " +
                        "AND a.searchQuery IS NOT NULL " +
                        "GROUP BY a.searchQuery " +
                        "ORDER BY count DESC")
        List<Object[]> findTopSearchQueriesByUser(@Param("userId") Long userId);

        @Query("SELECT a.targetName, COUNT(a) FROM UserActivity a " +
                        "WHERE a.user.id = :userId AND a.activityType IN ('VIEW_CATEGORY', 'VIEW_SHOP') " +
                        "AND a.targetName IS NOT NULL " +
                        "GROUP BY a.targetName ORDER BY COUNT(a) DESC")
        List<Object[]> findTopCategoriesByUser(@Param("userId") Long userId);

        @Query("SELECT a.targetName, COUNT(a) FROM UserActivity a " +
                        "WHERE a.deviceId = :deviceId AND a.activityType IN ('VIEW_CATEGORY', 'VIEW_SHOP') " +
                        "AND a.targetName IS NOT NULL " +
                        "GROUP BY a.targetName ORDER BY COUNT(a) DESC")
        List<Object[]> findTopCategoriesByDevice(@Param("deviceId") String deviceId);

        /**
         * Find recently viewed shops by user
         */
        @Query("SELECT a FROM UserActivity a " +
                        "WHERE a.user.id = :userId AND a.shop.id IS NOT NULL " +
                        "AND a.activityType = 'VIEW_SHOP' " +
                        "ORDER BY a.createdAt DESC")
        List<UserActivity> findRecentlyViewedShopsByUser(@Param("userId") Long userId);

        @org.springframework.data.jpa.repository.Modifying
        @Query("UPDATE UserActivity a SET a.user.id = :userId WHERE a.deviceId = :deviceId AND a.user IS NULL")
        void bindDeviceActivityToUser(@Param("deviceId") String deviceId, @Param("userId") Long userId);

        void deleteByActivityTypeInAndCreatedAtBefore(List<ActivityType> types, LocalDateTime cutoff);

        void deleteByUserIdIsNullAndCreatedAtBefore(LocalDateTime cutoff);
}
