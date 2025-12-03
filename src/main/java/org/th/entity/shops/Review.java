package org.th.entity.shops;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.th.entity.User;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "reviews")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Review {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "shop_id", nullable = false)
    private Shop shop;

    // ========== ADD USER RELATIONSHIP ==========
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;
    // ==========================================

    @Column(nullable = false)
    private Integer rating;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String comment;

    @Column(name = "comment_mm", columnDefinition = "TEXT")
    private String commentMm;

    @Column(name = "reviewer_name", length = 255)
    private String reviewerName;

    @Column(name = "reviewer_email", length = 255)
    private String reviewerEmail;

    @Column(name = "helpful_count")
    private Integer helpfulCount = 0;

    @Column(name = "is_visible")
    private Boolean isVisible = true;

    @Column(name = "is_verified")
    private Boolean isVerified = false;

    @Column(name = "owner_response", columnDefinition = "TEXT")
    private String ownerResponse;

    @Column(name = "owner_response_mm", columnDefinition = "TEXT")
    private String ownerResponseMm;

    @Column(name = "owner_response_at")
    private LocalDateTime ownerResponseAt;

    @OneToMany(mappedBy = "review", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ReviewPhoto> photos = new ArrayList<>();

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;
}