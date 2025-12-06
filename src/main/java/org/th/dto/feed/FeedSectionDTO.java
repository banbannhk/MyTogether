package org.th.dto.feed;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.th.entity.enums.FeedSectionType;

import java.util.List;

/**
 * Represents a single section in the personalized feed
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FeedSectionDTO {
    private String title;
    private String description;
    private FeedSectionType sectionType;
    private List<ShopFeedItemDTO> shops;
    private Integer totalCount; // Total available (might be more than shown)
}
