package com.wasil.ShopSphere.controller;

import com.wasil.ShopSphere.dto.payment.PaymentRequest;
import com.wasil.ShopSphere.dto.payment.PaymentResponse;
import com.wasil.ShopSphere.services.PaymentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
@Tag(name = "Payment API's")
@RestController
@RequestMapping("/payment")
public class PaymentController {
    private final PaymentService paymentService;
    public PaymentController(PaymentService paymentService) {
        this.paymentService = paymentService;
    }

    @Operation(summary = "Used for making payment for the order in checkout.")
    @PostMapping("/me/{orderId}")
    public PaymentResponse processPayment(@Valid @RequestBody PaymentRequest paymentRequest, Authentication authentication, @PathVariable Long orderId) {
        String email = authentication.getName();
        return paymentService.processPayment(email, paymentRequest, orderId);
    }
}
