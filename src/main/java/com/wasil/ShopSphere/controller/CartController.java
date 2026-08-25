package com.wasil.ShopSphere.controller;

import com.wasil.ShopSphere.dto.cart.AddToCartRequest;
import com.wasil.ShopSphere.dto.cart.CartResponse;
import com.wasil.ShopSphere.dto.cart.UpdateCartItemRequest;
import com.wasil.ShopSphere.services.CartService;
import jakarta.validation.Valid;
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
    @PutMapping("/items/{cartItemId}")
        public CartResponse updateCartItemQuantity(@PathVariable Long cartItemId, @Valid @RequestBody UpdateCartItemRequest request){
            return cartService.updateCartItemQuantity(cartItemId, request);
        }
    @DeleteMapping("/items/{cartItemId}")
    public CartResponse removeCartItem(@PathVariable Long cartItemId){
        return cartService.removeCartItem(cartItemId);
    }
    @PutMapping("/{userId}")
    public CartResponse clearCart(@PathVariable Long userId){
        return cartService.clearCart(userId);
    }
    }

