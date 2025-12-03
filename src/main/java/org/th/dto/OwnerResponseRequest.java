package org.th.dto;

import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Request DTO for owner response to a review
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OwnerResponseRequest {

    @Size(max = 1000, message = "Response cannot exceed 1000 characters")
    private String ownerResponse;

    @Size(max = 1000, message = "Myanmar response cannot exceed 1000 characters")
    private String ownerResponseMm;
}
