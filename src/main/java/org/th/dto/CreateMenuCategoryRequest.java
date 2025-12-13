package org.th.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
@Schema(description = "Request object for creating a menu category")
public class CreateMenuCategoryRequest {

    @NotBlank(message = "Name is required")
    private String name;

    private String nameMm;
    private String nameEn;

    private Integer displayOrder = 0;
    private Boolean isActive = true;
}
