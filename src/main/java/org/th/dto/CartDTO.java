package org.th.dto;

import lombok.Builder;
import lombok.Data;
import java.math.BigDecimal;
import java.util.List;

@Data
@Builder
public class CartDTO {
    private Long id;
    private Long shopId;
    private String shopName;
    private String shopImageUrl;
    private List<CartItemDTO> items;
    private BigDecimal subtotal;
    private BigDecimal deliveryFee;
    private BigDecimal total;
    private Integer totalItems;
}
