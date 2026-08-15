package com.wasil.ShopSphere.dto.order;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;

@Data
public class OrderItemRequest {
    @NotNull(message = "Product ID is required")
    @Positive(message = "Product ID must be greater than zero")
    private Long productId;
    @NotNull(message = "Quantity is required")
    @Positive(message = "Quantity should be greater than zero.")
    private Integer quantity;
}
