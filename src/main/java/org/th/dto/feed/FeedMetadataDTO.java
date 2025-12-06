package org.th.dto.feed;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.th.entity.enums.TimeContext;
import org.th.entity.enums.UserSegment;

import java.time.LocalDateTime;

/**
 * Metadata about feed generation
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FeedMetadataDTO {
    private LocalDateTime generatedAt;
    private UserSegment userSegment;
    private Boolean locationUsed;
    private TimeContext timeContext;
    private Double userLatitude;
    private Double userLongitude;
    private Double radiusKm;
}
