package org.th.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MenuItemReviewDTO {

    private Long id;
    private Long menuItemId;
    private String menuItemName;
    private String reviewerName;
    private Integer rating;
    private String comment;
    private LocalDateTime createdAt;

    private List<ReviewPhotoDTO> photos;
    private List<ReviewCommentDTO> comments;
}
