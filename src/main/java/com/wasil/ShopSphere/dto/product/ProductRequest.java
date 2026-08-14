package com.wasil.ShopSphere.dto.product;

import jakarta.validation.constraints.*;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class ProductRequest {
    @NotBlank(message = "Product name is required")
    @Size(min = 2, max = 100, message = "Product name must be between 2 and 100 characters")
    private String prodName;

    @NotNull(message = "Product price is required")
    @Positive(message = "Product price must be greater than 0")
    private BigDecimal prodPrice;

    @NotBlank(message = "Product description is required")
    @Size(min = 10, max = 1000, message = "Description must be between 10 and 1000 characters")
    private String prodDescription;

    @NotNull(message = "Product stock is required")
    @PositiveOrZero(message = "Stock cannot be negative")
    private Integer prodStock;
}
