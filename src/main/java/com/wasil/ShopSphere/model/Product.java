package com.wasil.ShopSphere.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.Instant;


@Entity
@Data
@Table(
        indexes = {
                @Index(name = "idx_product_category", columnList = "category_id"),
                @Index(name = "idx_product_price", columnList = "prod_price"),
                @Index(name = "idx_product_active", columnList = "prod_is_active")
        }
)
@NoArgsConstructor
public class Product {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long prodId;

    private String prodName;

    private BigDecimal prodPrice;

    private String prodDescription;

    @Column(nullable = false)
    private Boolean prodIsActive;

    @CreationTimestamp
    private Instant prodCreatedAt;
    @UpdateTimestamp
    private Instant prodUpdatedAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id", nullable = false)
    private Category category;
}
