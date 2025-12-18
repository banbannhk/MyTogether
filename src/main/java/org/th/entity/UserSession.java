package org.th.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.th.entity.enums.SessionEntryPoint;

import java.time.LocalDateTime;

/**
 * Tracks complete user sessions for analytics
 */
@Entity
@Table(name = "user_sessions", indexes = {
        @Index(name = "idx_session_id", columnList = "session_id"),
        @Index(name = "idx_session_user", columnList = "user_id"),
        @Index(name = "idx_session_device", columnList = "device_info_id"),
        @Index(name = "idx_session_start", columnList = "session_start"),
        @Index(name = "idx_sessions_start_between", columnList = "session_start"),
        @Index(name = "idx_session_active", columnList = "is_active")
})
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserSession {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "session_id", unique = true, nullable = false, length = 100)
    private String sessionId; // UUID

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user; // Nullable for guest sessions

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "device_info_id")
    private DeviceInfo deviceInfo;

    @Column(name = "session_start", nullable = false)
    private LocalDateTime sessionStart;

    @Column(name = "session_end")
    private LocalDateTime sessionEnd; // Nullable if session is active

    @Builder.Default
    @Column(name = "activity_count")
    private Integer activityCount = 0;

    @Builder.Default
    @Column(name = "shops_viewed")
    private Integer shopsViewed = 0;

    @Builder.Default
    @Column(name = "searches_performed")
    private Integer searchesPerformed = 0;

    @Enumerated(EnumType.STRING)
    @Column(name = "entry_point", length = 50)
    private SessionEntryPoint entryPoint;

    @Column(name = "exit_point", length = 100)
    private String exitPoint; // Last action before leaving

    @Column(name = "duration_seconds")
    private Integer durationSeconds; // Calculated on session end

    @Builder.Default
    @Column(name = "is_active")
    private Boolean isActive = true;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
        if (sessionStart == null) {
            sessionStart = LocalDateTime.now();
        }
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
