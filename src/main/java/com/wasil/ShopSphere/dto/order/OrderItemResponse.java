package com.wasil.ShopSphere.dto.order;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class OrderItemResponse {
    private Long orderItemId;
    private Long productId;
    private Integer quantity;
    private BigDecimal price;
}
