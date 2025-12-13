package org.th.entity.shops;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Entity
@Table(name = "menu_item_review_photos")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class MenuItemReviewPhoto {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 1000)
    private String url;

    @Column(length = 1000)
    private String thumbnailUrl;

    @ToString.Exclude
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "menu_item_review_id", nullable = false)
    private MenuItemReview menuItemReview;
}
