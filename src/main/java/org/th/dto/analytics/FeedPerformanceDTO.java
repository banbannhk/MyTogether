package org.th.dto.analytics;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.th.entity.enums.FeedSectionType;

import java.util.List;
import java.util.Map;

/**
 * Feed section performance metrics
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FeedPerformanceDTO {
    private FeedSectionType sectionType;
    private Long totalViews;
    private Long totalClicks;
    private Double clickThroughRate; // Percentage
    private Double averageClickPosition;
    private List<TopShopDTO> topShops;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class TopShopDTO {
        private Long shopId;
        private String shopName;
        private Long clickCount;
    }
}
