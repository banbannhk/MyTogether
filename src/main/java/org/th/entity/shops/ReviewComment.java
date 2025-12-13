package org.th.entity.shops;

import lombok.Data;
import lombok.ToString;
import org.hibernate.annotations.CreationTimestamp;
import org.th.entity.User;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "review_comments")
@Data
public class ReviewComment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    @ToString.Exclude
    private User user;

    @Column(columnDefinition = "TEXT", nullable = false)
    private String content;

    // Link to Shop Review
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "shop_review_id")
    @ToString.Exclude
    private ShopReview shopReview;

    // Link to Menu Item Review
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "menu_item_review_id")
    @ToString.Exclude
    private MenuItemReview menuItemReview;

    // Self-referencing for replies
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_comment_id")
    @ToString.Exclude
    private ReviewComment parentComment;

    @OneToMany(mappedBy = "parentComment", cascade = CascadeType.ALL, orphanRemoval = true)
    @ToString.Exclude
    private List<ReviewComment> replies = new ArrayList<>();

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;
}
