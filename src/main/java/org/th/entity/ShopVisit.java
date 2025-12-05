package org.th.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.th.entity.shops.Shop;

import java.time.LocalDateTime;

/**
 * Entity for tracking user check-ins/visits to shops
 */
@Entity
@Table(name = "shop_visits",
    indexes = {
        @Index(name = "idx_visit_user", columnList = "user_id"),
        @Index(name = "idx_visit_shop", columnList = "shop_id"),
        @Index(name = "idx_visit_date", columnList = "visited_at")
    }
)
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ShopVisit {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "shop_id", nullable = false)
    private Shop shop;

    @Column(name = "visited_at", nullable = false)
    private LocalDateTime visitedAt;

    @Column(name = "visit_duration_minutes")
    private Integer visitDurationMinutes;

    @Column(name = "was_helpful")
    private Boolean wasHelpful; // User feedback after visit

    @Column(name = "notes", columnDefinition = "TEXT")
    private String notes;

    @Column(name = "check_in_latitude")
    private Double checkInLatitude;

    @Column(name = "check_in_longitude")
    private Double checkInLongitude;

    @PrePersist
    protected void onCreate() {
        if (visitedAt == null) {
            visitedAt = LocalDateTime.now();
        }
    }
}