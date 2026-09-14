package com.wasil.ShopSphere.repositories;

import com.wasil.ShopSphere.model.Inventory;
import com.wasil.ShopSphere.model.Product;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface InventoryRepository extends JpaRepository<Inventory, Long> {
    Optional<Inventory> findByProduct(Product product);

    /*
     * Row-locking read used anywhere stock is being validated AND
     * decremented in the same transaction (order placement, payment
     * confirmation). Prevents two concurrent requests from both
     * reading the same currentStock and oversubscribing the last unit.
     */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select i from Inventory i where i.product = :product")
    Optional<Inventory> findByProductForUpdate(@Param("product") Product product);
}
