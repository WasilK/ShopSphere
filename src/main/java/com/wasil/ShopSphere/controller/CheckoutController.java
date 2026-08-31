package com.wasil.ShopSphere.controller;

import com.wasil.ShopSphere.dto.order.OrderRequest;
import com.wasil.ShopSphere.dto.order.OrderResponse;
import com.wasil.ShopSphere.services.CheckoutService;
import jakarta.validation.Valid;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/checkout")
public class CheckoutController {
    private final CheckoutService checkoutService;

    public CheckoutController(CheckoutService checkoutService){
        this.checkoutService = checkoutService;
    }
    @PostMapping("/me")
    public OrderResponse createCheckoutOrder(Authentication authentication, @RequestHeader("Idempotency-Key") String idempotencyKey){
        String email = authentication.getName();
        return checkoutService.createCheckoutOrder(email, idempotencyKey);
    }
    @PostMapping("/me/create")
    public OrderResponse directCheckoutOrder(Authentication authentication, @RequestHeader("Idempotency-Key") String idempotencyKey, @Valid @RequestBody OrderRequest orderRequest){
        String email = authentication.getName();
        return checkoutService.directCheckoutOrder(email, idempotencyKey, orderRequest);
    }
}
