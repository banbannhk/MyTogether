package org.th.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Schema(description = "Request object for creating a menu item")
public class CreateMenuItemRequest {

    @NotBlank(message = "Name is required")
    private String name;

    private String nameMm;
    private String nameEn;

    @NotNull(message = "Price is required")
    @DecimalMin(value = "0.0", message = "Price must be non-negative")
    private BigDecimal price;

    private String currency = "MMK";

    private Boolean isAvailable = true;
    private Boolean isPopular = false;
    private Boolean isVegetarian;
    private Boolean isSpicy;
    private Integer displayOrder;
    private Long subCategoryId;
}
