package org.th.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ReviewPhotoDTO {
    private Long id;
    private String url;
    private String thumbnailUrl;
}
