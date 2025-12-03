package org.th.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Shop photo DTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ShopPhotoDTO {

    private Long id;
    private String url;
    private String thumbnailUrl;
    private String photoType;
    private String caption;
    private String captionMm;
    private String captionEn;
    private Boolean isPrimary;
    private Integer displayOrder;
    private LocalDateTime uploadedAt;
}
