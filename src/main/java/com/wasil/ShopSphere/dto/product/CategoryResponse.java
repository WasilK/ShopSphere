package com.wasil.ShopSphere.dto.product;

import lombok.Data;

@Data
public class CategoryResponse {
    private Long categoryId;
    private String categoryName;
    private Boolean categoryIsActive;
}
