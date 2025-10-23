package org.th.entity.shops;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.th.entity.User;

import java.time.LocalDateTime;

@Entity
@Table(name = "shop_photos")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ShopPhoto {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "shop_id", nullable = false)
    private Shop shop;

    @Column(nullable = false, length = 500)
    private String url;

    @Column(name = "thumbnail_url", length = 500)
    private String thumbnailUrl;

    @Column(name = "photo_type", length = 50)
    private String photoType = "other";

    @Column(name = "is_primary")
    private Boolean isPrimary = false;

    @Column(name = "display_order")
    private Integer displayOrder = 0;

    @CreationTimestamp
    @Column(name = "uploaded_at", updatable = false)
    private LocalDateTime uploadedAt;

    // ========== ADD THIS RELATIONSHIP ==========
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "uploaded_by")
    private User uploadedBy;
    // ==========================================
}