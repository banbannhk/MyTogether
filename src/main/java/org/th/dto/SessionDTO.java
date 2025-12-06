package org.th.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.th.entity.enums.SessionEntryPoint;

import java.time.LocalDateTime;

/**
 * Session details for API responses
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SessionDTO {
    private String sessionId;
    private Long userId;
    private Long deviceId;
    private LocalDateTime sessionStart;
    private LocalDateTime sessionEnd;
    private Integer durationSeconds;
    private Integer activityCount;
    private Integer shopsViewed;
    private Integer searchesPerformed;
    private SessionEntryPoint entryPoint;
    private String exitPoint;
    private Boolean isActive;
}
