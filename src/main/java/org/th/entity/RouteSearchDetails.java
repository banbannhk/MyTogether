package org.th.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "route_search_details", indexes = {
        @Index(name = "idx_route_usage_id", columnList = "route_usage_id")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
public class RouteSearchDetails {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "route_usage_id", nullable = false)
    private Long routeUsageId;

    @Column(name = "route_index")
    private Integer routeIndex;  // 0, 1, 2 (which alternative)

    @Column(name = "total_routes")
    private Integer totalRoutes;

    @Column(name = "has_fare")
    private Boolean hasFare = false;

    @Column(name = "fare_source", length = 50)
    private String fareSource;  // GOOGLE, CALCULATED, ESTIMATED

    @Column(name = "transit_lines", columnDefinition = "TEXT")
    private String transitLines;  // JSON array

    @Column(name = "num_transfers")
    private Integer numTransfers;

    @Column(name = "walking_distance_km", precision = 10, scale = 2)
    private Double walkingDistanceKm;

    @Column(name = "selected")
    private Boolean selected = false;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }
}