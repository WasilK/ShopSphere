package com.wasil.ShopSphere.controller;

import com.wasil.ShopSphere.dto.order.OrderRequest;
import com.wasil.ShopSphere.dto.order.OrderResponse;
import com.wasil.ShopSphere.services.CheckoutService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
@Tag(name = "Checkout API's", description = "Requires an idempotency key. Used to create orders.")
@RestController
@RequestMapping("/checkout")
public class CheckoutController {
    private final CheckoutService checkoutService;
    public CheckoutController(CheckoutService checkoutService){
        this.checkoutService = checkoutService;
    }
    @Operation(summary = "Creates the order of the logged in user using cart items.")
    @PostMapping("/me")
    public OrderResponse createCheckoutOrder(Authentication authentication, @RequestHeader("Idempotency-Key") String idempotencyKey){
        String email = authentication.getName();
        return checkoutService.createCheckoutOrder(email, idempotencyKey);
    }
    @Operation(summary = "Directly creates the order of a logged in user without using cart and uses order items directly.")
    @PostMapping("/me/create")
    public OrderResponse directCheckoutOrder(Authentication authentication, @RequestHeader("Idempotency-Key") String idempotencyKey, @Valid @RequestBody OrderRequest orderRequest){
        String email = authentication.getName();
        return checkoutService.directCheckoutOrder(email, idempotencyKey, orderRequest);
    }
}
