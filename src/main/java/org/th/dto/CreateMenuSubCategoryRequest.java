package org.th.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class CreateMenuSubCategoryRequest {
    @NotBlank(message = "Name is required")
    private String name;

    private String nameMm;
    private String nameEn;
    private Integer displayOrder;
    private Boolean isActive = true;
}
