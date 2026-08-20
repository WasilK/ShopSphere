package com.wasil.ShopSphere.dto.order;

import com.wasil.ShopSphere.model.OrderStatus;
import lombok.Data;

@Data
public class OrderStatusUpdateRequest {
    private OrderStatus newStatus;
}
