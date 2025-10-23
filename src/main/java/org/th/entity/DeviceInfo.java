// File: src/main/java/org/th/entity/DeviceInfo.java
package org.th.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "device_info", indexes = {
        @Index(name = "idx_is_logged_in", columnList = "is_logged_in")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
public class DeviceInfo {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "device_id", unique = true, nullable = false, length = 255)
    private String deviceId;

    @Column(name = "device_type", length = 20)
    private String deviceType;  // MOBILE, WEB, TABLET

    @Column(name = "device_model", length = 100)
    private String deviceModel;

    @Column(name = "os_name", length = 50)
    private String osName;

    @Column(name = "os_version", length = 50)
    private String osVersion;

    @Column(name = "browser_name", length = 50)
    private String browserName;

    @Column(name = "browser_version", length = 50)
    private String browserVersion;

    @Column(name = "ip_address", length = 45)
    private String ipAddress;

    @Column(name = "user_agent", columnDefinition = "TEXT")
    private String userAgent;

    @Column(name = "first_seen", nullable = false)
    private LocalDateTime firstSeen;

    @Column(name = "last_seen", nullable = false)
    private LocalDateTime lastSeen;

    // JWT Refresh Token Tracking
    @Column(name = "last_token_refresh")
    private LocalDateTime lastTokenRefresh;

    @Column(name = "token_refresh_count")
    private Integer tokenRefreshCount = 0;

    @Column(name = "current_refresh_token", length = 500)
    private String currentRefreshToken;

    @Column(name = "is_logged_in")
    private Boolean isLoggedIn = false;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = true)
    private User user;

    @Column(name = "is_active")
    private Boolean isActive = true;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @OneToMany(mappedBy = "deviceInfo", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<RefreshToken> refreshTokens = new ArrayList<>();

    @OneToMany(mappedBy = "deviceInfo", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<RouteUsage> routeUsage = new ArrayList<>();

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        firstSeen = LocalDateTime.now();
        lastSeen = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
        lastSeen = LocalDateTime.now();
    }
}