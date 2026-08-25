package com.wasil.ShopSphere.controller;

import com.wasil.ShopSphere.dto.cart.AddToCartRequest;
import com.wasil.ShopSphere.dto.cart.CartResponse;
import com.wasil.ShopSphere.dto.cart.UpdateCartItemRequest;
import com.wasil.ShopSphere.services.CartService;
import jakarta.validation.Valid;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/cart")
public class CartController {
    private final CartService cartService;
    public CartController(CartService cartService) {
        this.cartService = cartService;
    }
    @PostMapping("/me")
    public CartResponse addToCart(Authentication authentication ,@Valid @RequestBody AddToCartRequest request) {
        String email = authentication.getName();
        return cartService.addToCart(email, request);
    }
    @PutMapping("me/items/{prodId}")
        public CartResponse updateCartItemQuantity(Authentication authentication, @PathVariable Long prodId, @Valid @RequestBody UpdateCartItemRequest request){
        String email = authentication.getName();
           return cartService.updateCartItemQuantity(email, prodId, request);
        }
    @DeleteMapping("me/items/{prodId}")
    public CartResponse removeCartItem(Authentication authentication, @PathVariable Long prodId){
        String email = authentication.getName();
        return cartService.removeCartItem(email, prodId);
    }
    @PutMapping("/me")
    public CartResponse clearCart(Authentication authentication){
        String email = authentication.getName();
        return cartService.clearCart(email);
    }
    @GetMapping("/me")
    public CartResponse getMyCart(Authentication authentication){
        String email = authentication.getName();
        return cartService.getMyCart(email);
    }
    }

