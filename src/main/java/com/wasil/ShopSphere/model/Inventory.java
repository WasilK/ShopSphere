package com.wasil.ShopSphere.model;

import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.Instant;

@Entity
@Data
public class Inventory {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long inventoryId;
    @OneToOne
    @JoinColumn(name = "product_id")
    private Product product;
    private Integer currentStock;
    @UpdateTimestamp
    private Instant updatedAt;
}
