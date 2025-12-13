package org.th.entity.shops;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.th.entity.User;
import org.th.entity.shops.MenuItemReviewPhoto;
import org.th.entity.shops.ReviewComment;

import java.time.LocalDateTime;

@Entity
@Table(name = "menu_item_reviews", indexes = {
        @Index(name = "idx_item_review_item", columnList = "menu_item_id"),
        @Index(name = "idx_item_review_user", columnList = "user_id")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
public class MenuItemReview {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "menu_item_id", nullable = false)
    private MenuItem menuItem;

    @Column(nullable = false)
    private Integer rating; // 1-5

    @Column(columnDefinition = "TEXT")
    private String comment;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @OneToMany(mappedBy = "menuItemReview", cascade = CascadeType.ALL, orphanRemoval = true)
    private java.util.List<MenuItemReviewPhoto> photos = new java.util.ArrayList<>();

    @OneToMany(mappedBy = "menuItemReview", cascade = CascadeType.ALL, orphanRemoval = true)
    private java.util.List<ReviewComment> comments = new java.util.ArrayList<>();
}
