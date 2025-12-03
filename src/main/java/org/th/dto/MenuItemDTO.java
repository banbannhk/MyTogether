package org.th.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * Menu item DTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MenuItemDTO {

    private Long id;
    private String name;
    private String nameMm;
    private String nameEn;
    private BigDecimal price;
    private String currency;
    private String imageUrl;
    private Boolean isAvailable;
    private Boolean isPopular;
    private Boolean isVegetarian;
    private Boolean isSpicy;
    private Integer displayOrder;
}
