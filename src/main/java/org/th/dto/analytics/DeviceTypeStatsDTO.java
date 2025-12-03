package org.th.dto.analytics;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DeviceTypeStatsDTO {
    private String type; // e.g., "Android", "iOS", "Mobile", "Desktop"
    private long count;
    private double percentage;
}
