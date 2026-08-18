package com.wasil.ShopSphere.controller;

import com.wasil.ShopSphere.dto.cart.AddToCartRequest;
import com.wasil.ShopSphere.dto.cart.CartResponse;
import com.wasil.ShopSphere.services.CartService;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/cart")
public class CartController {
    private final CartService cartService;
    public CartController(CartService cartService) {
        this.cartService = cartService;
    }
    @PostMapping("/{userId}")
    public CartResponse addToCart(@PathVariable Long userId, @RequestBody AddToCartRequest request) {
        return cartService.addToCart(userId, request);
    }
}
