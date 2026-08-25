package com.wasil.ShopSphere.dto.product;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class CategoryRequest {
    @NotBlank
    @Size(min = 2, max = 25)
    private String categoryName;
}
