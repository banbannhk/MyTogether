package org.th.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Complete shop details DTO
 * Includes all shop information and related entities
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ShopDetailDTO {

    private Long id;

    private String name;
    private String nameMm;
    private String nameEn;

    private String slug;

    private String category;
    private String subCategory;

    private BigDecimal latitude;
    private BigDecimal longitude;

    private String address;
    private String addressMm;
    private String township;
    private String city;

    private String phone;
    private String email;

    private String description;
    private String descriptionMm;

    private String specialties;

    private Boolean hasDelivery;
    private Boolean hasParking;
    private Boolean hasWifi;

    private BigDecimal ratingAvg;
    private Integer ratingCount;
    private Integer viewCount;

    private Boolean isActive;
    private Boolean isVerified;

    // Personalization
    private Boolean isHalal;
    private Boolean isVegetarian;
    private org.th.entity.enums.PricePreference pricePreference;
    private String pricePreferenceMm;

    private String primaryPhotoUrl;
    private List<ShopPhotoDTO> photos;
    private List<MenuCategoryDTO> menuCategories;
    private List<ReviewSummaryDTO> recentReviews;
    private List<OperatingHourDTO> operatingHours;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    // Distance from user (optional)
    private Double distance;
}
