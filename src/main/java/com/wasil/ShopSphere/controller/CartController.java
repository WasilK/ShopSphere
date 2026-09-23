package com.wasil.ShopSphere.controller;

import com.wasil.ShopSphere.dto.cart.AddToCartRequest;
import com.wasil.ShopSphere.dto.cart.CartResponse;
import com.wasil.ShopSphere.dto.cart.UpdateCartItemRequest;
import com.wasil.ShopSphere.services.CartService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@Tag(name = "Cart API's")
@RestController
@RequestMapping("/cart")
public class CartController {
    private final CartService cartService;
    public CartController(CartService cartService) {
        this.cartService = cartService;
    }
    @Operation(summary = "Adds products to cart.")
    @PostMapping("/me")
    public CartResponse addToCart(Authentication authentication ,@Valid @RequestBody AddToCartRequest request) {
        String email = authentication.getName();
        return cartService.addToCart(email, request);
    }
    @Operation(summary = "Updates cart items quantity using product id.")
    @PutMapping("me/items/{prodId}")
        public CartResponse updateCartItemQuantity(Authentication authentication, @PathVariable Long prodId, @Valid @RequestBody UpdateCartItemRequest request){
        String email = authentication.getName();
           return cartService.updateCartItemQuantity(email, prodId, request);
        }
    @Operation(summary = "Removes cart items using product id.")
    @DeleteMapping("me/items/{prodId}")
    public CartResponse removeCartItem(Authentication authentication, @PathVariable Long prodId){
        String email = authentication.getName();
        return cartService.removeCartItem(email, prodId);
    }
    @Operation(summary = "Clears the entire cart.")
    @PutMapping("/me")
    public CartResponse clearCart(Authentication authentication){
        String email = authentication.getName();
        return cartService.clearCart(email);
    }
    @Operation(summary = "Gets the user's cart")
    @GetMapping("/me")
    public CartResponse getMyCart(Authentication authentication){
        String email = authentication.getName();
        return cartService.getMyCart(email);
    }
    }

