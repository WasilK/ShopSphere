package com.wasil.ShopSphere.dto.payment;

import com.wasil.ShopSphere.model.PaymentMethod;
import com.wasil.ShopSphere.model.PaymentStatus;
import lombok.Data;

import java.math.BigDecimal;
import java.time.Instant;

@Data
public class PaymentResponse {

    private Long paymentId;

    private Long orderId;

    private PaymentMethod paymentMethod;

    private PaymentStatus paymentStatus;

    private BigDecimal amount;

    private String transactionId;

    private Instant createdAt;

    private Instant updatedAt;
}
