package org.th.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class AddToCartRequest {
    private Long menuItemId;
    private Integer quantity;
    private String specialInstructions;
}
