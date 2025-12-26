package org.th.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MenuSubCategoryDTO {
    private Long id;
    private String name;
    private String nameMm;
    private String nameEn;
    private String slug;
    private Integer displayOrder;
    private Boolean isActive;
    private Long menuCategoryId;
    private String imageUrl;
    private List<String> photoUrls;
}
