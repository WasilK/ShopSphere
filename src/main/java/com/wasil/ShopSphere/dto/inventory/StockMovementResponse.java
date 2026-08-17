package com.wasil.ShopSphere.dto.inventory;

import com.wasil.ShopSphere.model.MovementType;
import lombok.Data;

import java.time.Instant;

@Data
public class StockMovementResponse {
    private Long stockId;
    private Integer quantity;
    private MovementType movementType;
    private Instant createdAt;
}
