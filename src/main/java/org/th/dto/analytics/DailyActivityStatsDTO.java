package org.th.dto.analytics;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DailyActivityStatsDTO {
    private LocalDate date;
    private String deviceId;
    private long totalActions;
    private long searchCount;
    private long viewShopCount;
    private long viewNearbyCount;
}
