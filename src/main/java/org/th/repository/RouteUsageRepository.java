// File: src/main/java/org/th/repository/RouteUsageRepository.java
package org.th.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.th.entity.RouteUsage;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface RouteUsageRepository extends JpaRepository<RouteUsage, Long> {

    List<RouteUsage> findByDeviceIdOrderBySearchTimestampDesc(String deviceId);

    List<RouteUsage> findTop10ByDeviceIdOrderBySearchTimestampDesc(String deviceId);

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

    @Query("SELECT COUNT(r) FROM RouteUsage r WHERE r.deviceId = :deviceId")
    Long countSearchesByDevice(@Param("deviceId") String deviceId);

    @Query("SELECT FUNCTION('DATE', r.searchTimestamp) as date, COUNT(r) as count " +
            "FROM RouteUsage r " +
            "WHERE r.searchTimestamp >= :startDate " +
            "GROUP BY FUNCTION('DATE', r.searchTimestamp) " +
            "ORDER BY date DESC")
    List<Object[]> getSearchesByDate(@Param("startDate") LocalDateTime startDate);
}