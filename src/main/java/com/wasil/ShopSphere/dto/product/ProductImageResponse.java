package com.wasil.ShopSphere.dto.product;

import lombok.Data;

@Data
public class ProductImageResponse {
    private Long id;
    private String imageUrl;
    private Boolean primaryImage;
    private Long prodId;
}
