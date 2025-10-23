package org.th.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Entity
@Table(name = "route_usage", indexes = {
        @Index(name = "idx_search_timestamp", columnList = "search_timestamp")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
public class RouteUsage {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Relationship to DeviceInfo
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "device_info_id", nullable = false)
    private DeviceInfo deviceInfo;

    @Column(name = "origin", nullable = false)
    private String origin;

    @Column(name = "destination", nullable = false)
    private String destination;

    @Column(name = "route_type", length = 50)
    private String routeType;

    @Column(name = "transit_mode", length = 50)
    private String transitMode;

    @Column(name = "distance_km")
    private Double distanceKm;

    @Column(name = "duration_minutes")
    private Integer durationMinutes;

    @Column(name = "fare_amount")
    private Double fareAmount;

    @Column(name = "search_timestamp")
    private LocalDateTime searchTimestamp;

    @Column(name = "ip_address", length = 45)
    private String ipAddress;

    @Column(name = "session_id", length = 255)
    private String sessionId;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        searchTimestamp = LocalDateTime.now();
    }
}