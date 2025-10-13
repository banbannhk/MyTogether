// File: src/main/java/org/th/entity/RouteUsage.java
package org.th.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Entity
@Table(name = "route_usage")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class RouteUsage {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "device_id", nullable = false)
    private String deviceId;

    @Column(name = "origin", nullable = false)
    private String origin;

    @Column(name = "destination", nullable = false)
    private String destination;

    @Column(name = "route_type")
    private String routeType;  // "TRANSIT", "DRIVING", "WALKING", "BICYCLING"

    @Column(name = "transit_mode")
    private String transitMode;  // "BUS", "BTS", "MRT", "TRAIN"

    @Column(name = "distance_km")
    private Double distanceKm;

    @Column(name = "duration_minutes")
    private Integer durationMinutes;

    @Column(name = "fare_amount")
    private Double fareAmount;

    @Column(name = "search_timestamp")
    private LocalDateTime searchTimestamp;

    @Column(name = "ip_address")
    private String ipAddress;

    @Column(name = "session_id")
    private String sessionId;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        searchTimestamp = LocalDateTime.now();
    }
}