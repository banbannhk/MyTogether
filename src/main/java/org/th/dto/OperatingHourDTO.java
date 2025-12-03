package org.th.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalTime;

/**
 * Operating hours DTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OperatingHourDTO {

    private Long id;
    private Integer dayOfWeek; // 0=Sunday, 6=Saturday
    private LocalTime openingTime;
    private LocalTime closingTime;
    private Boolean isClosed;
}
