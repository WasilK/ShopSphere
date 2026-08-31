package com.wasil.ShopSphere.dto.payment;

import com.wasil.ShopSphere.model.PaymentMethod;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class PaymentRequest {
    @NotNull(message = "Payment method is required")
    private PaymentMethod paymentMethod;
}