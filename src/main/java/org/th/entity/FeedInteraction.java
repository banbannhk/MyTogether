package org.th.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.th.entity.enums.FeedInteractionAction;
import org.th.entity.enums.FeedSectionType;

import java.time.LocalDateTime;

/**
 * Tracks user interactions with personalized feed
 */
@Entity
@Table(name = "feed_interactions", indexes = {
        @Index(name = "idx_feed_section_timestamp", columnList = "section_type, timestamp"),
        @Index(name = "idx_feed_action_section", columnList = "action, section_type")
})
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FeedInteraction {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user; // Nullable for guest users

    @Column(name = "device_id", length = 255)
    private String deviceId;

    @Column(name = "session_id", length = 100)
    private String sessionId;

    @Enumerated(EnumType.STRING)
    @Column(name = "section_type", nullable = false, length = 50)
    private FeedSectionType sectionType;

    @Column(name = "shop_id")
    private Long shopId;

    @Column(name = "shop_name", length = 255)
    private String shopName; // Denormalized for analytics

    @Column(name = "position")
    private Integer position; // Position in feed (1-10)

    @Enumerated(EnumType.STRING)
    @Column(name = "action", nullable = false, length = 50)
    private FeedInteractionAction action;

    @Column(name = "timestamp", nullable = false)
    private LocalDateTime timestamp;

    @PrePersist
    protected void onCreate() {
        if (timestamp == null) {
            timestamp = LocalDateTime.now();
        }
    }
}
