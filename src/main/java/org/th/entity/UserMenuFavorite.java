package org.th.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.th.entity.shops.MenuItem;

import java.time.LocalDateTime;

/**
 * Entity for tracking user's favorite menu items (dishes)
 */
@Entity
@Table(name = "user_menu_favorites", uniqueConstraints = {
        @UniqueConstraint(columnNames = { "user_id", "menu_item_id" })
}, indexes = {
        @Index(name = "idx_menu_fav_user", columnList = "user_id"),
        @Index(name = "idx_menu_fav_item", columnList = "menu_item_id"),
        @Index(name = "idx_menu_fav_created", columnList = "created_at")
})
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserMenuFavorite {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "menu_item_id", nullable = false)
    private MenuItem menuItem;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "notes")
    private String notes;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }
}
