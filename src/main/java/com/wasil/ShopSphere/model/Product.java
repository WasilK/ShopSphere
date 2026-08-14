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

    private String prodName;

    private BigDecimal prodPrice;

    private String prodDescription;

    private Integer prodStock;

    @CreationTimestamp
    private Instant prodCreatedAt;
    @UpdateTimestamp
    private Instant prodUpdatedAt;

    public Product(String prodName, BigDecimal prodPrice, String prodDescription, Integer prodStock) {
        this.prodName = prodName;
        this.prodPrice = prodPrice;
        this.prodDescription = prodDescription;
        this.prodStock = prodStock;
    }
}
