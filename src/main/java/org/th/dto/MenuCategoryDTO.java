package org.th.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * Menu category DTO with items
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MenuCategoryDTO {

    private Long id;
    private String name;
    private String nameMm;
    private String nameEn;
    private Integer displayOrder;
    private Boolean isActive;
    // Enhanced fields
    private String imageUrl;
    private List<PhotoDTO> photos;
    private List<MenuItemDTO> items;
}
