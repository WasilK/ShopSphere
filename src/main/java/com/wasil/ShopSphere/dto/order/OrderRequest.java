package com.wasil.ShopSphere.dto.order;

import lombok.Data;

import java.util.List;

@Data
public class OrderRequest {
    private Long userId;

    private List<OrderItemRequest> orderItems;
}
