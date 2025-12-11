package org.th.entity.shops;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import org.th.entity.User;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "shops", indexes = {
        @Index(name = "idx_shops_category_active", columnList = "category, is_active"),
        @Index(name = "idx_shops_trending_active", columnList = "trending_score, is_active"),
        @Index(name = "idx_shops_created_active", columnList = "created_at, is_active"),
        @Index(name = "idx_shop_name", columnList = "name"),
        @Index(name = "idx_shop_name_mm", columnList = "name_mm"),
        @Index(name = "idx_shop_name_en", columnList = "name_en"),
        @Index(name = "idx_shop_township", columnList = "township"),
        @Index(name = "idx_shop_category", columnList = "category"),
        @Index(name = "idx_shop_is_active", columnList = "is_active"),
        @Index(name = "idx_shop_rating", columnList = "rating_avg DESC")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Shop {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 255)
    private String name;

    @Column(name = "name_mm", nullable = false, length = 255)
    private String nameMm;

    @Column(name = "name_en", length = 255)
    private String nameEn;

    @Column(unique = true, nullable = false, length = 255)
    private String slug;

    @Column(nullable = false, length = 100)
    private String category;

    @Column(name = "sub_category", length = 100)
    private String subCategory;

    @Column(nullable = false, precision = 10, scale = 8)
    private BigDecimal latitude;

    @Column(nullable = false, precision = 11, scale = 8)
    private BigDecimal longitude;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String address;

    @Column(name = "address_mm", columnDefinition = "TEXT")
    private String addressMm;

    @Column(length = 100)
    private String township;

    @Column(length = 100)
    private String city = "Yangon";

    @Column(length = 50)
    private String phone;

    @Column(length = 255)
    private String email;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(name = "description_mm", columnDefinition = "TEXT")
    private String descriptionMm;

    @Column(columnDefinition = "TEXT")
    private String specialties;

    @Column(name = "has_delivery")
    private Boolean hasDelivery = false;

    @Column(name = "has_parking")
    private Boolean hasParking = false;

    @Column(name = "has_wifi")
    private Boolean hasWifi = false;

    @Column(name = "rating_avg", precision = 3, scale = 2)
    private BigDecimal ratingAvg = BigDecimal.ZERO;

    @Column(name = "rating_count")
    private Integer ratingCount = 0;

    @Column(name = "trending_score")
    private Double trendingScore = 0.0;

    @Column(name = "view_count")
    private Integer viewCount = 0;

    @ToString.Exclude
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "owner_id")
    private User owner;

    @Column(name = "is_active")
    private Boolean isActive = true;

    @Column(name = "is_verified")
    private Boolean isVerified = false;

    @ToString.Exclude
    @OneToMany(mappedBy = "shop", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ShopPhoto> photos = new ArrayList<>();

    @ToString.Exclude
    @OneToMany(mappedBy = "shop", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<MenuCategory> menuCategories = new ArrayList<>();

    @ToString.Exclude
    @OneToMany(mappedBy = "shop", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Review> reviews = new ArrayList<>();

    @ToString.Exclude
    @OneToMany(mappedBy = "shop", cascade = CascadeType.ALL, orphanRemoval = true)
    private java.util.Set<OperatingHour> operatingHours = new java.util.HashSet<>();

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    // Personalization Fields
    @Column(name = "is_halal")
    private Boolean isHalal = false;

    @Column(name = "is_vegetarian")
    private Boolean isVegetarian = false;

    @Enumerated(EnumType.STRING)
    @Column(name = "price_preference")
    private org.th.entity.enums.PricePreference pricePreference;

    // ========== ADD THIS RELATIONSHIP ==========
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "created_by")
    private User createdBy;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "district_id")
    private org.th.entity.District districtObj;
    // ==========================================
}