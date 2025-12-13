package org.th.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Review summary DTO for shop details
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReviewSummaryDTO {

    private Long id;
    private Integer rating;
    private String comment;
    private String commentMm;
    private String reviewerName;
    private Integer helpfulCount;
    private String ownerResponse;
    private String ownerResponseMm;
    private LocalDateTime createdAt;

    private java.util.List<ReviewPhotoDTO> photos;
    private java.util.List<ReviewCommentDTO> comments;
}
