package com.wasil.ShopSphere.model;

import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;

import java.time.Instant;

@Entity
@Data
public class StockMovement {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long stockId;
    @ManyToOne
    @JoinColumn(name = "inventory_id")
    private Inventory inventory;
    private Integer quantity;
    @Enumerated(EnumType.STRING)
    private MovementType movementType;
    @CreationTimestamp
    private Instant createdAt;
}
