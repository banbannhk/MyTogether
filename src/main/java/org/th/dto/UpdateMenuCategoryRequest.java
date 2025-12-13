package org.th.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "Request object for updating a menu category")
public class UpdateMenuCategoryRequest {

    private String name;
    private String nameMm;
    private String nameEn;
    private Integer displayOrder;
    private Boolean isActive;
}
