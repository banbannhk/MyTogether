package org.th.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.th.entity.enums.ActivityType;
import org.th.entity.shops.Shop;

import java.time.LocalDateTime;

@Entity
@Table(name = "user_activities", indexes = {
        @Index(name = "idx_activity_created_at", columnList = "created_at"),
        @Index(name = "idx_activity_type", columnList = "activity_type"),
        @Index(name = "idx_activity_user", columnList = "user_id"),
        @Index(name = "idx_activity_shop", columnList = "shop_id"),
        @Index(name = "idx_activity_device", columnList = "device_id"),
        @Index(name = "idx_activity_os", columnList = "os_name"),
        // Composite Indexes for frequent queries
        @Index(name = "idx_device_created", columnList = "device_id, created_at"),
        @Index(name = "idx_user_created", columnList = "user_id, created_at"),
        @Index(name = "idx_type_created", columnList = "activity_type, created_at"),
        @Index(name = "idx_search_query", columnList = "search_query")
})
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserActivity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "shop_id")
    private Shop shop; // Shop related to this activity (for VIEW_SHOP, etc.)

    @Column(name = "device_id")
    private String deviceId;

    @Column(name = "ip_address")
    private String ipAddress;

    @Column(name = "os_name")
    private String osName;

    @Column(name = "device_type")
    private String deviceType;

    @Enumerated(EnumType.STRING)
    @Column(name = "activity_type", nullable = false)
    private ActivityType activityType;

    @Column(name = "search_query")
    private String searchQuery;

    @Column(name = "category")
    private String category; // Category searched or filtered

    @Column(name = "target_id")
    private Long targetId;

    @Column(name = "target_name")
    private String targetName;

    @Column(name = "session_id")
    private String sessionId; // For grouping activities in same session

    private Double latitude;
    private Double longitude;

    @Column(columnDefinition = "TEXT")
    private String metadata;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }
}
