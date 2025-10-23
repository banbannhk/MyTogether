package org.th.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "refresh_tokens", indexes = {
        @Index(name = "idx_token", columnList = "token"),
        @Index(name = "idx_device_info_id", columnList = "device_info_id"),
        @Index(name = "idx_user_id", columnList = "user_id"),  // ← Add index
        @Index(name = "idx_device_id", columnList = "device_id")  // ← Add index
})
@Data
@NoArgsConstructor
@AllArgsConstructor
public class RefreshToken {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // ========== RELATIONSHIP ==========
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "device_info_id", nullable = false)
    private DeviceInfo deviceInfo;
    // ==================================

    // ========== ADD THESE FOR CONVENIENCE ==========
    // Direct access to user_id without loading User entity
    @Column(name = "user_id")
    private Long userId;

    // Direct access to device_id without loading DeviceInfo entity
    @Column(name = "device_id", length = 255)
    private String deviceId;
    // ===============================================

    @Column(name = "token", unique = true, nullable = false, length = 500)
    private String token;

    @Column(name = "expires_at", nullable = false)
    private LocalDateTime expiresAt;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "last_used_at")
    private LocalDateTime lastUsedAt;

    @Column(name = "revoked")
    private Boolean revoked = false;

    @Column(name = "revoked_at")
    private LocalDateTime revokedAt;

    @Column(name = "ip_address", length = 45)
    private String ipAddress;

    @Column(name = "user_agent", columnDefinition = "TEXT")
    private String userAgent;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        lastUsedAt = LocalDateTime.now();

        // Auto-populate userId and deviceId from relationships
        if (deviceInfo != null) {
            if (deviceId == null) {
                deviceId = deviceInfo.getDeviceId();
            }
            if (userId == null && deviceInfo.getUser() != null) {
                userId = deviceInfo.getUser().getId();
            }
        }
    }

    @PreUpdate
    protected void onUpdate() {
        lastUsedAt = LocalDateTime.now();
    }

    // Helper methods
    public boolean isExpired() {
        return LocalDateTime.now().isAfter(expiresAt);
    }

    public boolean isValid() {
        return !revoked && !isExpired();
    }
}