package com.wasil.ShopSphere.repositories;


import com.wasil.ShopSphere.model.Inventory;
import com.wasil.ShopSphere.model.StockMovement;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;


@Repository
public interface StockMovementRepository extends JpaRepository<StockMovement, Long> {
    List<StockMovement> findByInventory(Inventory inventory);
}
