package org.th.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * Lightweight DTO for shop list views
 * Optimized for displaying multiple shops in lists or maps
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ShopListDTO {

    private Long id;

    private String name;
    private String nameMm;
    private String nameEn;

    private String slug;

    private String category;
    private String subCategory;

    private String address;
    private String addressMm;
    private String district;
    private String districtMm;
    private String city;
    private String cityMm;

    private BigDecimal latitude;
    private BigDecimal longitude;

    private BigDecimal ratingAvg;
    private Integer ratingCount;

    private String primaryPhotoUrl;

    private Boolean hasDelivery;
    private Boolean hasParking;
    private Boolean hasWifi;

    private Boolean isVerified;

    // Personalization
    private Boolean isHalal;
    private Boolean isVegetarian;
    private org.th.entity.enums.PricePreference pricePreference;
    private String pricePreferenceMm;

    // Distance from user location (only populated for nearby searches)
    private Double distance;

    // ETA fields
    private Integer minEta;
    private Integer maxEta;
    private String estimatedTime; // Formatted "12 - 18 min"
}
