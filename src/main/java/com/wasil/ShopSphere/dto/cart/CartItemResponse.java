package com.wasil.ShopSphere.dto.cart;

import lombok.Data;

@Data
public class CartItemResponse {
    private Long cartItemId;
    private Long productId;
    private String productName;
    private Integer quantity;
}