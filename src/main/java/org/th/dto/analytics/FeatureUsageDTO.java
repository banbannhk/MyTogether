package org.th.dto.analytics;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.th.entity.enums.ActivityType;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FeatureUsageDTO {
    private ActivityType feature;
    private long usageCount;
    private double percentage;
}
