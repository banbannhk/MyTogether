package org.th.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.DecimalMin;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Schema(description = "Request object for updating a menu item")
public class UpdateMenuItemRequest {

    private String name;
    private String nameMm;
    private String nameEn;

    @DecimalMin(value = "0.0")
    private BigDecimal price;

    private String currency; // "MMK"

    private Boolean isAvailable;
    private Boolean isPopular;
    private Boolean isVegetarian;
    private Boolean isSpicy;
    private Integer displayOrder;
    private Long subCategoryId;

    // Category ID to move item to another category?
    private Long categoryId;
}
