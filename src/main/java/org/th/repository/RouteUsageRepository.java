package org.th.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.th.entity.DeviceInfo;
import org.th.entity.RouteUsage;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface RouteUsageRepository extends JpaRepository<RouteUsage, Long> {

    // ========== FIXED: Query by DeviceInfo object ==========
    List<RouteUsage> findByDeviceInfoOrderBySearchTimestampDesc(DeviceInfo deviceInfo);

    List<RouteUsage> findTop10ByDeviceInfoOrderBySearchTimestampDesc(DeviceInfo deviceInfo);

    Long countByDeviceInfo(DeviceInfo deviceInfo);

    // ========== ALTERNATIVE: Query by DeviceInfo ID ==========
    List<RouteUsage> findByDeviceInfo_IdOrderBySearchTimestampDesc(Long deviceInfoId);

    List<RouteUsage> findTop10ByDeviceInfo_IdOrderBySearchTimestampDesc(Long deviceInfoId);

    Long countByDeviceInfo_Id(Long deviceInfoId);

    // ========== These are OK (no device involved) ==========
    List<RouteUsage> findByRouteType(String routeType);

    List<RouteUsage> findByTransitMode(String transitMode);

    @Query("SELECT r FROM RouteUsage r WHERE r.searchTimestamp >= ?1 ORDER BY r.searchTimestamp DESC")
    List<RouteUsage> findRecentSearches(LocalDateTime since);

    @Query("SELECT r.origin, r.destination, COUNT(r) as count FROM RouteUsage r " +
            "GROUP BY r.origin, r.destination ORDER BY count DESC")
    List<Object[]> findMostPopularRoutes();

    @Query("SELECT r.routeType, COUNT(r) FROM RouteUsage r GROUP BY r.routeType")
    List<Object[]> countByRouteType();

    @Query("SELECT r.transitMode, COUNT(r) FROM RouteUsage r WHERE r.transitMode IS NOT NULL GROUP BY r.transitMode")
    List<Object[]> countByTransitMode();

    @Query("SELECT FUNCTION('DATE', r.searchTimestamp) as date, COUNT(r) as count " +
            "FROM RouteUsage r " +
            "WHERE r.searchTimestamp >= :startDate " +
            "GROUP BY FUNCTION('DATE', r.searchTimestamp) " +
            "ORDER BY date DESC")
    List<Object[]> getSearchesByDate(@Param("startDate") LocalDateTime startDate);

    // ========== FIXED: Custom queries with join ==========
    @Query("SELECT r FROM RouteUsage r WHERE r.deviceInfo.deviceId = :deviceId ORDER BY r.searchTimestamp DESC")
    List<RouteUsage> findByDeviceDeviceId(@Param("deviceId") String deviceId);

    @Query("SELECT COUNT(r) FROM RouteUsage r WHERE r.deviceInfo.deviceId = :deviceId")
    Long countByDeviceDeviceId(@Param("deviceId") String deviceId);
}