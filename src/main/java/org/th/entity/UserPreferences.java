package org.th.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Entity for storing user preferences and settings
 */
@Entity
@Table(name = "user_preferences")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserPreferences {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    private User user;

    // Favorite categories (stored as comma-separated string)
    @Column(name = "favorite_categories", length = 500)
    private String favoriteCategories; // "Restaurant,Cafe,Market"

    // Favorite cuisines (stored as comma-separated string)
    @Column(name = "favorite_cuisines", length = 500)
    private String favoriteCuisines; // "Burmese,Thai,Chinese"

    // Price range preferences
    @Column(name = "price_range_min")
    private Integer priceRangeMin;

    @Column(name = "price_range_max")
    private Integer priceRangeMax;

    // Dietary restrictions (stored as comma-separated string)
    @Column(name = "dietary_restrictions", length = 500)
    private String dietaryRestrictions; // "Vegetarian,Halal,Vegan"

    // Default search radius
    @Column(name = "preferred_radius_km")
    private Double preferredRadiusKm = 5.0;

    // Language preference
    @Column(name = "preferred_language", length = 10)
    private String preferredLanguage = "en"; // "en" or "mm"

    // Notification settings
    @Column(name = "receive_notifications")
    private Boolean receiveNotifications = true;

    @Column(name = "notification_distance_km")
    private Double notificationDistanceKm = 1.0;

    @Column(name = "notify_new_shops")
    private Boolean notifyNewShops = true;

    @Column(name = "notify_favorite_updates")
    private Boolean notifyFavoriteUpdates = true;

    @Column(name = "notify_special_offers")
    private Boolean notifySpecialOffers = true;

    // Timestamps
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}