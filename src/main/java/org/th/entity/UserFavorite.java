package org.th.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.th.entity.shops.Shop;

import java.time.LocalDateTime;

/**
 * Entity for tracking user's favorite/bookmarked shops
 */
@Entity
@Table(name = "user_favorites",
    uniqueConstraints = {
        @UniqueConstraint(columnNames = {"user_id", "shop_id"})
    },
    indexes = {
        @Index(name = "idx_favorite_user", columnList = "user_id"),
        @Index(name = "idx_favorite_shop", columnList = "shop_id"),
        @Index(name = "idx_favorite_created", columnList = "created_at")
    }
)
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserFavorite {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "shop_id", nullable = false)
    private Shop shop;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "notes")
    private String notes; // Personal notes about why they favorited it

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }
}
