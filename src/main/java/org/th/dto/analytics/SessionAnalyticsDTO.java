package org.th.dto.analytics;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.th.entity.enums.SessionEntryPoint;

import java.util.Map;

/**
 * Session analytics statistics
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SessionAnalyticsDTO {
    private Long totalSessions;
    private Double averageDurationSeconds;
    private Double averageActivitiesPerSession;
    private Double averageShopsViewed;
    private Double averageSearches;
    private Map<SessionEntryPoint, Long> topEntryPoints;
    private Map<String, Long> topExitPoints;
    private Long activeSessions;
}
