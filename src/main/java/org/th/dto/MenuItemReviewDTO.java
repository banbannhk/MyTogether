package org.th.dto;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class MenuItemReviewDTO {
    private Long id;
    private Long menuItemId;
    private String menuItemName;
    private String reviewerName;
    private Integer rating;
    private String comment;
    private LocalDateTime createdAt;
}
