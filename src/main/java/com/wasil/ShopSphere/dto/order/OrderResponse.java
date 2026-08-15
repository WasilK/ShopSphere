package com.wasil.ShopSphere.dto.order;

import com.wasil.ShopSphere.model.OrderStatus;
import lombok.Data;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

@Data
public class OrderResponse {
    private Long orderId;
    private Long userId;
    private OrderStatus orderStatus;
    private BigDecimal totalAmount;
    private Instant orderCreatedAt;
    private Instant orderUpdatedAt;
    private List<OrderItemResponse> orderItems;
}
