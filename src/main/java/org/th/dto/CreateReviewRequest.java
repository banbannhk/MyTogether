package org.th.dto;

import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Request DTO for creating a new review
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateReviewRequest {

    @NotNull(message = "Shop ID is required")
    private Long shopId;

    @NotNull(message = "Rating is required")
    @Min(value = 1, message = "Rating must be between 1 and 5")
    @Max(value = 5, message = "Rating must be between 1 and 5")
    private Integer rating;

    @NotBlank(message = "Comment is required")
    @Size(max = 2000, message = "Comment cannot exceed 2000 characters")
    private String comment;

    @Size(max = 2000, message = "Myanmar comment cannot exceed 2000 characters")
    private String commentMm;

    // Optional: for guest reviewers (if authentication is optional)
    private String reviewerName;
    private String reviewerEmail;

    // Optional: photo URLs
    private java.util.List<String> photoUrls;
}
