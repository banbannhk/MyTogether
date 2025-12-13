package org.th.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

@Data
public class OperatingHourRequest {

    @NotNull
    @Min(1)
    @Max(7)
    @Schema(description = "Day of week (1=Mon, 7=Sun)", example = "1")
    private Integer dayOfWeek;

    @NotNull
    @Pattern(regexp = "^([0-1]?[0-9]|2[0-3]):[0-5][0-9]$", message = "Time must be in HH:mm format")
    @Schema(example = "09:00")
    private String openTime;

    @NotNull
    @Pattern(regexp = "^([0-1]?[0-9]|2[0-3]):[0-5][0-9]$", message = "Time must be in HH:mm format")
    @Schema(example = "22:00")
    private String closeTime;

    private Boolean isClosed = false;
}
