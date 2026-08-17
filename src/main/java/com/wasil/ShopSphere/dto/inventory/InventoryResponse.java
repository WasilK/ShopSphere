package com.wasil.ShopSphere.dto.inventory;

import lombok.Data;

import java.time.Instant;

@Data
public class InventoryResponse {
    private Long inventoryId;
    private Long productId;
    private Integer currentStock;
    private Instant updatedAt;
}
