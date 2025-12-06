package org.th.dto.feed;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Main personalized feed response containing all sections
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PersonalizedFeedDTO {
    private FeedSectionDTO forYouNow;
    private FeedSectionDTO trendingNearby;
    private FeedSectionDTO basedOnFavorites;
    private FeedSectionDTO newShops;
    private FeedMetadataDTO metadata;
}
