package org.th.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO for user engagement metrics
 * Used to consolidate multiple count queries into one
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class UserEngagementDTO {
    private Long totalActivities;
    private Long totalFavorites;
    private Long totalReviews;
    private Long recentActivities;
}
