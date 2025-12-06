package org.th.entity.shops;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.th.entity.User;

import java.time.LocalDateTime;

@Entity
@Table(name = "favorites", indexes = {
        @Index(name = "idx_favorites_user_created", columnList = "user_id, created_at")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Favorite {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // ========== ADD USER RELATIONSHIP ==========
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;
    // ==========================================

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "shop_id", nullable = false)
    private Shop shop;

    @Column(columnDefinition = "TEXT")
    private String notes;

    @Column(name = "notes_mm", columnDefinition = "TEXT")
    private String notesMm;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;
}