package com.wasil.ShopSphere.dto.inventory;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;

@Data
public class RestockRequest {

    @NotNull
    private Long productId;

    @NotNull
    @Positive(message = "Restock quantity must be greater than zero")
    private Integer quantity;
}
