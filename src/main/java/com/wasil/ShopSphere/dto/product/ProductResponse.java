package com.wasil.ShopSphere.dto.product;

import jakarta.validation.constraints.*;
import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.Instant;

@Data
public class ProductResponse {
    private Long prodId;
    private String prodName;
    private BigDecimal prodPrice;
    private String prodDescription;
    private Instant prodCreatedAt;
    private Instant prodUpdatedAt;
}
