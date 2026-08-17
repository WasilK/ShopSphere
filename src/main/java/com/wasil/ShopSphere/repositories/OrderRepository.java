package com.wasil.ShopSphere.repositories;

import com.wasil.ShopSphere.model.Order;
import com.wasil.ShopSphere.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface OrderRepository extends JpaRepository<Order, Long> {
    List<Order> findByUser(User user);
}
