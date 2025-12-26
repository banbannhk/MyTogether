package org.th.dto;

import lombok.Builder;
import lombok.Data;
import java.math.BigDecimal;

@Data
@Builder
public class CartItemDTO {
    private Long id;
    private Long menuItemId;
    private String name;
    private BigDecimal price;
    private Integer quantity;
    private String imageUrl;
    private String specialInstructions;
    private BigDecimal itemTotal;
}
