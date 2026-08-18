package com.wasil.ShopSphere.repositories;

import com.wasil.ShopSphere.model.Cart;
import com.wasil.ShopSphere.model.CartItem;
import com.wasil.ShopSphere.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface CartRepository extends JpaRepository<Cart, Long> {
    Optional<Cart> findByUser(User user);
}
