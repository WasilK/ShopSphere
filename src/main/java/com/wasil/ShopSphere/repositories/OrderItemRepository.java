package com.wasil.ShopSphere.repositories;

import com.wasil.ShopSphere.model.Order;
import com.wasil.ShopSphere.model.OrderItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface OrderItemRepository extends JpaRepository<OrderItem, Long> {
    List<OrderItem> findByOrder(Order order);
}
