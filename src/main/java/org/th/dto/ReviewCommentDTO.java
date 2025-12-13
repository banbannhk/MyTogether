package org.th.dto;

import lombok.Builder;
import lombok.Data;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
public class ReviewCommentDTO {
    private Long id;
    private String content;
    private String userName;
    private LocalDateTime createdAt;
    private List<ReviewCommentDTO> replies;
}
