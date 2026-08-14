package com.wasil.ShopSphere.model;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.validation.constraints.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.Instant;


@Entity
@Data
@NoArgsConstructor
public class Product {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long prodId;

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
    @CreationTimestamp
    private Instant prodCreatedAt;
    @UpdateTimestamp
    private Instant prodUpdatedAt;

    public Product(String prodName, BigDecimal prodPrice, String prodDescription) {
        this.prodName = prodName;
        this.prodPrice = prodPrice;
        this.prodDescription = prodDescription;
    }

}
