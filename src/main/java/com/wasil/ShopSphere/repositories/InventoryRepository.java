package com.wasil.ShopSphere.repositories;

import com.wasil.ShopSphere.model.Inventory;
import com.wasil.ShopSphere.model.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface InventoryRepository extends JpaRepository<Inventory, Long> {
    Optional<Inventory> findByProduct(Product product);
}
