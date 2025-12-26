package org.th.dto;

import lombok.Data;

@Data
public class UpdateMenuSubCategoryRequest {
    private String name;
    private String nameMm;
    private String nameEn;
    private Integer displayOrder;
    private Boolean isActive;
}
