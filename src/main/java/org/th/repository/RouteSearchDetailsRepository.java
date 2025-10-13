package org.th.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.th.entity.RouteSearchDetails;

import java.util.List;

@Repository
public interface RouteSearchDetailsRepository extends JpaRepository<RouteSearchDetails, Long> {

    List<RouteSearchDetails> findByRouteUsageId(Long routeUsageId);

    List<RouteSearchDetails> findBySelectedTrue();

    @Query("SELECT r FROM RouteSearchDetails r WHERE r.hasFare = true")
    List<RouteSearchDetails> findRoutesWithFare();

    @Query("SELECT r.fareSource, COUNT(r) FROM RouteSearchDetails r WHERE r.hasFare = true GROUP BY r.fareSource")
    List<Object[]> countByFareSource();
}