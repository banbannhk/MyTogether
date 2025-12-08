package org.th.dto.feed;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.th.entity.enums.ShopBadge;

import java.math.BigDecimal;
import java.util.List;

/**
 * Enhanced shop representation for feed items
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ShopFeedItemDTO {
    private Long id;
    private String name;
    private String nameMm;
    private String category;
    private String subCategory;
    private BigDecimal ratingAvg;
    private Integer ratingCount;
    private String address;
    private String township;

    // Location info
    private BigDecimal latitude;
    private BigDecimal longitude;
    private Double distanceKm; // Distance from user's location

    // Feed-specific fields
    private List<ShopBadge> badges;
    private List<String> badgeLabelsMm;
    private Double relevanceScore; // Why this shop was recommended (0-100)
    private String matchReason; // Human-readable explanation
    private String matchReasonMm; // Myanmar translation

    // Features
    private Boolean hasDelivery;
    private Boolean hasParking;
    private Boolean hasWifi;

    // Trending info
    private Double trendingScore;
}
